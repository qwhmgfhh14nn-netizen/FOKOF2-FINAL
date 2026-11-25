import sys,json,os; sys.path.insert(0,'tools/catalog')
import gen, validate
path='tools/catalog/catalog.json'
existing=json.load(open(path)) if os.path.exists(path) else []
batch=int(sys.argv[1]); per=int(sys.argv[2])
new,_=gen.generate(per,existing,batch)
errs=validate.validate(new)
print('new',len(new),'errs',errs[:5])
assert not errs
allc=existing+[gen.enc(t) for t in new]
fps=[tuple(e['fp']) for e in allc]; assert len(fps)==len(set(fps)); ids=[e['id'] for e in allc]; assert len(ids)==len(set(ids))
json.dump(allc,open(path,'w'),ensure_ascii=False,separators=(',',':'))
print('total',len(allc),os.path.getsize(path))
