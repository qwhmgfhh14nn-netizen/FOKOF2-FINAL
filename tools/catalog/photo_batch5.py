#!/usr/bin/env python3
"""duck.ai (GPT Image) photo templates, batch 5 (additive, ph5_* ids). photo_batch4.py json -> catalog_photo5.json for specs whose image exists."""
import json, os, sys
import photo_batch as P
P.SIZES["16:9"]=(1280,720,203)
_old=P.layout
def layout(asp,acc,scr,k,l1,l2,sub,cta,var,sid=""):
    if asp!="16:9": return _old(asp,acc,scr,k,l1,l2,sub,cta,var,sid)
    e=[["S","RECTANGLE",180.0,150.0,360,150,scr,0.92,0,"grad:b"],["S","RECTANGLE",180.0,175.0,360,90,scr,0.7,0,"grad:b"]]
    def T(t,y,s,b,c,o=1.0): e.append(["T",t,180,y,s,b,c,o])
    T(k,52,14,1,acc); T(l1,86,30,1,"#FFFFFF"); T(l2,120,30,1,acc); T(sub,148,14,1,"#FFFFFF"); T(cta,172,15,1,acc)
    e.append(["S","RECTANGLE",180,184,60,2,acc,1.0,0,""])
    return e
P.layout=layout
P.WHITE2=set(); P.BIGSUB=set()
# id, cat, asp, prompt(unused: images made on duck.ai), accent, scrim, kicker, l1, l2, sub, cta, variant
P.SPECS=[
 ("ph5_social_coffee","منشورات السوشيال ميديا","1:1","","#B8892B","#FFFFFF","صباح الخير","ابدأ يومك","بهدوء","اقتباس وفكرة جديدة كل صباح","تابعنا","l"),
 ("ph5_social_sunset","منشورات السوشيال ميديا","1:1","","#FFD9A3","#1A0A05","مساء الخير","كل غروب","بداية جديدة","لحظات تستاهل المشاركة","شاركها مع حد","b"),
 ("ph5_social_food","منشورات السوشيال ميديا","1:1","","#F5D58A","#2A1204","سفرة عامرة","يوم الجمعة","على السفرة","أطباق مصرية وشامية أصلية","اطلب الآن","b"),
 ("ph5_sale_friday","عروض وتخفيضات","1:1","","#F5D58A","#05070A","بلاك فرايداي","أقوى خصومات","السنة","لحد ٧٠ في المية لمدة ٣ أيام","تسوق الآن","b"),
 ("ph5_sale_tech","عروض وتخفيضات","1:1","","#7FD6FF","#04161A","عروض التقنية","موبايلك الجديد","بنص تمن","أجهزة أصلية وضمان سنة","اشتري دلوقتي","b"),
 ("ph5_prod_shoes","إعلانات المنتجات التجارية","1:1","","#B8892B","#FFFFFF","كوليكشن جديد","خطوتك","أريح","كوتشي خفيف ومريح لكل يوم","اطلب مقاسك","l"),
 ("ph5_prod_candle","إعلانات المنتجات التجارية","1:1","","#B8892B","#FFFFFF","شموع معطرة","ريحة","تريّح بالك","صناعة يدوية بخلاصات طبيعية","اطلبها الآن","l"),
 ("ph5_menu_burger","قوائم طعام ومطاعم","4:5","","#F5B83D","#1A0E05","برجر هاوس","برجر","مفيش زيه","لحم طازج وبطاطس مقرمشة","اطلب الآن","b"),
 ("ph5_menu_pasta","قوائم طعام ومطاعم","4:5","","#F5D58A","#1A0E05","مطبخ إيطالي","باستا","بالكريمة","صلصات طازجة ومعكرونة طرية","شوف المنيو","b"),
 ("ph5_invite_birthday","دعوات ومناسبات","4:5","","#B8892B","#FFFFFF","عيد ميلاد","تعالوا نحتفل","معانا","السبت الساعة ٦ مساء","أكد حضورك","l"),
 ("ph5_invite_graduation","دعوات ومناسبات","4:5","","#B8892B","#FFFFFF","حفل تخرج","اتخرجنا","وفرحتنا كبيرة","ندعوكم لمشاركتنا الفرحة","أكد حضورك","l"),
 ("ph5_banner_eid","بانرات ومطبوعات","16:9","","#F5D58A","#031418","عيد مبارك","عيدكم مبارك","وكل عام وانتم بخير","عروض العيد لفترة محدودة","اكتشف العروض","b"),
]
if __name__=="__main__":
    P.SPECS=[s for s in P.SPECS if os.path.exists(f"{P.ROOT}/photo/{s[0]}.webp")]
    json.dump(P.build("5",500),open(f"{P.ROOT}/catalog_photo5.json","w"),ensure_ascii=False,separators=(",",":"))
    print("wrote",len(P.SPECS))
