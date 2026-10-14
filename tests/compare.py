#!/usr/bin/env python3
"""Run only in local mode. Changes the two demo workloads; restores good images afterward."""
import argparse,csv,json,statistics,subprocess,time,urllib.request,urllib.error
from pathlib import Path
K=['kubectl','--context','kind-shoppingcart','-n','shoppingcart']
def cmd(args,check=True):return subprocess.run(K+args,check=check,text=True,capture_output=True)
def patch(kind,name,image):
 payload=json.dumps([{'op':'replace','path':'/spec/template/spec/containers/0/image','value':image}])
 cmd(['patch',kind,name,'--type=json','-p',payload])
def healthy():cmd(['wait','--for=jsonpath={.status.phase}=Healthy','rollout/shoppingcart','--timeout=600s'])
def sample(url):
 start=time.monotonic();version='';code=0
 try:
  with urllib.request.urlopen(urllib.request.Request(url,headers={'Connection':'close'}),timeout=3) as res:
   code=res.status;version=res.headers.get('X-App-Version','');res.read()
 except urllib.error.HTTPError as e:code=e.code;version=e.headers.get('X-App-Version','')
 except Exception:pass
 return code,version,(time.monotonic()-start)*1000

def run(strategy,url,change,seconds,out):
 print('Measuring '+strategy+' for '+str(seconds)+' seconds...',flush=True)
 rows=[];start=time.monotonic();injected=False;change_time=None
 while time.monotonic()-start<seconds:
  elapsed=time.monotonic()-start
  if elapsed>=15 and not injected:
   change();change_time=time.monotonic()-start;injected=True
  t=time.monotonic();code,version,latency=sample(url)
  rows.append({'elapsed_s':round(t-start,3),'status':code,'version':version,'latency_ms':round(latency,3),'post_change':injected})
  time.sleep(max(0,0.25-(time.monotonic()-t)))
 with (out/(strategy+'-requests.csv')).open('w',newline='') as f:
  w=csv.DictWriter(f,fieldnames=rows[0].keys());w.writeheader();w.writerows(rows)
 post=[x for x in rows if x['post_change']];failed=[x for x in post if not 200<=x['status']<400]
 latencies=sorted(x['latency_ms'] for x in post if 200<=x['status']<400)
 first=next((x['elapsed_s'] for x in post if not 200<=x['status']<400),None)
 # Recovery means the last observed error is followed by at least 20 consecutive successful samples.
 last_index=max((i for i,x in enumerate(post) if not 200<=x['status']<400),default=-1)
 recovered=first is not None and len(post)-last_index-1>=20
 return {'strategy':strategy,'post_change_requests':len(post),'failed_requests':len(failed),'error_pct':round(100*len(failed)/max(len(post),1),2),'mean_success_latency_ms':round(statistics.mean(latencies),3) if latencies else '', 'p95_success_latency_ms':latencies[min(len(latencies)-1,int(0.95*len(latencies)))] if latencies else '', 'observed_recovery_s':round(post[last_index+1]['elapsed_s']-change_time,3) if recovered else 'not observed','first_error_s':first if first is not None else 'not observed'}

def main():
 p=argparse.ArgumentParser();p.add_argument('--seconds',type=int,default=180);args=p.parse_args()
 if args.seconds<150:raise SystemExit('Use at least 150 seconds to allow scraping, analysis and recovery.')
 if subprocess.run(['kubectl','--context','kind-shoppingcart','-n','argocd','get','application','shoppingcart'],capture_output=True).returncode==0:
  raise SystemExit('Run comparison before enabling GitOps, or delete only the Argo CD Application first (see guide).')
 out=Path('results')/time.strftime('comparison-%Y%m%d-%H%M%S');out.mkdir(parents=True)
 cmd(['apply','-f','tests/baseline.yaml']);cmd(['apply','-f','tests/baseline-service.yaml'])
 cmd(['rollout','status','deployment/shoppingcart-baseline','--timeout=300s'])
 patch('rollout','shoppingcart','shoppingcart:v1');healthy()
 summaries=[]
 try:
  summaries.append(run('rolling','http://localhost:8081/demo/products',lambda:patch('deployment','shoppingcart-baseline','shoppingcart:bad'),args.seconds,out))
  # Avoid competing baseline failures/resources during the canary trial.
  patch('deployment','shoppingcart-baseline','shoppingcart:v1')
  cmd(['rollout','status','deployment/shoppingcart-baseline','--timeout=300s'])
  summaries.append(run('canary','http://localhost:8080/demo/products',lambda:patch('rollout','shoppingcart','shoppingcart:bad'),args.seconds,out))
  (out/'rollout.json').write_text(cmd(['get','rollout','shoppingcart','-o','json']).stdout)
  (out/'analysisruns.json').write_text(cmd(['get','analysisrun','-o','json']).stdout)
  with (out/'summary.csv').open('w',newline='') as f:
   w=csv.DictWriter(f,fieldnames=summaries[0].keys());w.writeheader();w.writerows(summaries)
  print(json.dumps(summaries,indent=2));print('Saved to',out)
 finally:
  patch('deployment','shoppingcart-baseline','shoppingcart:v1')
  patch('rollout','shoppingcart','shoppingcart:v1')
  healthy()
if __name__=='__main__':main()
