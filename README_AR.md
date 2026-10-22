# Manga MM — مشروع manga-999

مشروع **جديد ومستقل** من الصفر، مستوحى من الصور الأربع المرفوعة. ليس مجرد إعادة تسمية لتطبيق Manga XW القديم.

- اسم المستودع/المجلد: `manga-999`
- اسم التطبيق على الهاتف: **Manga MM**
- اسم الحزمة: `com.mangamm.reader`
- التقنية: Java Android Activity + متصفح WebView معزول + واجهة HTML/CSS/JavaScript محلية. **ليس دمجًا فعليًا لمحرك TachiyomiAZ**.
- متطلبات التطوير: JDK 17، Gradle 8.9، Android Gradle Plugin 8.7.3، Android SDK 35.
- أقل إصدار Android: 8.0 (API 26).

## الشاشات والوظائف الحالية

1. الرئيسية: صور أغلفة توضيحية، تقدم القراءة، المصادر، مواقع حديثة، مكتبة، شريط تنقل سفلي.
2. المتصفح: تحميل مواقع HTTPS/HTTP الحقيقية في WebView منفصل، رجوع/تقدم، عنوان URL، علامة مرجعية، تنزيل ملفات عبر DownloadManager، زر إدخال الصور إلى القارئ.
3. المصادر: إضافة مصادر ويب شخصية، اكتشاف تكرار الروابط، فتح المصدر، حذف المصدر الشخصي.
4. المكتبة: إضافة مانجا برابط، غلاف خارجي اختياري، مجلدات، حالات Reading/Completed، مفضلة، عرض قائمة أو شبكة، بحث وحذف مع تأكيد.
5. التفاصيل: غلاف، وصف توضيحي، فصول تجريبية، متابعة وحفظ ومشاركة، أزرار الانتقال إلى المواقع.
6. القارئ: قارئ صفحات افتراضية دون إنترنت، بالإضافة إلى **محاولة** التقاط روابط صور الفصل من صفحة ويب فعلية وعرضها أونلاين؛ تمرير عمودي أو صفحة مفردة، حجم الصور صغير/متوسط/كبير، سطوع، تكبير باللمس وإيماءة الإصبعين، واجهة إخفاء.
7. الإعدادات: خمسة أنماط ألوان، مانع بسيط واختياري لبعض نطاقات الإعلان، إعدادات القارئ، عرض حجم البيانات المحلية، تصدير واستعادة بيانات التطبيق في ZIP يحتوي ملف JSON للحالة (ويدعم استيراد JSON القديم).
8. الحركة والأصول: 70 أيقونة SVG منفصلة و37 حركة Lottie JSON أصيلة، مع تشغيل مؤشر متحرك محليًا، وانتقالات CSS.

**مهم:** مصادر المانجا تعتمد على مواقع خارجية تتغير روابطها وآلياتها. استخراج صور الفصل قد لا يعمل في مواقع تستخدم حماية أو تحميلًا خاصًا. الترجمة الظاهرة في القارئ **مثال بصري فقط**؛ لا يوجد حتى الآن OCR حقيقي أو نموذج ترجمة عربي يعمل دون إنترنت أو محرك TachiyomiAZ كامل. كما أن روابط الصور المستوردة تحتاج الإنترنت؛ ملف ZIP يحفظ بيانات المكتبة والمصادر والتاريخ والمجلدات فقط ولا يضم الصور المحملة. لا يوجد نظام تنزيل فصول كاملة ضمن مكتبة خاصة ولا استعادة للمرفقات المحملة. الصور التجريبية مقصوصة من مراجع التصميم التي رفعها المستخدم وليست مكتبة مانجا مرخصة للنشر.

## البناء عبر GitHub — الطريقة الأسهل من Termux

1. فك ضغط ملف `manga-999-ready.zip` على الهاتف داخل مجلد يمكن لـ Termux الوصول إليه، ثم انتقل إلى مجلد `manga-999`.
2. افتح GitHub وأنشئ **مستودعًا فارغًا** باسم `manga-999`، بدون README أو ملفات ابتدائية.
3. في Termux (بعد إعداد git/الاتصال بـ GitHub):

