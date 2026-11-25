import sys; sys.path.insert(0,'tools/catalog')
import gen
from gen import tw, contrast, blend
def validate(out):
    errs=[]
    ids=set(); fps=set(); heads=set()
    bycat={}
    for t in out:
        if t['id'] in ids: errs.append(('dup id',t['id']))
        ids.add(t['id'])
        if tuple(t['fp']) in fps: errs.append(('dup fp',t['id']))
        fps.add(tuple(t['fp']))
        if (t['cat'],t['head']) in heads: errs.append(('dup head',t['id']))
        heads.add((t['cat'],t['head']))
        bycat.setdefault(t['cat'],[]).append(tuple(t['fp']))
        h=t['h']
        texts=[i for i in t['items'] if i['t']=='T']
        if len(t['items'])>120: errs.append(('too many',t['id']))
        for i in texts:
            if tw(i['text'],i['fs'],i['b'])>330: errs.append(('text wide',t['id'],i['text']))
            if not (8<i['y']<h-8): errs.append(('text off',t['id'],i['text']))
        ys=sorted(i['y'] for i in texts)
        if any(b-a<10 for a,b in zip(ys,ys[1:])): errs.append(('text overlap',t['id']))
        z=t['zone']
        if z[1]<-1 or z[3]>h+1: errs.append(('zone off',t['id'],z[1],z[3],h))
    for c,l in bycat.items():
        for i in range(len(l)):
            for j in range(i+1,len(l)):
                if gen.fdist(l[i],l[j])<2: errs.append(('close fp',c))
    return errs
if __name__=='__main__':
    out,_=gen.generate(20); e=validate(out); print(len(out),len(e)); print(e[:12])
