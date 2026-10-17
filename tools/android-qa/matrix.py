import qa,time,json,re,sys
CASES=[('es-AR',1,0,'gestural'),('ar',2,0,'gestural'),('ar',2,1,'threebutton'),('de',2,0,'threebutton'),('gn',1,0,'gestural'),('quz',1,0,'gestural'),('bm-Nkoo',1.3,0,'gestural'),('hi',1.3,1,'gestural'),('iu',1.3,0,'gestural'),('yua',1,0,'gestural'),('es-AR',2,0,'threebutton')]
def stable(slug):
 end=time.monotonic()+75
 while time.monotonic()<end:
  d=qa.probe()
  if any(n['text'] and n['package']==qa.pkg(slug) for n in d['nodes']):return d
  time.sleep(1)
 raise RuntimeError('Application semantics did not become ready')
def start(slug):
 r=qa.shell('am','start','-W','-n',qa.pkg(slug)+f'/com.{slug}premium.app.MainActivity',timeout=60)
 # Android's fixed -W timeout can precede the first frame on a cold interpreter.
 assert 'Error:' not in r,r
 return stable(slug)
def label(slug,d,query):return next(n for n in qa.own(slug,d) if query in n['text'])
def main(slug,start_index=0):
 out=qa.PROJECTS[slug]/'reports'/qa.RUN;out.mkdir(parents=True,exist_ok=True)
 qa.language(slug,'es-AR');qa.shell('run-as',qa.pkg(slug),'rm','-f','shared_prefs/premium_entitlement.xml')
 results=json.loads((out/'matrix.json').read_text(encoding='utf-8'))[:start_index] if start_index else []
 for i,(code,font,rot,nav) in enumerate(CASES):
  if i<start_index:continue
  name=f'{i:02}-{code}-font{font}-rot{rot}-{nav}'
  print(slug,name,'START',flush=True)
  qa.shell('wm','size','640x960' if i==10 else 'reset')
  if i in (1,2):qa.shell('cmd','overlay','enable-exclusive','--category','com.android.internal.display.cutout.emulation.hole')
  else:qa.shell('cmd','overlay','disable','com.android.internal.display.cutout.emulation.hole')
  qa.profile(code,font,rot,nav);qa.language(slug,code);d=start(slug);d=qa.ensure_rotation(slug,rot)
  d=qa.save(slug,name+'-top');assert (d['screen'][0]>d['screen'][1])==bool(rot%2),'Screenshot orientation mismatch';nodes=qa.own(slug,d)
  assert any(n['scrollable'] for n in nodes),'Main screen must scroll'
  p=qa.menu(slug,d);assert any(n['scrollable'] for n in qa.own(slug,p)),'Menu must scroll'
  if code=='es-AR':assert any(n['selected'] or n.get('checked') for n in qa.own(slug,p)),'Selected language state missing'
  qa.save(slug,name+'-menu');qa.shell('input','keyevent',111)
  # Reach the end, proving that a small viewport does not hide the final actions.
  for _ in range(6):
   d=qa.probe();qa.scroll(slug,d)
  d=qa.save(slug,name+'-bottom')
  stored=qa.shell('run-as',qa.pkg(slug),'cat','shared_prefs/app_language.xml')
  assert code in stored,'Language persistence missing'
  results.append(dict(name=name,code=code,font=font,rotation=rot,navigation=nav,status='captured-awaiting-review'))
  (out/'matrix.json').write_text(json.dumps(results,ensure_ascii=False,indent=2),encoding='utf-8')
  print(slug,name,'CAPTURED',flush=True)
if __name__=='__main__':
 try:
  main(sys.argv[1],int(sys.argv[2]) if len(sys.argv)>2 else 0)
 finally:
  qa.verify_emulator()
  qa.shell('input','keyevent',3);time.sleep(1);qa.shell('am','force-stop',qa.pkg(sys.argv[1]))