```bash
pkg install git unzip -y
cd /storage/emulated/0/Download/manga-99
# يُسمح بـ termux-setup-storage إذا ظهرت مشكلة صلاحية الوصول

git init -b main
git add .
git commit -m "Manga MM first rebuild"
git remote add origin https://github.com/YOUR_USERNAME/manga-999.git
git push -u origin main
```

4. GitHub > `manga-999` > Actions > **Build Manga MM APK** > آخر تشغيل ناجح > Artifacts > **Manga-MM-debug-apk**.
5. الملف الناتج `app-debug.apk` هو نسخة اختبار Debug؛ قم بتثبيته على هاتفك بعد مراجعة صلاحيات Android.

بديل عبر GitHub CLI (إذا كان متاحًا): بعد `gh auth login` يمكنك إنشاء المستودع بأمر `gh repo create manga-999 --private --source=. --remote=origin --push`.

## البناء داخل Android Studio

- افتح المجلد `manga-999` كـ Gradle Project.
- اختر Gradle JDK 17. Gradle 8.9 مطلوب مع AGP 8.7.
- إن لم يتعرف Android Studio على Gradle wrapper (لم تُضمّن `gradle-wrapper.jar` الرسمية)، اضبط التوزيعة Gradle 8.9 من إعدادات Gradle أو شغّل `./gradlew :app:assembleDebug` على Linux/macOS مع توفر الإنترنت.
- الملف النهائي في `app/build/outputs/apk/debug/app-debug.apk`.
- لا يوجد SDK أو أداة Gradle في بيئة إنشاء هذه الحزمة، لذلك **لم يتم إجراء بناء APK فعلي داخل هذه الجلسة**. تم اختبار ملفات JavaScript تفاعليًا في متصفح Chromium فقط.

## مجلدات مهمة

```text
manga-999/
  app/src/main/java/com/mangamm/reader/MainActivity.java
  app/src/main/assets/index.html
  app/src/main/assets/app.js
  app/src/main/assets/style.css
  app/src/main/assets/lottie-mini.js
  app/src/main/assets/icons/       (70 SVG)
  app/src/main/assets/animations/  (37 Lottie JSON)
  app/src/main/assets/art/         (مراجع بصرية تجريبية)
  .github/workflows/android.yml
  preview/                        (صور اختبار الواجهة)
  scripts/                        (إنشاء/فحص/جلب الأصول الاختيارية)
  design-tokens.json
  RESOURCE_SOURCES.md
```

لا ترفع `local.properties` أو مفاتيح/رموز الدخول إلى GitHub. حزمة الأيقونات والحركات هنا مستقلة ولا تحتاج API Key، والمتصفح لا يحصل على صلاحيات JavaScript الخاصة بالتطبيق.

## نشر المشروع عبر Termux إلى GitHub باسم manga-999

1. حمّل الملف `manga-999-ready.zip` إلى `Download` بهاتفك.
2. في Termux شغّل:

```bash
pkg install git gh unzip -y && termux-setup-storage && unzip -oq ~/storage/downloads/manga-999-ready.zip -d ~ && bash ~/manga-999/publish-termux.sh
```

3. أول مرة، سيطلب GitHub CLI تسجيل الدخول إلى GitHub. أكمل خطوة التفويض على المتصفح.
4. السكربت يتحقق من أنك داخل حساب `Alqasimx` وينشئ مستودعًا خاصًا اسمه `manga-999` إذا لم يكن موجودًا، ثم يرفع المشروع. إذا كان موجودًا، يدفع التغييرات بدون قوة دفع.

**تحذير:** لا تشارك رموز GitHub الشخصية. لا تغلق Termux أثناء الرفع، ولا تحذف فرعًا موجودًا أو تستخدم `--force` لحل أي تعارض.
