#!/usr/bin/env python3
"""duck.ai (GPT Image) photo templates, batch 6 (additive, ph6_* ids). photo_batch4.py json -> catalog_photo6.json for specs whose image exists."""
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
LIGHT={"ph6_biz_team","ph6_edu_online","ph6_edu_kids","ph6_fit_water"}
P.SPECS=[
 ("ph6_car_luxury","سيارات","1:1","","#F5D58A","#05070A","سيارات فاخرة","الفخامة","لها عنوان","أحدث الموديلات بأقساط مرنة","احجز تجربة قيادة","b"),
 ("ph6_car_wash","سيارات","4:5","","#7FD6FF","#04161A","غسيل وتلميع","عربيتك","زي الجديدة","غسيل خارجي وداخلي وتلميع","احجز دورك","b"),
 ("ph6_biz_team","أعمال وتسويق","4:5","","#B8892B","#FFFFFF","حلول الشركات","فريقك","نجاحنا","إدارة وتسويق ومتابعة شهرية","كلمنا الآن","l"),
 ("ph6_biz_handshake","أعمال وتسويق","1:1","","#F5D58A","#2A1A08","شراكات","نبدأ","بثقة","عقود واضحة وخدمة موثوقة","اتفق معانا","b"),
 ("ph6_event_conf","مناسبات واحتفالات","4:5","","#9FD8FF","#04101E","مؤتمرات وفعاليات","مؤتمرك","القادم عندنا","تنظيم كامل وقاعات مجهزة","احجز الفعالية","b"),
 ("ph6_event_newyear","مناسبات واحتفالات","1:1","","#F5D58A","#000000","رأس السنة","سنة جديدة","وفرحة جديدة","حفل ليلة رأس السنة","احجز طاولتك","b"),
 ("ph6_edu_online","تعليم وتدريب","1:1","","#B8892B","#FFFFFF","تعلم أونلاين","اتعلم","من بيتك","كورسات مسجلة ومباشرة","سجل الآن","l"),
 ("ph6_edu_kids","تعليم وتدريب","4:5","","#B8892B","#FFFFFF","حضانة وأنشطة","طفلك","يتعلم ويلعب","أنشطة وقصص ورسم","سجل طفلك","l"),
 ("ph6_tech_app","تكنولوجيا","1:1","","#FFFFFF","#0A0A3A","تطبيقات موبايل","تطبيقك","في جيبك","تصميم وبرمجة بأحدث التقنيات","اطلب عرض سعر","b"),
 ("ph6_fit_run","صحة ولياقة","4:5","","#FFD9A3","#2A1204","نادي جري","اجري","ورا هدفك","تجمع جري كل صباح جمعة","انضم إلينا","b"),
 ("ph6_fit_water","صحة ولياقة","1:1","","#B8892B","#FFFFFF","نمط صحي","اشرب","وانتعش","نصائح يومية لحياة أصح","تابعنا","l"),
 ("ph6_sale_ramadan","عروض وتخفيضات","4:5","","#F5D58A","#031A12","عروض رمضان","رمضان","بأحلى العروض","خصومات على مستلزمات الشهر","تسوق الآن","b"),
]
if __name__=="__main__":
    P.SPECS=[s for s in P.SPECS if os.path.exists(f"{P.ROOT}/photo/{s[0]}.webp")]
    L=P.build("6",600)
    for t in L:
        if t["id"] in LIGHT:
            for x in t["e"]:
                if x[0]=="T" and x[6]=="#B8892B": x[6]="#8A6414"
            t["e"].insert(0,["S","RECTANGLE",180.0,{"1:1":265.0,"4:5":355.0}[t["asp"]],360,190,"#FFFFFF",0.82,0,"grad:b"])
    json.dump(L,open(f"{P.ROOT}/catalog_photo6.json","w"),ensure_ascii=False,separators=(",",":"))
    print("wrote",len(P.SPECS))
