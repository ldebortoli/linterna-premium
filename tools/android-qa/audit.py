from pathlib import Path
import qa,json,re,sys
def contains(outer,inner):return outer[0]<=inner[0]<inner[2]<=outer[2] and outer[1]<=inner[1]<inner[3]<=outer[3]
def safe_rect(window,w,h):
 left=top=right=bottom=0
 m=re.search(r'mDisplayCutout=DisplayCutout\{insets=Rect\((\d+), (\d+) - (\d+), (\d+)\)',window)
 if m:left,top,right,bottom=map(int,m.groups())
 for typ,a,b,c,d in re.findall(r'InsetsSource id=\S+ type=(statusBars|navigationBars) frame=\[(\d+),(\d+)\]\[(\d+),(\d+)\] visible=true',window):
  a,b,c,d=map(int,(a,b,c,d))
  if typ=='statusBars' and b==0:top=max(top,d)
  elif typ=='navigationBars':
   if a==0 and c==w and d==h:bottom=max(bottom,h-b)
   elif a==0 and b==0 and d==h:left=max(left,c)
   elif c==w and b==0 and d==h:right=max(right,w-a)
 return left,top,w-right,h-bottom
def audit(slug):
 out=qa.PROJECTS[slug]/'reports'/qa.RUN;records=[];errors=[]
 for path in sorted(out.glob('[0-9][0-9]-*.json')):
  d=json.loads(path.read_text(encoding='utf-8'));w,h=d['screen'];window=path.with_name(path.stem+'-window.txt').read_text(encoding='utf-8')
  safe=safe_rect(window,w,h);nodes=[n for n in qa.own(slug,d) if n['visible']]
  foreground=[n for n in nodes if n['text'] or n['description']]
  for n in foreground:
   if n['bounds'][2]>n['bounds'][0] and n['bounds'][3]>n['bounds'][1] and not contains(safe,n['bounds']):errors.append((path.name,'foreground outside runtime safe drawing area',n['text'] or n['description'],n['bounds'],safe))
  scrolls=[n['bounds'] for n in nodes if n['scrollable']]
  # Include fixed app actions as well as fully visible scrolled controls. Ad
  # WebViews own their hit targets and are outside this application's contract.
  webviews=[n['bounds'] for n in nodes if n['class']=='android.webkit.WebView']
  controls=[n for n in nodes if n['clickable'] and n['enabled']
   and contains(safe,n['bounds']) and not any(contains(r,n['bounds']) for r in webviews)
   and (not any(max(r[0],n['bounds'][0])<min(r[2],n['bounds'][2]) and max(r[1],n['bounds'][1])<min(r[3],n['bounds'][3]) for r in scrolls)
    or any(contains(r,n['bounds']) for r in scrolls))]
  for n in controls:
   a,b,c,e=n['bounds']
   if c-a<88 or e-b<88:errors.append((path.name,'target smaller than 44dp',n['bounds']))
   names=[x['text'] or x['description'] for x in foreground if contains(n['bounds'],x['bounds'])]
   if not (n['text'] or n['description'] or names):errors.append((path.name,'unnamed control',n['bounds']))
  records.append(dict(file=path.name,safeDrawingBounds=safe,foreground=len(foreground),fullyVisibleControls=len(controls)))
 result=dict(status='passed' if not errors else 'failed',captures=len(records),records=records,errors=errors)
 (out/'geometry-audit.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
 print(slug,result['status'],len(records),'captures',len(errors),'errors',flush=True)
 for e in errors[:12]:print(e,flush=True)
 return result
if __name__=='__main__':audit(sys.argv[1])
