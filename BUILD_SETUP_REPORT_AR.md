# تقرير تجهيز أدوات بناء FOKOF2 Android 1.2.0

## الإصلاحات المضافة

1. إضافة `gradlew` لـ Linux/macOS و`gradlew.bat` مع `gradlew.ps1` لـ Windows.
2. إضافة `distributionSha256Sum` لتثبيت والتحقق من Gradle 9.3.1 قبل التشغيل. البصمة المستخدمة مأخوذة من مرجع Gradle الرسمي.
3. إضافة `debug.keystore` لأن إعداد التطبيق الحالي يحدد هذا المسار صراحةً عند بناء Debug.
4. إضافة شرح واضح لأوامر بناء Debug وتحذير من استخدام مفتاح التطوير مع Release.
5. الحفاظ على `applicationId` و`namespace` ونسخة التطبيق 1.2.0 وإعدادات Room الحالية دون تغيير.

## التوافق الذي تم التحقق منه من وثائق Android الرسمية

- AGP 9.1.1 يتطلب Gradle 9.3.1 على الأقل، وهو الإصدار المحدد في `gradle-wrapper.properties`.
- AGP 9.1.1 يتوافق مع Kotlin Gradle Plugin 2.2.10، وهو الإصدار المحدد في `gradle/libs.versions.toml`.
- AGP 9.1.1 يدعم API 37.0 وما دونه، لذلك API 36.1 المطلوب في إعدادات التطبيق ضمن النطاق المدعوم.

## ما تم اختباره داخل بيئة العمل الحالية

- فحوصات مصدر المشروع: 21/21 ناجحة.
- اختبارات مستقلة لأداة كلمات المرور `SecurityUtils`: 10/10 ناجحة باستخدام Kotlin/JVM.
- اختبارات SQL منفصلة لترحيل Room 11→12 و12→13: 2/2 ناجحة على SQLite.
- فحص صياغة سكربت Linux/macOS: `sh -n gradlew` ناجح.
- تجربة تمرير الأوامر إلى Gradle بعد وجود نسخة مخبأة محليًا: ناجحة.
- فحص ملف المفتاح التطويري باستخدام `keytool -list`: ناجح.
- فحص سلامة ملف ZIP النهائي وبصمته: أُجري بعد إنشاء الأرشيف.

## ما لم يمكن تنفيذه هنا

لم يتوفر Gradle أو Android SDK، كما تعذر الوصول إلى خوادم تنزيل Gradle وAndroid بسبب عدم توفر DNS/اتصال شبكة في بيئة التنفيذ. لذلك لم يتم تشغيل `gradlew testDebugUnitTest assembleDebug` ولم يتم إنشاء APK. يلزم تنفيذ هذا الأمر في Google AI Studio أو Android Studio/بيئة CI فيها JDK وAndroid SDK واتصال إنترنت.

## أمر الاختبار النهائي

Linux/macOS:

```bash
./gradlew --no-daemon clean testDebugUnitTest assembleDebug
```

Windows:

```bat
gradlew.bat --no-daemon clean testDebugUnitTest assembleDebug
```

المخرج المتوقع بعد نجاح البناء: `app/build/outputs/apk/debug/app-debug.apk`.

> `debug.keystore` مخصص للتطوير فقط، ولا يستخدم في توقيع إصدار Release أو نشر التطبيق.
