# سجل التغييرات والإصلاحات (CHANGELOG)

## [1.0.2] - 2026-10-03

### إصلاح خطأ Setup Android SDK برمز خروج 127 في GitHub Actions
- **تثبيت بيئة التشغيل**: تحديد بيئة `ubuntu-24.04` بدقة في `runs-on`.
- **ترقية إعداد Java**: استخدام `actions/setup-java@v5` مع `java-version: '21'` وإصدار `temurin`.
- **تهيئة أدوات Android SDK**: إضافة `android-actions/setup-android@v3` لتهيئة مسار أدوات أندرويد و`$ANDROID_HOME` وإضافة مسار `cmdline-tools` إلى `$PATH` قبل أي استدعاء لـ `sdkmanager`.
- **التحقق وتثبيت الحزم الرسمية**:
  - فحص توفر الأداة باستخدام `command -v sdkmanager`.
  - قبول التراخيص رسمياً عبر `yes | sdkmanager --licenses`.
  - تثبيت الحزم المطلوبة لمطابقة `compileSdk 36.1`: `platforms;android-36` و `platforms;android-36.1` و `build-tools;36.0.0` و `platform-tools`.
- **تحديث إعداد Gradle**:
  - اعتماد `gradle/actions/setup-gradle@v4` مع تحديد إصدار `gradle-version: '9.3.1'`.
  - استخدام أمر Gradle المثبت مباشرة: `gradle :app:assembleDebug --stacktrace --no-daemon` دون إعادة توليد الـ wrapper أثناء التجميع.
- **الحفاظ على مفتاح debug.keystore**:
  - فحص وجود المفتاح وتوليده تلقائياً بالمواصفات المطابقة لـ `signingConfigs` (`androiddebugkey`/`android`).
  - تخزين المفتاح مؤقتاً عبر `actions/cache@v4` لمنع اختلاف التوقيع بين عمليات البناء.
- **رفع حزمة APK والنشر المباشر**:
  - رفع ملف الـ APK الناتج عبر `actions/upload-artifact@v4` باسم `yemen-4g-hashem-debug-apk` مع `if-no-files-found: error`.
  - إضافة مهمة نشر تلقائي لإصدارات GitHub Releases عبر `softprops/action-gh-release@v2` مع منح صلاحية `contents: write` لإنشاء رابط تحميل مباشر عند الدفع إلى `main`.
- **المصداقية والشفافية**:
  - لا توجد أي أكواد كابتشا وهمية أو أرصدة مصطنعة؛ التطبيق يربط المشترك مباشرة ببوابة المؤسسة العامة للاتصالات الرسمية (`https://svc.ptc.gov.ye/4g/`) لتظهر الكابتشا الحقيقية والرصيد الفعلي من سيرفرات الاتصالات.
  - الحفاظ على كافة حقوق البرمجة والتصميم باسم **«هاشم القديمي»**.
