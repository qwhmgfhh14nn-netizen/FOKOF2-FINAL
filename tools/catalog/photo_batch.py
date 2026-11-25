#!/usr/bin/env python3
"""Photo-template batch builder. Usage: photo_batch.py gen <batch>  (generates images, needs /tmp/gen_token)
                                         photo_batch.py json <batch> (writes catalog_photo<N>.json)"""
import json, os, subprocess, sys, urllib.parse
ROOT = os.path.expanduser("~/FOKOF2-FINAL/app/src/main/assets/templates")
B = "https://fokof2-api.qwhmgfhh14nn.workers.dev/gen2"
NT = ", professional photography, natural light, high detail, no text, no logos, no watermark, no people"
SIZES = {"1:1": (1024, 1024, 360), "4:5": (896, 1120, 450), "9:16": (768, 1344, 640)}
# id, cat, asp, prompt, accent, scrim, kicker, line1, line2, sub, cta, variant
SPECS = [
 ("ph_car_sport","سيارات","4:5","low angle of a glossy dark grey sports car on a wet city street at dusk, cinematic","#F5B83D","#05070A","سيارات مستعملة بحالة الزيرو","اختار عربيتك","وامشي بيها النهارده","فحص كامل وضمان ٦ شهور","احجز تجربة قيادة","b"),
 ("ph_car_suv","سيارات","1:1","white modern SUV parked on a desert road at golden hour, wide shot","#F5B83D","#05070A","معرض السيارات","رحلتك الجاية","بتبدأ من هنا",  "أقساط مريحة بدون مقدم","اعرف أكتر","b"),
 ("ph_car_service","سيارات","9:16","clean modern car repair garage interior with tools on wall and a lifted car, bright, organized","#F5B83D","#05070A","مركز صيانة","عربيتك","في أيد أمينة","صيانة دورية وقطع غيار أصلية","احجز ميعاد الصيانة","t"),
 ("ph_social_desk","سوشيال ميديا","1:1","flat lay of a creative desk with smartphone, notebook, coffee and small plant on light wood, soft light","#F5B83D","#1A1033","محتوى يلفت النظر","خلي بيدجك","يتكلم عنك","خطة محتوى كاملة لمدة شهر","ابدأ دلوقتي","b"),
 ("ph_social_phone","سوشيال ميديا","4:5","smartphone on a stand with a ring light in a bright studio, blurred background, pastel tones","#FFD9A3","#1A1033","ستوديو تصوير","صوّر محتواك","بشكل احترافي","تصوير وتعديل وإخراج","احجز جلسة","b"),
 ("ph_social_studio","سوشيال ميديا","9:16","podcast studio with a microphone and headphones on a table, warm moody light, no people","#F5B83D","#1A1033","بودكاست","صوتك","يستاهل يوصل","تسجيل ومونتاج بجودة عالية","احجز الاستوديو","t"),
 ("ph_biz_office","أعمال وتسويق","4:5","bright modern office with a large wooden meeting table and city view, minimal, ivory tones","#E8C766","#0A1A2A","استشارات أعمال","كبّر شغلك","بخطوات مدروسة","خطط تسويق تناسب ميزانيتك","احجز استشارة","b"),
 ("ph_biz_desk","أعمال وتسويق","1:1","overhead view of a clean executive desk with laptop, notebook, pen and a cup of tea, soft shadows","#E8C766","#0A1A2A","خدمات الشركات","نظّم شغلك","وارتاح","حلول إدارة وحسابات متكاملة","كلمنا","b"),
 ("ph_biz_skyline","أعمال وتسويق","9:16","glass skyscrapers seen from below at sunrise, deep blue sky, minimal","#E8C766","#0A1A2A","نمو وتوسع","ابني براندك","من الأول","هوية وموقع وإعلانات في مكان واحد","ابدأ مشروعك","t"),
 ("ph_event_wedding","مناسبات واحتفالات","4:5","elegant wedding hall set with round tables, white flowers and warm chandeliers, no guests","#F5D58A","#2A1204","قاعات أفراح","ليلة العمر","تستاهل الأحلى","تجهيز كامل وتنسيق زهور","احجز القاعة","b"),
 ("ph_event_party","مناسبات واحتفالات","1:1","birthday party table with a cake, balloons and gold decorations in soft light, no people","#F5D58A","#2A1204","حفلات أعياد الميلاد","فرحتهم","علينا","تورتة وبلالين وتنظيم كامل","احجز حفلتك","b"),
 ("ph_event_flowers","مناسبات واحتفالات","9:16","large bouquet of white and blush roses on a marble table, soft window light","#F5D58A","#2A1204","كوش وورد","تفاصيل","تفرق في يومك","تنسيق ورد لكل المناسبات","اطلب تنسيقك","t"),
 ("ph_edu_class","تعليم وتدريب","4:5","bright empty classroom with wooden desks, big windows and a green chalkboard, warm sunlight","#F5B83D","#0A1A2A","مركز تعليمي","ابدأ سنتك","بقوة","مجموعات صغيرة ومدرسين متميزين","سجل دلوقتي","b"),
 ("ph_edu_books","تعليم وتدريب","1:1","stack of books with an open notebook, pencil and glasses on a wooden desk, soft morning light","#F5B83D","#0A1A2A","كورسات ودروس","المذاكرة","أسهل مع الخطة","دروس خصوصية وأونلاين","احجز مكانك","b"),
 ("ph_edu_lab","تعليم وتدريب","9:16","modern school science lab with glassware and microscopes on clean benches, bright","#F5B83D","#0A1A2A","دورات علمية","اكتشف","موهبتك","دورات تدريبية للأطفال والشباب","اشترك الآن","t"),
 ("ph_tech_laptop","تكنولوجيا","4:5","sleek laptop and smartphone on a dark desk with soft blue glow, minimal, product photography","#7FD6FF","#06121E","حلول تقنية","موقعك","جاهز في أيام","تصميم مواقع وتطبيقات","اطلب عرض سعر","b"),
 ("ph_tech_chip","تكنولوجيا","1:1","macro of a circuit board with glowing blue lights, shallow depth of field","#7FD6FF","#06121E","تقنية المعلومات","شبكتك","آمنة وسريعة","دعم فني وصيانة أجهزة","كلمنا","b"),
 ("ph_tech_server","تكنولوجيا","9:16","modern data center corridor with glowing blue lights, symmetrical, clean","#7FD6FF","#06121E","استضافة وسيرفرات","شغلك","شغال ٢٤ ساعة","استضافة سريعة ودعم دائم","اشترك الآن","t"),
 ("ph_fit_gym","صحة ولياقة","4:5","modern gym interior with dumbbell rack and rubber floor, dramatic side light, no people","#F5B83D","#05070A","جيم","جسمك","يستاهل الاهتمام","اشتراك شهري بمدربين معتمدين","سجل دلوقتي","b"),
 ("ph_fit_yoga","صحة ولياقة","1:1","yoga mat, water bottle and towel in a bright minimal studio with large windows, calm","#E8C766","#10261A","يوجا","اهدى","وارتاح","حصص يومية للمبتدئين","جرّب حصة مجانية","b"),
 ("ph_fit_food","صحة ولياقة","9:16","colorful healthy meal prep bowls with vegetables and grilled chicken, overhead, bright","#F5B83D","#10261A","نظام غذائي","كُل صح","وعيش أحسن","وجبات صحية جاهزة كل يوم","اطلب وجباتك","t"),
 ("ph_sale_bags","عروض وتخفيضات","4:5","colorful shopping bags and gift boxes on a pastel pink background, studio shot, festive","#FFFFFF","#3A1220","عروض الموسم","خصم ٥٠","على كل حاجة","لفترة محدودة بس","تسوق الآن","b"),
 ("ph_sale_gift","عروض وتخفيضات","1:1","gift box with a gold ribbon on a soft ivory background with confetti, studio","#F5D58A","#2A1204","عرض خاص","اشتري ١","واخد ١ هدية","العرض ساري لحد آخر الأسبوع","اطلب العرض","b"),
 ("ph_sale_cart","عروض وتخفيضات","9:16","red shopping cart on a clean white studio background with colorful boxes, bright, minimal","#FFFFFF","#2A0A0A","أكبر تخفيضات","ماتفوتش","الفرصة","خصومات تصل لـ ٧٠ بالمية","تسوق دلوقتي","t"),
]
def layout(asp, acc, scr, k, l1, l2, sub, cta, var):
    H = SIZES[asp][2]; W = 360
    e = []
    def T(t,y,s,b,c,o=1.0): e.append(["T",t,180,y,s,b,c,o])
    if var == "t" and asp == "9:16":
        e.append(["S","RECTANGLE",180.0,140.0,W,280,scr,0.93,0,"grad:t"])
        e.append(["S","RECTANGLE",180.0,H-120.0,W,240,scr,0.9,0,"grad:b"])
        T(k,56,15,1,acc); T(l1,108,36,1,"#FFFFFF"); T(l2,156,42,1,acc)
        T(sub,H-98,14,0,"#FFFFFF",0.95); T(cta,H-58,16,1,acc)
        e.append(["S","RECTANGLE",180,H-44,60,2,acc,1.0,0,""])
    else:
        h = 300
        e.append(["S","RECTANGLE",180.0,H-h/2,W,h,scr,0.95,0,"grad:b"])
        T(k,H-150,15,1,acc); T(l1,H-112,36,1,"#FFFFFF"); T(l2,H-68,36,1,acc)
        T(sub,H-40,13,0,"#FFFFFF",0.95)
        # cta below sub would crowd small canvases: put cta on same block bottom
        e[-1][3]=H-40
        T(cta,H-18,15,1,acc); e.append(["S","RECTANGLE",180,H-6,60,2,acc,1.0,0,""])
    return e
