#!/usr/bin/env python3
"""Gemini-art photo templates for the defaults categories. Images are made interactively (no API), saved as photo/<id>.webp.
Usage: photo_batch3.py json  -> writes catalog_photo3.json for specs whose image exists."""
import json, os, sys
sys.argv=[sys.argv[0]]+sys.argv[1:]
import photo_batch as P
D="Create an image: "
# id, cat, asp, prompt, accent, scrim, kicker, l1, l2, sub, cta, variant
P.SPECS=[
 ("ph3_prod_perfume","إعلانات المنتجات التجارية","1:1","a luxury perfume bottle with gold cap on cream marble with soft shadows, ivory and gold palette, studio product photo, empty space at the bottom","#B8892B","#FFFFFF","عطور فاخرة","رائحة تفضل","في الذاكرة","تشكيلة جديدة بأسعار مناسبة","اطلب الآن","l"),
 ("ph3_menu_coffee","قوائم طعام ومطاعم","4:5","a cup of latte art coffee with croissant on a dark wooden table, warm cafe light, top-down slight angle, empty space at the bottom","#F5D58A","#1A0E05","كافيه","قهوتك","على أصولها","مشروبات ساخنة وباردة ومخبوزات طازجة","شوف المنيو","b"),
 ("ph3_invite_gold","دعوات ومناسبات","4:5","elegant ivory and gold invitation backdrop with soft gold foil floral branches and subtle sparkles, minimal, empty center and bottom","#B8892B","#FFFFFF","دعوة خاصة","يسعدنا","حضورك معانا","ليلة لا تتكرر في أجمل الأجواء","أكد الحضور","l"),
 ("ph3_social_gold","منشورات السوشيال ميديا","1:1","flat lay of gold stationery, pen, small plant and notebook on ivory linen, soft daylight, elegant minimal, empty space at the bottom","#B8892B","#FFFFFF","أفكار ملهمة","محتوى","بيبيع لوحده","خطط شهرية جاهزة لصفحتك","ابدأ دلوقتي","l"),
 ("ph3_banner_store","بانرات ومطبوعات","9:16","elegant boutique storefront interior with soft warm lights, ivory walls and gold accents, clean, empty space top and bottom","#B8892B","#FFFFFF","افتتاح جديد","محلنا","اتفتح","خصومات الافتتاح لفترة محدودة","زورنا النهارده","l"),
 ("ph3_sale_gold","عروض وتخفيضات","1:1","scattered gold and white confetti and ribbons on a soft ivory background with a few small gift boxes at the top, studio, empty bottom half","#B8892B","#FFFFFF","عرض النهارده","خصم","لحد ٣٠ في المية","على مجموعة مختارة","تسوق الآن","l"),
]
if __name__=="__main__":
    P.SPECS=[s for s in P.SPECS if os.path.exists(f"{P.ROOT}/photo/{s[0]}.webp")]
    json.dump(P.build("3",300),open(f"{P.ROOT}/catalog_photo3.json","w"),ensure_ascii=False,separators=(",",":"))
    print("wrote",len(P.SPECS))
