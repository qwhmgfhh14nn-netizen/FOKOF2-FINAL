import sys; sys.path.insert(0,'tools/catalog')
from PIL import Image, ImageDraw, ImageFont
import math
from gen import hx, FONT_B, FONT_R
S=2
def shape_img(it):
    w,h=max(1,int(it['w']*S)),max(1,int(it['h']*S))
    im=Image.new('RGBA',(w,h),(0,0,0,0)); d=ImageDraw.Draw(im); c=hx(it['c'])+(int(it['o']*255),)
    s=it['s']
    if it['tag'].startswith('grad'): d.rectangle([0,0,w,h],fill=c)
    elif s=='RECTANGLE': d.rectangle([0,0,w,h],fill=c)
    elif s=='ROUNDED_RECT': d.rounded_rectangle([0,0,w-1,h-1],radius=min(w,h)*.18,fill=c)
    elif s=='CIRCLE': d.ellipse([0,0,w-1,h-1],fill=c)
    elif s=='DIAMOND': d.polygon([(w/2,0),(w,h/2),(w/2,h),(0,h/2)],fill=c)
    elif s=='HEXAGON': d.polygon([(w*.25,0),(w*.75,0),(w,h/2),(w*.75,h),(w*.25,h),(0,h/2)],fill=c)
    elif s=='STAR':
        pts=[];ro=min(w,h)/2;ri=ro*.42
        for i in range(10):
            r=ro if i%2==0 else ri;a=math.pi/5*i-math.pi/2;pts.append((w/2+r*math.cos(a),h/2+r*math.sin(a)))
        d.polygon(pts,fill=c)
    return im
def render(t):
    h=int(t['h']*S); W=360*S
    base=Image.new('RGBA',(W,h))
    a,b=hx(t['bs']),hx(t['be']); px=base.load()
    for y in range(h):
        for x in range(0,W):
            f=(x/W*0.5+y/h*0.5); px[x,y]=tuple(int(a[i]*(1-f)+b[i]*f) for i in range(3))+(255,)
    for it in t['items']:
        if it['t']=='S':
            im=shape_img(it)
            if it['r']: im=im.rotate(-it['r'],expand=True,resample=Image.BICUBIC)
            base.alpha_composite(im.convert('RGBA'),(0,0)) if False else None
            layer=Image.new('RGBA',base.size,(0,0,0,0)); layer.paste(im,(int(it['x']*S-im.width/2),int(it['y']*S-im.height/2)),im)
            base=Image.alpha_composite(base,layer)
        else:
            f=(FONT_B if it['b'] else FONT_R).font_variant(size=int(it['fs']*S))
            layer=Image.new('RGBA',base.size,(0,0,0,0)); d=ImageDraw.Draw(layer)
            d.text((it['x']*S,it['y']*S),it['text'],font=f,fill=hx(it['c'])+(int(it['o']*255),),anchor='mm',direction='rtl',language='ar')
            base=Image.alpha_composite(base,layer)
    return base.convert('RGB')
def sheet(ts,path,cols=6,cell=260):
    ims=[render(t) for t in ts]
    rows=(len(ims)+cols-1)//cols
    sh=Image.new('RGB',(cols*cell,rows*cell),(60,60,60))
    for i,im in enumerate(ims):
        im.thumbnail((cell-10,cell-10)); sh.paste(im,((i%cols)*cell+5,(i//cols)*cell+5))
    sh.save(path)
