from pathlib import Path
import os, subprocess, json, time, re, shlex, struct, sys
sys.stdout.reconfigure(encoding="utf-8")
ROOT = Path(__file__).resolve().parents[2]
SLUG = json.loads((ROOT / "apps/mobile/app.json").read_text(encoding="utf-8"))["app"]["slug"].removesuffix("-premium")
assert SLUG in ("linterna", "gemidos"), "This harness only targets the two PREMIUM apps"
SERIAL = os.environ.get("PREMIUM_QA_SERIAL", "emulator-5556")
assert re.fullmatch(r"emulator-\d+", SERIAL), "Physical devices are outside this test harness"
RUN = os.environ.get("PREMIUM_QA_RUN", "android-ui")
assert re.fullmatch(r"[A-Za-z0-9_-]+", RUN), "Use a report folder name, not a path"
BASE = ROOT / "reports/android-qa/harness"
BASE.mkdir(parents=True, exist_ok=True)
PROJECTS = {SLUG: ROOT}

def verify_emulator():
    assert adb("emu", "avd", "name").splitlines()[0] == "Premium_QA_API36", "Only the isolated PREMIUM QA AVD is allowed"

def adb(*args,timeout=45):
 r=subprocess.run(['adb','-s',SERIAL,*map(str,args)],capture_output=True,timeout=timeout)
 if r.returncode: raise RuntimeError(r.stderr.decode(errors='replace')[-600:])
 return r.stdout.decode('utf-8',errors='replace').strip()
def shell(*args,timeout=45):return adb('shell',shlex.join(map(str,args)),timeout=timeout)
def probe(query='',action=''):
 raw=shell('am','instrument','-w','-e','query',query,'-e','action',action,'qa.premium/qa.premium.Probe')
 match=re.search(r'INSTRUMENTATION_RESULT: json=(.*)',raw)
 if not match:raise RuntimeError(raw)
 d=json.loads(match[1]); assert d['nodes'],'empty accessibility root'
 assert not any("isn't responding" in n['text'] for n in d['nodes']),'System UI ANR'
 return d
def screenshot(path):
 r=subprocess.run(['adb','-s',SERIAL,'exec-out','screencap','-p'],capture_output=True,check=True,timeout=25)
 assert r.stdout[:8]==b'\x89PNG\r\n\x1a\n';Path(path).write_bytes(r.stdout)
 return struct.unpack('>II',r.stdout[16:24])
def save(slug,name):
 out=PROJECTS[slug]/'reports'/RUN;out.mkdir(parents=True,exist_ok=True)
 d=probe();d['screen']=screenshot(out/(name+'.png'))
 (out/(name+'.json')).write_text(json.dumps(d,ensure_ascii=False,indent=2),encoding='utf-8')
 (out/(name+'-window.txt')).write_text(shell('dumpsys','window'),encoding='utf-8')
 return d
def tap(n):
 x1,y1,x2,y2=n['bounds'];shell('input','tap',(x1+x2)//2,(y1+y2)//2)
def pkg(slug):return f'com.{slug}premium.app.demo.debug'
def start(slug):
 r=shell('am','start','-W','-n',pkg(slug)+f'/com.{slug}premium.app.MainActivity',timeout=60)
 assert 'Status: ok' in r,r
 time.sleep(.5)
def language(slug,code):
 verify_emulator()
 p=pkg(slug);shell('input','keyevent',3);time.sleep(.2);shell('am','force-stop',p)
 f=BASE/'language.xml';f.write_text(f'<map><string name="selected_language">{code}</string></map>',encoding='utf-8')
 adb('push',f,'/data/local/tmp/premium-language.xml');shell('run-as',p,'mkdir','-p','shared_prefs')
 shell('run-as',p,'cp','/data/local/tmp/premium-language.xml','shared_prefs/app_language.xml')
def own(slug,d):return [n for n in d['nodes'] if n['package']==pkg(slug)]
def scroll(slug,d,up=True):
 n=next(n for n in own(slug,d) if n['scrollable'])
 x1,y1,x2,y2=n['bounds'];x=(x1+x2)//2;top=y1+min(60,(y2-y1)//5);bottom=y2-min(60,(y2-y1)//5)
 shell('input','swipe',x,bottom if up else top,x,top if up else bottom,300)
def profile(code='es-AR',font=1,rot=0,nav='gestural'):
 verify_emulator()
 shell('wm','density','320')
 shell('cmd','overlay','enable-exclusive','--category','com.android.internal.systemui.navbar.'+nav)
 shell('settings','put','system','font_scale',font);shell('wm','user-rotation','lock',rot)
 shell('input','keyevent',224);shell('wm','dismiss-keyguard')
def ensure_rotation(slug,rotation):
 shell('wm','user-rotation','lock',rotation)
 deadline=time.monotonic()+30
 while time.monotonic()<deadline:
  d=probe();frames=[n['bounds'] for n in own(slug,d) if n['class']=='android.widget.FrameLayout']
  if frames:
   x1,y1,x2,y2=max(frames,key=lambda r:(r[2]-r[0])*(r[3]-r[1]))
   if (x2-x1>y2-y1)==bool(rotation%2):
    time.sleep(.5);return d
  time.sleep(.3)
 raise RuntimeError('Requested display rotation did not become visible')

def menu(slug,d):
 for _ in range(12):
  matches=[n for n in own(slug,d) if '🌐' in n['text'] and n['visible']]
  if matches:break
  scroll(slug,d);d=probe()
 else:raise RuntimeError('Language control not reachable')
 tap(matches[0])
 for _ in range(15):
  p=probe()
  if any(n['text'] in ('Español (Argentina)','✓ Español (Argentina)') for n in own(slug,p)):return p
  time.sleep(.3)
 raise RuntimeError('Language popup did not open')
if __name__=='__main__':
 slug=sys.argv[1];code=sys.argv[2] if len(sys.argv)>2 else 'es-AR'
 profile(code,float(sys.argv[3]) if len(sys.argv)>3 else 1,int(sys.argv[4]) if len(sys.argv)>4 else 0,sys.argv[5] if len(sys.argv)>5 else 'gestural')
 language(slug,code);start(slug);d=save(slug,'inspect-'+code)
 print(json.dumps([n for n in own(slug,d) if n['text'] or n['description'] or n['clickable']],ensure_ascii=False,indent=2))
