#!/usr/bin/env python3
import sys,subprocess
from pathlib import Path
if len(sys.argv)!=2 or not sys.argv[1].startswith('https://github.com/'): raise SystemExit('Usage: python3 scripts/enable-gitops.py https://github.com/USERNAME/REPO.git')
text=Path('gitops/application.example.yaml').read_text().replace('https://github.com/YOUR_USERNAME/shoppingcart-local.git',sys.argv[1])
subprocess.run(['kubectl','--context','kind-shoppingcart','apply','-f','-'],input=text,text=True,check=True)
