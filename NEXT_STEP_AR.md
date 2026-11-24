# الخطوة التالية لتشغيل FOKOF2

النسخة الحالية هي **ملفات مصدر Android معدّلة** وليست APK جاهزًا.

## الطريقة الأسهل من الموبايل: Google AI Studio

Google AI Studio أصبح يدعم بناء تطبيقات Android الأصلية بـ Kotlin وJetpack Compose، مع محاكي يعمل في السحابة من المتصفح، فلا يلزم تثبيت Android SDK على هاتفك أو جهازك. راجع [دليل Google AI Studio الرسمي](https://ai.google.dev/gemini-api/docs/aistudio-android).

1. نزّل ملف `FOKOF2-Android-1.2.0-2026-11-19.zip`.
2. افتح مشروع Android في Google AI Studio، أو أنشئ مشروعًا جديدًا واختر Android.
3. من مستكشف الملفات اضغط زر `+` واستخدم `Upload Zip file` إن ظهر الخيار، ثم اختر ملف ZIP. هذه طريقة رفع أبلغ عنها مستخدمون في منتدى Google AI Developers؛ إن لم يظهر الخيار، لا ترفع ملفات عشوائية أو تستبدل مشروعك الأصلي.
4. الصق الأمر الموجود في `AI_STUDIO_BUILD_PROMPT_AR.md`، واطلب **Build Debug فقط**. لا تطلب نشرًا على Google Play.
5. راجع نتيجة البناء والاختبارات في المحاكي السحابي قبل تثبيت أي نسخة على هاتفك.

## بديل: Android Studio على كمبيوتر

1. فك ضغط الملف وافتح المجلد الذي يحتوي `settings.gradle.kts` في Android Studio.
2. جهّز JDK 21 وAndroid SDK Platform 36.1، ثم نفّذ **Sync Project with Gradle Files**.
3. شغّل **Unit Tests** وRobolectric، ثم أنشئ نسخة **Debug APK**.

## قبل النشر

- لا تنشئ Release APK قبل نجاح الاختبارات الفعلية.
- نشر التطبيق على Google Play يتطلب حساب مطوّر برسوم تسجيل؛ لذلك لا تستخدم خيار النشر إذا كان هدفك عدم دفع أي مبلغ.
- لا تشارك ملفات keystore أو كلمات مرور التوقيع.
- لا تعتمد على مزامنة المدفوعات بين الأجهزة؛ قاعدة البيانات الحالية محلية.
- لا تفعّل Gemini أو Firebase أو Firestore أو أي خدمة خارجية مدفوعة؛ التكاملات السحابية متوقفة في هذه النسخة.

## ملاحظة مهمة

تمت إضافة مشغلات تمهيدية لـ Gradle على Linux/macOS وWindows، مع بصمة SHA-256 رسمية لتوزيعة Gradle 9.3.1. هذه المشغلات ليست نسخة Gradle الرسمية التي تعتمد على `gradle-wrapper.jar`، لكنها تجهز التوزيعة وتنفذ أوامر Gradle عند توفر الإنترنت. ملف `debug.keystore` المرفق مخصص للتطوير فقط ولا يصلح لتوقيع إصدار Release. لم يتم تنفيذ بناء APK فعلي في بيئة هذه المحادثة لأن Android SDK غير مثبت والاتصال بتنزيلات Gradle/Google غير متاح.
