#!/usr/bin/env python3
"""duck.ai (GPT Image) photo templates, batch 4 (additive, ph4_* ids). photo_batch4.py json -> catalog_photo4.json for specs whose image exists."""
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
 ("ph4_social_launch","منشورات السوشيال ميديا","1:1","","#F5B83D","#1A1033","إطلاق جديد","شغلك","يستاهل يتشاف","خطة نشر أسبوعية جاهزة","ابدأ دلوقتي","b"),
 ("ph4_social_fitness","منشورات السوشيال ميديا","1:1","","#F5B83D","#05070A","تحدي الشهر","ابدأ","النهارده","تمارين يومية قصيرة وفعالة","انضم للتحدي","b"),
 ("ph4_social_travel","منشورات السوشيال ميديا","1:1","","#F5D58A","#2A1204","رحلات","جهز شنطتك","ويلا نسافر","عروض رحلات لكل الميزانيات","احجز رحلتك","b"),
 ("ph4_ad_electronics","الإعلانات الممولة","1:1","","#7FD6FF","#06121E","سماعات لاسلكية","صوت نقي","من غير أسلاك","عزل ضوضاء وبطارية ٣٠ ساعة","اطلب الآن","b"),
 ("ph4_ad_realestate","الإعلانات الممولة","1:1","","#F5D58A","#2A1204","فيلات للبيع","بيت أحلامك","في انتظارك","تقسيط مريح ومعاينة مجانية","احجز معاينة","b"),
 ("ph4_ad_clinic","الإعلانات الممولة","1:1","","#B8892B","#FFFFFF","عيادة أسنان","ابتسامتك","تستاهل الأفضل","كشف مجاني وخطة علاج واضحة","احجزي ميعاد","l"),
 ("ph4_banner_summer","بانرات ومطبوعات","16:9","","#FFD9A3","#05202A","عروض الصيف","الصيف","وصل","خصومات على الرحلات والمصايف","احجز دلوقتي","b"),
 ("ph4_banner_ramadan","بانرات ومطبوعات","16:9","","#F5D58A","#050B1E","رمضان كريم","كل سنة","وانتم طيبين","عروض رمضان بدأت","اكتشف العروض","b"),
 ("ph4_menu_dessert","قوائم طعام ومطاعم","4:5","","#F5D58A","#1A0E05","حلويات","حلاوتك","هنا","تورتات وحلويات شرقي وغربي","شوف المنيو","b"),
 ("ph4_menu_grill","قوائم طعام ومطاعم","4:5","","#F5B83D","#1A0E05","مشويات","مشويات","على الفحم","أطباق مشاوي وطواجن للعيلة","اطلب الآن","b"),
 ("ph4_invite_wedding","دعوات ومناسبات","4:5","","#B8892B","#FFFFFF","دعوة زفاف","بكل الحب","ندعوكم لفرحنا","يوم الجمعة ٢٠ ديسمبر الساعة ٨ مساء","أكد الحضور","l"),
 ("ph4_prod_skincare","إعلانات المنتجات التجارية","1:1","","#B8892B","#FFFFFF","عناية بالبشرة","بشرة","بتنور","سيروم طبيعي بنتيجة من أول أسبوع","اطلبيه الآن","l"),
]
if __name__=="__main__":
    P.SPECS=[s for s in P.SPECS if os.path.exists(f"{P.ROOT}/photo/{s[0]}.webp")]
    json.dump(P.build("4",400),open(f"{P.ROOT}/catalog_photo4.json","w"),ensure_ascii=False,separators=(",",":"))
    print("wrote",len(P.SPECS))
