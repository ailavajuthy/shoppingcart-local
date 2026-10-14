#!/usr/bin/env python3
import sys,re
from pathlib import Path
if len(sys.argv)!=3: raise SystemExit("Usage: update-image.py IMAGE TAG")
image,tag=sys.argv[1:]
if not re.fullmatch(r'[a-z0-9./:_-]+',image) or not re.fullmatch(r'[A-Za-z0-9_.-]+',tag): raise SystemExit('Invalid image/tag')
p=Path('gitops/app/kustomization.yaml')
s=p.read_text()
s,n=re.subn(r'(?m)^(\s*newName:) .+$',lambda m:m.group(1)+' '+image,s)
assert n==1
s,n=re.subn(r'(?m)^(\s*newTag:) .+$',lambda m:m.group(1)+' '+tag,s)
assert n==1
p.write_text(s)