def build(batch, start_fp):
    out=[]
    for i,s in enumerate(SPECS):
        sid,cat,asp,pr,acc,scr,k,l1,l2,sub,cta,var=s
        out.append({"id":sid,"cat":cat,"asp":asp,"tAr":(l1+" "+l2).strip(),"tEn":sid[3:].replace('_',' ').title()+" Photo","bs":"#1A1A1A","be":"#000000",
          "fp":[start_fp+i]*4,"img":"photo/%s.webp"%sid,"e":layout(asp,acc,scr,k,l1,l2,sub,cta,var)})
    return out
def gen(only=None):
    tok=open("/tmp/gen_token").read().strip()
    for s in SPECS:
        sid,asp,pr=s[0],s[2],s[3]
        if only and sid not in only: continue
        dst=f"{ROOT}/photo/{sid}.webp"
        if os.path.exists(dst): continue
        w,h,_=SIZES[asp]
        raw=f"/tmp/gen_{sid}.jpg"
        for t in range(3):
            r=subprocess.run(["curl","-s","-G",B,"--data-urlencode","t="+tok,"--data-urlencode","p="+pr+NT,"--data-urlencode",f"w={w}","--data-urlencode",f"h={h}","--data-urlencode","m=k","-o",raw,"-w","%{http_code}"],capture_output=True,text=True)
            if r.stdout.strip()=="200" and os.path.getsize(raw)>20000: break
        else:
            print("FAIL",sid,r.stdout); continue
        subprocess.run(["convert",raw,"-resize","720x","-quality","58","-define","webp:method=6",dst],check=True)
        print("ok",sid,os.path.getsize(dst))
if __name__=="__main__":
    if sys.argv[1]=="gen": gen(sys.argv[2:] or None)
    else:
        n=sys.argv[2]; 
        json.dump(build(n,112),open(f"{ROOT}/catalog_photo{n}.json","w"),ensure_ascii=False,separators=(",",":"))
        print("wrote",len(SPECS))
