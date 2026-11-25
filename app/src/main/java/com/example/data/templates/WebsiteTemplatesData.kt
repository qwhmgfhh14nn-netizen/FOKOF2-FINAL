package com.example.data.templates

import com.example.data.model.WebsiteFeatureToggles
import com.example.data.model.WebsiteItem
import com.example.data.model.WebsiteTemplate

object WebsiteTemplatesData {

    fun getDefaultTemplates(): List<WebsiteTemplate> {
        return listOf(
            // 1. Restaurant & Cafe
            WebsiteTemplate(
                id = "site_tpl_restaurant",
                businessType = "RESTAURANT",
                businessTypeNameAr = "مطاعم وكافيهات",
                titleAr = "مطعم وكافيه جريل وبرجر الملكي",
                titleEn = "Royal Burger & Grill House",
                taglineAr = "أشهى المشويات والبرجر على الفحم بأجود المكونات الطازجة",
                descriptionAr = "قالب موقع متكامل للمطاعم والكافيهات يشمل قائمة الطعام بالأسعار، إمكانية الطلب الفوري عبر واتساب، وخريطة الوصول.",
                primaryColorHex = "#E65100", // Warm Orange
                secondaryColorHex = "#FFB300",
                darkBgHex = "#120B05",
                priceEgp = 0,
                isPremium = false,
                isPublished = true,
                featureToggles = WebsiteFeatureToggles(
                    featureWhatsappOrders = true,
                    featureBookingSystem = true,
                    featureProductCatalog = true,
                    featureCustomerReviews = true,
                    featureFaqSection = true,
                    featureGoogleMaps = true,
                    featureDirectCalling = true
                ),
                sampleHeadline = "طعم المشويات الأصيل على الفحم يجمعنا",
                sampleSubheadline = "توصيل سريع ساخن لجميع المناطق على مدار 24 ساعة",
                sampleAbout = "مطعم وكافيه يقدم ألذ الساندوتشات والوجبات العائلية والمشويات الطازجة يومياً مع صلصاتنا السرية الخاصة منذ عام 2018.",
                sampleAddress = "شارع النصر، المعادي الجديدة، القاهرة",
                sampleWorkingHours = "يومياً من 11:00 صباحاً حتى 2:00 بعد منتصف الليل",
                samplePhone = "01023456789",
                sampleWhatsapp = "201023456789",
                sampleItems = listOf(
                    WebsiteItem(nameAr = "برجر دبل بيف بلاك أنجوس عالفحم", nameEn = "Double Angus Burger", priceEgp = 145, originalPriceEgp = 175, descriptionAr = "قطعتين لحم أنجوس 220 جم مع جبن شيدر ذائب وصوص سموكي وخيار مخلل", category = "البرجر", badgeAr = "الأكثر طلباً"),
                    WebsiteItem(nameAr = "وجبة ميكس جريل مشويات مشكلة", nameEn = "Mix Grill Platter", priceEgp = 260, originalPriceEgp = 295, descriptionAr = "شيش طاووق، كفتة لحم بلدي، وكباب مع أرز بسمتي بالمكسرات وسلطات", category = "المشويات", badgeAr = "عائلي"),
                    WebsiteItem(nameAr = "بيتزا رانش الدجاج المقرمش", nameEn = "Crispy Chicken Ranch Pizza", priceEgp = 160, descriptionAr = "عجينة إيطالية هشة، قطع كرسبي، صوص رانش غني وجبنة موتزاريلا طبيعية", category = "البيتزا"),
                    WebsiteItem(nameAr = "تشيز كيك كراميل لوتس فاخر", nameEn = "Lotus Caramel Cheesecake", priceEgp = 75, descriptionAr = "حلوى التشيز كيك الغنية بطبقة بسكويت اللوتس المقرمش وصوص الكراميل", category = "الحلويات والمشروبات")
                )
            ),

            // 2. Pharmacy & Healthcare
            WebsiteTemplate(
                id = "site_tpl_pharmacy",
                businessType = "PHARMACY",
                businessTypeNameAr = "صيدليات وعناية طبية",
                titleAr = "صيدلية ورعاية كير الطبية",
                titleEn = "Care Pharmacy & Wellness",
                taglineAr = "صحتك وسلامة عائلتك أولويتنا مع خدمة التوصيل الفوري 24/7",
                descriptionAr = "موقع صيدلية متطور يتيح إرسال صورة الروشتة عبر واتساب، تصفح مستحضرات العناية والتجميل، وأرقام الطوارئ السريعة.",
                primaryColorHex = "#00897B", // Medical Teal
                secondaryColorHex = "#26A69A",
                darkBgHex = "#041513",
                priceEgp = 0,
                isPremium = false,
                isPublished = true,
                featureToggles = WebsiteFeatureToggles(
                    featureWhatsappOrders = true,
                    featureBookingSystem = false,
                    featureProductCatalog = true,
                    featureCustomerReviews = true,
                    featureFaqSection = true,
                    featureGoogleMaps = true,
                    featureDirectCalling = true
                ),
                sampleHeadline = "صيدليتك العائلية على مدار الساعة بين يديك",
                sampleSubheadline = "أرسل صورة الروشتة أو طلبك عبر واتساب ونوصله لك أينما كنت",
                sampleAbout = "صيدلية متكاملة توفر الأدوية المعتمدة، الفيتامينات، وأحدث منتجات العناية بالبشرة والشعر ومستلزمات الأطفال مع نخبة من الصيادلة لخدمتكم.",
                sampleAddress = "ميدان الحجاز، مصر الجديدة، القاهرة",
                sampleWorkingHours = "مفتوح 24 ساعة طوال أيام الأسبوع",
                samplePhone = "01123456789",
                sampleWhatsapp = "201123456789",
                sampleItems = listOf(
                    WebsiteItem(nameAr = "سيروم فيتامين C للنضارة وتفتيح البشرة", nameEn = "Vitamin C Glowing Serum", priceEgp = 280, originalPriceEgp = 340, descriptionAr = "سيروم مركز مضاد للأكسدة يعيد النضارة ويوحد لون البشرة", category = "العناية بالبشرة", badgeAr = "عرض خاص"),
                    WebsiteItem(nameAr = "مكمل غذائي مالتي فيتامين أوميجا 3", nameEn = "Omega-3 & Multivitamins", priceEgp = 195, descriptionAr = "كبسولات لتعزيز المناعة وصحة القلب والنشاط الذهني اليومي", category = "الفيتامينات والمكملات"),
                    WebsiteItem(nameAr = "كريم مرطب طبيعي للجلد الجاف والحساس", nameEn = "Hydrating Moisturizer Cream", priceEgp = 140, descriptionAr = "ترطيب عميق وحماية تدوم 48 ساعة خالٍ من العطور والبارابين", category = "العناية اليومية"),
                    WebsiteItem(nameAr = "جهاز قياس الضغط الرقمي المنزلي الذكي", nameEn = "Digital Blood Pressure Monitor", priceEgp = 680, originalPriceEgp = 820, descriptionAr = "قياس دقيق وسهل الاستخدام مع ذاكرة قراءات لشخصين وضمان عامين", category = "الأجهزة الطبية")
                )
            ),

            // 3. Fashion & Boutique
            WebsiteTemplate(
                id = "site_tpl_fashion",
                businessType = "FASHION",
                businessTypeNameAr = "أزياء وموضة وملابس",
                titleAr = "متجر إليجانس للأزياء والموضة",
                titleEn = "Elegance Fashion Boutique",
                taglineAr = "أحدث صيحات الموضة والأزياء الراقية بتصاميم عصرية مميزة",
                descriptionAr = "واجهة متجر ملابس فاخرة لعرض التشكيلات الجديدة، جدول المقاسات، والطلب السريع عبر واتساب.",
                primaryColorHex = "#C2185B", // Fashion Berry Pink
                secondaryColorHex = "#D81B60",
                darkBgHex = "#18060F",
                priceEgp = 0,
                isPremium = false,
                isPublished = true,
                featureToggles = WebsiteFeatureToggles(
                    featureWhatsappOrders = true,
                    featureBookingSystem = false,
                    featureProductCatalog = true,
                    featureCustomerReviews = true,
                    featureFaqSection = true,
                    featureGoogleMaps = true,
                    featureDirectCalling = true
                ),
                sampleHeadline = "تألقي بإطلالة فريدة تعبر عن أناقتك",
                sampleSubheadline = "تشكيلة الموسم الجديد متوفرة الآن مع شحن مجاني للطلبات المميزة",
                sampleAbout = "بوتيك أزياء نسائي ورجالي يقدم أرقى الخامات والتصاميم المختارة بعناية لتناسب كل الأوقات والمناسبات الرسمية واليومية.",
                sampleAddress = "شارع عباس العقاد، مدينة نصر، القاهرة",
                sampleWorkingHours = "يومياً من 12:00 ظهراً إلى 11:30 مساءً",
                samplePhone = "01234567890",
                sampleWhatsapp = "201234567890",
                sampleItems = listOf(
                    WebsiteItem(nameAr = "فستان سهرة كلاسيكي مطرز بحزام ذهبي", nameEn = "Evening Satin Gown", priceEgp = 890, originalPriceEgp = 1150, descriptionAr = "قماش ستان فاخر مع تطريز يدوي ناعم ومقاسات تناسب الجميع", category = "فساتين سهرة", badgeAr = "حصري"),
                    WebsiteItem(nameAr = "بليزر كاجوال أوفر سايز عملي", nameEn = "Oversized Casual Blazer", priceEgp = 540, originalPriceEgp = 620, descriptionAr = "خامة كريب تركية مستوردة مناسبة للعمل والخروجات اليومية", category = "الملابس الكاجوال"),
                    WebsiteItem(nameAr = "بدلة رجالي إيطالي سليم فيت كاملة", nameEn = "Italian Slim Fit Suit", priceEgp = 1450, originalPriceEgp = 1800, descriptionAr = "قماش صوف إيطالي عالي الجودة مع جاكيت وبنطلون وقميص مجاناً", category = "أزياء رجالية", badgeAr = "خصم 20%"),
                    WebsiteItem(nameAr = "حقيبة يد جلد طبيعي هاند ميد", nameEn = "Handcrafted Leather Handbag", priceEgp = 420, descriptionAr = "جلد طبيعي 100% مع إكسسوارات ذهبية مقاومة لتغير اللون", category = "إكسسوارات وحقائب")
                )
            ),

            // 4. Clinic & Dental Center
            WebsiteTemplate(
                id = "site_tpl_clinic",
                businessType = "CLINIC",
                businessTypeNameAr = "عيادات ومراكز طبية وأسنان",
                titleAr = "مركز سمايل كلينك لطب وتجميل الأسنان",
                titleEn = "Smile Dental & Specialty Clinic",
                taglineAr = "ابتسامتك المثالية تبدأ هنا بأحدث التكنولوجيات العالمية بدون ألم",
                descriptionAr = "موقع متكامل للعيادات والمراكز التخصصية يتيح حجز المواعيد المباشرة، عرض الأطباء والخدمات، وشهادات المرضى.",
                primaryColorHex = "#1E88E5", // Clinical Blue
                secondaryColorHex = "#42A5F5",
                darkBgHex = "#071424",
                priceEgp = 0,
                isPremium = false,
                isPublished = true,
                featureToggles = WebsiteFeatureToggles(
                    featureWhatsappOrders = false,
                    featureBookingSystem = true,
                    featureProductCatalog = true,
                    featureCustomerReviews = true,
                    featureFaqSection = true,
                    featureGoogleMaps = true,
                    featureDirectCalling = true
                ),
                sampleHeadline = "ابتسامة ناصعة وثقة دائمة مع أفضل استشاريي الأسنان",
                sampleSubheadline = "أحدث أجهزة الليزر وزراعة وتجميل الأسنان بأعلى معايير التعقيم الأوروبية",
                sampleAbout = "مركز طبي متكامل يضم نخبة من أساتذة طب وجراحة وتجميل الأسنان، مجهز بأحدث وحدات التصوير ثلاثي الأبعاد وتقنيات الفينير السريع.",
                sampleAddress = "برج الأطباء، شارع جامعة الدول العربية، المهندسين، الجيزة",
                sampleWorkingHours = "السبت إلى الخميس: 1:00 ظهراً - 10:00 مساءً",
                samplePhone = "01098765432",
                sampleWhatsapp = "201098765432",
                sampleItems = listOf(
                    WebsiteItem(nameAr = "جلسة تبييض الأسنان بالليزر والزووم", nameEn = "Laser Teeth Whitening", priceEgp = 1200, originalPriceEgp = 1600, descriptionAr = "تبييض فوري لـ 8 درجات في جلسة واحدة مدتها 45 دقيقة بدون حساسية", category = "تجميل الأسنان", badgeAr = "الأكثر طلباً"),
                    WebsiteItem(nameAr = "تركيب فينير وهوليوود سمايل للسن الواحد", nameEn = "Porcelain Veneer Smile", priceEgp = 2100, originalPriceEgp = 2600, descriptionAr = "عدسات إيماكس الألمانية الفاخرة بضمان 10 سنوات وشكل طبيعي تماماً", category = "ابتسامة هوليوود"),
                    WebsiteItem(nameAr = "كشف شامل وأشعة بانوراما وتنظيف جير", nameEn = "Checkup & Cleaning", priceEgp = 350, descriptionAr = "فحص شامل بالميكروسكوب مع تنظيف الجير وتلميع الأسنان", category = "الخدمات العامة"),
                    WebsiteItem(nameAr = "زراعة الأسنان الفورية بتقنية الديجيتال", nameEn = "Immediate Dental Implant", priceEgp = 6500, originalPriceEgp = 8000, descriptionAr = "زراعة أسنان تيتانيوم ألمانية بدون جراحة تقليدية مع تاج دائم", category = "جراحة وزراعة الأسنان")
                )
            ),

            // 5. Real Estate & Development
            WebsiteTemplate(
                id = "site_tpl_realestate",
                businessType = "REAL_ESTATE",
                businessTypeNameAr = "عقارات واستثمار وتطوير",
                titleAr = "النخبة للتطوير العقاري والاستثمار",
                titleEn = "Elite City Real Estate & Living",
                taglineAr = "استثمر في مستقبلك مع أرقى المشروعات والوحدات السكنية والتجارية",
                descriptionAr = "موقع عقاري مميز لعرض المشروعات السكنية والتجارية، المساحات والأسعار، وأنظمة السداد وطلب المعاينات.",
                primaryColorHex = "#D4AF37", // Gold
                secondaryColorHex = "#F5D061",
                darkBgHex = "#12110D",
                priceEgp = 0,
                isPremium = false,
                isPublished = true,
                featureToggles = WebsiteFeatureToggles(
                    featureWhatsappOrders = false,
                    featureBookingSystem = true,
                    featureProductCatalog = true,
                    featureCustomerReviews = true,
                    featureFaqSection = true,
                    featureGoogleMaps = true,
                    featureDirectCalling = true
                ),
                sampleHeadline = "وحدتك الفاخرة بأميز مواقع العاصمة والتجمع والساحل",
                sampleSubheadline = "مقدم 10% فقط وتقسيط مريح يصل حتى 8 سنوات بدون فوائد",
                sampleAbout = "شركة تطوير عقاري رائدة تقدم حلولاً سكنية واستثمارية واستشارية موثوقة في أرقى المدن والمجتمعات العمرانية الجديدة بمصر.",
                sampleAddress = "التجمع الخامس، شارع التسعين الشمالي، القاهرة الجديدة",
                sampleWorkingHours = "السبت إلى الخميس: 10:00 صباحاً - 8:00 مساءً",
                samplePhone = "01001122334",
                sampleWhatsapp = "201001122334",
                sampleItems = listOf(
                    WebsiteItem(nameAr = "شقة 165م بفيو مفتوح على اللاجون والمساحات الخضراء", nameEn = "Luxury 165m Apartment", priceEgp = 3200000, originalPriceEgp = 3600000, descriptionAr = "3 غرف نوم، 2 حمام، ريسبشن واسع، تشطيب كامل ألترا سوبر لوكس", category = "وحدات سكنية", badgeAr = "استلام فوري"),
                    WebsiteItem(nameAr = "فيلا تاون هاوس 240م بحديقة خاصة 90م", nameEn = "Townhouse Villa 240m", priceEgp = 5900000, descriptionAr = "4 غرف نوم ماستر، غرفة مربية، روف خاص، كمبوند متكامل الخدمات والنوادي", category = "فيلات مستقلة"),
                    WebsiteItem(nameAr = "محل تجاري 55م بمول حيوي على محور رئيسي", nameEn = "Commercial Retail Shop", priceEgp = 2800000, descriptionAr = "عائد إيجاري مضمون 15% سنوياً بعقد تفويض بالإيجار", category = "تجاري وإداري", badgeAr = "عائد استثماري"),
                    WebsiteItem(nameAr = "شاليه 110م بالساحل الشمالي يرى البحر بالكامل", nameEn = "North Coast Beach Chalet", priceEgp = 2450000, originalPriceEgp = 2700000, descriptionAr = "غرفتين نوم، حمام سباحة أمام الوحدة، مفروش بالتكييفات", category = "مصايف وشاليهات")
                )
            ),

            // 6. Luxury Barbershop & Spa
            WebsiteTemplate(
                id = "site_tpl_salon",
                businessType = "SALON",
                businessTypeNameAr = "صالونات حلاقة وعناية وتجميل",
                titleAr = "صالون رويال باربر للعناية الرجالية",
                titleEn = "Royal Barbershop & Grooming Lounge",
                taglineAr = "تجربة عناية استثنائية للرجل العصري تجمع بين الفخامة والاحترافية",
                descriptionAr = "قالب موقع للصالونات ومراكز التجميل يتيح حجز المواعيد الفورية وعرض قوائم الباقات والخدمات.",
                primaryColorHex = "#8D6E63", // Rich Brown
                secondaryColorHex = "#A1887F",
                darkBgHex = "#161210",
                priceEgp = 0,
                isPremium = false,
                isPublished = true,
                featureToggles = WebsiteFeatureToggles(
                    featureWhatsappOrders = false,
                    featureBookingSystem = true,
                    featureProductCatalog = true,
                    featureCustomerReviews = true,
                    featureFaqSection = true,
                    featureGoogleMaps = true,
                    featureDirectCalling = true
                ),
                sampleHeadline = "أناقتك وجاذبيتك في أيدٍ خبيرة ومحترفة",
                sampleSubheadline = "جلسات قص وتصفيف شعر وعناية باللحية والبشرة بأعلى معايير النظافة الفردية",
                sampleAbout = "صالون رجالي راقٍ يقدم خدمات حلاقة كلاسيكية وعصرية، حمام مغربي، تنظيف بشرة بالبخار، وعلاجات بروتين الشعر بمنتجات إيطالية أصلية.",
                sampleAddress = "شارع مصطفى النحاس، المنطقة التاسعة، مدينة نصر",
                sampleWorkingHours = "يومياً من 1:00 ظهراً حتى 12:00 منتصف الليل",
                samplePhone = "01198761234",
                sampleWhatsapp = "201198761234",
                sampleItems = listOf(
                    WebsiteItem(nameAr = "باقة رويال الشاملة (شعر + ذقن + ماسك بشرة)", nameEn = "Royal VIP Grooming", priceEgp = 220, originalPriceEgp = 280, descriptionAr = "قصة شعر مميزة، تحديد ذقن بالموس الساخن، تنظيف بشرة بالهيدرا فيشل وسشوار", category = "الباقات الملكية", badgeAr = "الأكثر طلباً"),
                    WebsiteItem(nameAr = "جلسة تنظيف عميق للبشرة بالبخار وماسك الفحم", nameEn = "Deep Charcoal Facial", priceEgp = 140, descriptionAr = "إزالة الرؤوس السوداء والشوائب مع ترطيب وتغذية بالفيتامينات", category = "العناية بالبشرة"),
                    WebsiteItem(nameAr = "جلسة علاج وفرد الشعر بالبروتين البرازيلي الأصلي", nameEn = "Brazilian Protein Therapy", priceEgp = 480, originalPriceEgp = 600, descriptionAr = "فرد ولمعان طبيعي خالٍ تماماً من الفورمالين يدوم حتى 6 أشهر", category = "علاجات الشعر"),
                    WebsiteItem(nameAr = "باقة تجهيز العريس الملكية الكاملة", nameEn = "Groom Wedding Package", priceEgp = 950, originalPriceEgp = 1300, descriptionAr = "شعر، ذقن، حمام مغربي بالصابون التونسي، باديكير، مساج واسترخاء", category = "باقات العرسان", badgeAr = "VIP")
                )
            ),

            // 7. Motors & Automotive
            WebsiteTemplate(
                id = "site_tpl_motors",
                businessType = "MOTORS",
                businessTypeNameAr = "سيارات وصيانة وقطع غيار",
                titleAr = "مركز أوتو جير لصيانة وخدمات السيارات",
                titleEn = "AutoGear Motors & Service Center",
                taglineAr = "صيانة شاملة بأحدث أجهزة كشف الأعطال بالكمبيوتر وقطع غيار أصلية",
                descriptionAr = "موقع مخصص لخدمات السيارات ومعارض البيع ومراكز الصيانة مع حجز فحص وتواصل مباشر.",
                primaryColorHex = "#D32F2F", // Automotive Red
                secondaryColorHex = "#EF5350",
                darkBgHex = "#180A0A",
                priceEgp = 0,
                isPremium = false,
                isPublished = true,
                featureToggles = WebsiteFeatureToggles(
                    featureWhatsappOrders = true,
                    featureBookingSystem = true,
                    featureProductCatalog = true,
                    featureCustomerReviews = true,
                    featureFaqSection = true,
                    featureGoogleMaps = true,
                    featureDirectCalling = true
                ),
                sampleHeadline = "سيارتك في أمان تام مع فحص شامل وضمان معتمد",
                sampleSubheadline = "صيانة سريعة، تغيير زيوت، فحص كمبيوتر وتكييف بأيدي مهندسين متخصصين",
                sampleAbout = "مركز صيانة سيارات متكامل يقدم خدمات الصيانة الميكانيكية والكهربائية والعفشة والتكييف لكافة الموديلات اليابانية والكورية والأوروبية.",
                sampleAddress = "المنطقة الصناعية، جسر السويس، القاهرة",
                sampleWorkingHours = "السبت إلى الخميس: 9:00 صباحاً - 8:00 مساءً",
                samplePhone = "01287654321",
                sampleWhatsapp = "201287654321",
                sampleItems = listOf(
                    WebsiteItem(nameAr = "فحص شامل بالكمبيوتر لـ 45 نقطة مع تقرير مطبوع", nameEn = "Full Diagnostic Computer Scan", priceEgp = 250, originalPriceEgp = 350, descriptionAr = "فحص الموتور، الفتيس، الحساسات، والكهرباء بالكامل مع مسح الأخطاء", category = "الفحص والكهرباء", badgeAr = "ضروري"),
                    WebsiteItem(nameAr = "صيانة دورية وتغيير زيت 10,000 كم وفلتر أصلي", nameEn = "Synthetic Oil & Filter Service", priceEgp = 880, originalPriceEgp = 1050, descriptionAr = "زيت تخليقي بالكامل مع فلتر زيت وفلتر هواء وفحص سوائل الفرامل", category = "الصيانة الدورية"),
                    WebsiteItem(nameAr = "شحن وصيانة تكييف السيارة بغاز فريون أمريكي", nameEn = "AC Freon Refill & Maintenance", priceEgp = 420, descriptionAr = "تفريغ واختبار تسريب وشحن كامل مع تغيير فلتر صالون التكييف", category = "تكييف السيارات"),
                    WebsiteItem(nameAr = "تيل فرامل أصلي مع ضبط وتلميع طنابير", nameEn = "Brake Pads & Rotor Polish", priceEgp = 580, descriptionAr = "تيل فرامل سيراميك فائق الأداء لفرملة آمنة وهادئة بدون صوت", category = "الفرامل والعفشة")
                )
            ),

            // 8. Supermarket & Grocery
            WebsiteTemplate(
                id = "site_tpl_grocery",
                businessType = "GROCERY",
                businessTypeNameAr = "سوبرماركت ومواد غذائية",
                titleAr = "فريش ماركت للبقالة والمنتجات الطازجة",
                titleEn = "Fresh Market & Grocery",
                taglineAr = "كل مستلزمات بيتك الطازجة توصلك لحد باب بيتك في أسرع وقت",
                descriptionAr = "موقع سوبرماركت ذكي يتيح لزبائن المنطقة تصفح العروض الأسبوعية وإرسال طلبات البقالة بضغطة زر لواتساب.",
                primaryColorHex = "#388E3C", // Fresh Green
                secondaryColorHex = "#66BB6A",
                darkBgHex = "#08160B",
                priceEgp = 0,
                isPremium = false,
                isPublished = true,
                featureToggles = WebsiteFeatureToggles(
                    featureWhatsappOrders = true,
                    featureBookingSystem = false,
                    featureProductCatalog = true,
                    featureCustomerReviews = true,
                    featureFaqSection = true,
                    featureGoogleMaps = true,
                    featureDirectCalling = true
                ),
                sampleHeadline = "طلبات البيت والخضروات الطازجة دليفري في 30 دقيقة",
                sampleSubheadline = "عروض يومية وخصومات حقيقية على كافة السلع الغذائية ومستلزمات النظافة",
                sampleAbout = "ماركت حديث يوفر تشكيلة هائلة من الخضروات والفاكهة الطازجة يومياً، اللحوم والدواجن المبردة، منتجات الألبان، والمستلزمات المنزلية بأسعار تنافسية.",
                sampleAddress = "شارع جمال عبد الناصر، ميامي، الإسكندرية",
                sampleWorkingHours = "مفتوح يومياً من 8:00 صباحاً حتى 1:00 بعد منتصف الليل",
                samplePhone = "01055443322",
                sampleWhatsapp = "201055443322",
                sampleItems = listOf(
                    WebsiteItem(nameAr = "كرتونة الخير والتوفير الأسبوعية للأسرة", nameEn = "Weekly Family Grocery Box", priceEgp = 420, originalPriceEgp = 510, descriptionAr = "سكر، أرز، زيت ذرة، مكرونة، شاي، سمن، وصلصة طماطم", category = "عروض البقالة", badgeAr = "توفير 20%"),
                    WebsiteItem(nameAr = "جبن شيدر طبيعي معتق نيوزيلندي 500 جم", nameEn = "Mature Cheddar Cheese", priceEgp = 135, descriptionAr = "جبن شيدر أصلي غني بالطعم الفاخر بدون زيوت نباتية مهدرجة", category = "الألبان والأجبان"),
                    WebsiteItem(nameAr = "صندوق خضروات مشكلة منتقاة بعناية 5 كجم", nameEn = "Fresh Vegetable Box 5kg", priceEgp = 95, originalPriceEgp = 120, descriptionAr = "طماطم، خيار، بطاطس تحمير، جزر، بصل وليمون طازج يومياً", category = "الخضروات والفاكهة"),
                    WebsiteItem(nameAr = "منظف أرضيات ومطهر عام برائحة اللافندر 3 لتر", nameEn = "Multipurpose Cleaner 3L", priceEgp = 68, descriptionAr = "تعقيم ونظافة تدوم 24 ساعة برائحة منعشة ومطهرة", category = "المنظفات والمنزل")
                )
            ),

            // 9. Digital Agency & Marketing
            WebsiteTemplate(
                id = "site_tpl_agency",
                businessType = "AGENCY",
                businessTypeNameAr = "شركات ووكالات تسويق وتصميم",
                titleAr = "وكالة نيكست ميديا للتسويق والحلول الرقمية",
                titleEn = "NextMedia Creative & Marketing Agency",
                taglineAr = "نحول علامتك التجارية إلى قصة نجاح ملهمة وتضاعف مبيعاتك عبر الإنترنت",
                descriptionAr = "موقع احترافي للشركات والوكالات لعرض حزم الخدمات الإعلانية وسابقة الأعمال وطلب عروض الأسعار.",
                primaryColorHex = "#7B1FA2", // Creative Purple
                secondaryColorHex = "#AB47BC",
                darkBgHex = "#130517",
                priceEgp = 0,
                isPremium = false,
                isPublished = true,
                featureToggles = WebsiteFeatureToggles(
                    featureWhatsappOrders = true,
                    featureBookingSystem = true,
                    featureProductCatalog = true,
                    featureCustomerReviews = true,
                    featureFaqSection = true,
                    featureGoogleMaps = true,
                    featureDirectCalling = true
                ),
                sampleHeadline = "حلول تسويقية متكاملة تصنع الفارق لعلامتك التجارية",
                sampleSubheadline = "إدارة الحملات الإعلانية الممولة، إدارة السوشيال ميديا، وتطوير الهويات البصرية والمواقع",
                sampleAbout = "وكالة تسويق رقمي وإعلانات ساعدت أكثر من 180 علامة تجارية في مصر والخليج على زيادة مبيعاتها وبناء هوية بصرية راسخة في السوق.",
                sampleAddress = "مبنى هاب بيزنس، الشيخ زايد، الجيزة",
                sampleWorkingHours = "الأحد إلى الخميس: 9:30 صباحاً - 6:00 مساءً",
                samplePhone = "01033221100",
                sampleWhatsapp = "201033221100",
                sampleItems = listOf(
                    WebsiteItem(nameAr = "باقة إدارة السوشيال ميديا الشاملة (16 بوست + إعلانات)", nameEn = "Social Media Growth Package", priceEgp = 3500, originalPriceEgp = 4500, descriptionAr = "تصميم منشورات احترافية، كتابة محتوى تسويقي، وجدولة وتتبع النتائج", category = "إدارة السوشيال ميديا", badgeAr = "الأكثر طلباً"),
                    WebsiteItem(nameAr = "تصميم هوية بصرية تجارية كاملة (لوجو + براند بوك)", nameEn = "Full Brand Identity Kit", priceEgp = 2800, originalPriceEgp = 3600, descriptionAr = "شعار، ألوان الهوية، بطاقات العمل، أوراق المراسلات والزي الموحد", category = "التصميم والهوية"),
                    WebsiteItem(nameAr = "إدارة حملات إعلانية ممولة (فيسبوك + إنستغرام + تيك توك)", nameEn = "Paid Ads Management", priceEgp = 2400, descriptionAr = "استهداف عملاء محتملين بدقة وتخفيض تكلفة النقرة وزيادة المبيعات", category = "الحملات الإعلانية"),
                    WebsiteItem(nameAr = "إنتاج فيديو ريلز إعلاني موشن جرافيك 30 ثانية", nameEn = "Motion Graphics Promo Video", priceEgp = 1900, descriptionAr = "سيناريو جذاب، تعليق صوتي احترافي، ومؤثرات بصرية سينمائية", category = "الفيديو والموشن")
                )
            ),

            // 10. Academy & Training Courses
            WebsiteTemplate(
                id = "site_tpl_academy",
                businessType = "ACADEMY",
                businessTypeNameAr = "أكاديميات وتدريب وكورسات",
                titleAr = "أكاديمية فيوتشر لعلوم البرمجة والتصميم",
                titleEn = "Future Tech Academy & Courses",
                taglineAr = "اكتسب مهارات المستقبل وتأهل لسوق العمل الحر والشركات العالمية",
                descriptionAr = "موقع تعليمي متكامل للأكاديميات ومراكز التدريب لعرض الدورات والمدربين واستمارة التسجيل المباشر.",
                primaryColorHex = "#0288D1", // Deep Tech Blue
                secondaryColorHex = "#29B6F6",
                darkBgHex = "#05121A",
                priceEgp = 0,
                isPremium = false,
                isPublished = true,
                featureToggles = WebsiteFeatureToggles(
                    featureWhatsappOrders = true,
                    featureBookingSystem = true,
                    featureProductCatalog = true,
                    featureCustomerReviews = true,
                    featureFaqSection = true,
                    featureGoogleMaps = true,
                    featureDirectCalling = true
                ),
                sampleHeadline = "ابدأ رحلتك الاحترافية في مجالات التكنولوجيا والذكاء الاصطناعي",
                sampleSubheadline = "دورات تدريبية عملية وتطبيقية بمشاريع حقيقية وشهادات معتمدة وفرص توظيف",
                sampleAbout = "أكاديمية تعليمية رائدة تقدم برامج تدريبية متطورة في البرمجة، تصميم واجهات المستخدم، الذكاء الاصطناعي، والتسويق الرقمي بإشراف خبراء معتمدين.",
                sampleAddress = "شارع التحرير، الدقي، الجيزة",
                sampleWorkingHours = "السبت إلى الخميس: 10:00 صباحاً - 9:00 مساءً",
                samplePhone = "01077889900",
                sampleWhatsapp = "201077889900",
                sampleItems = listOf(
                    WebsiteItem(nameAr = "دبلومة البرمجة وتطوير تطبيقات الموبايل بالذكاء الاصطناعي", nameEn = "Mobile & AI Full Diploma", priceEgp = 2800, originalPriceEgp = 3600, descriptionAr = "80 ساعة تدريبية، 6 مشاريع عملية كاملة، ومتابعة حتى إطلاق تطبيقك", category = "تطوير التطبيقات", badgeAr = "شهادة معتمدة"),
                    WebsiteItem(nameAr = "كورس تصميم واجهات وتجربة المستخدم UI/UX الاحترافي", nameEn = "UI/UX Design Masterclass", priceEgp = 1900, originalPriceEgp = 2400, descriptionAr = "تعلم أسرار فيجما والتفكير التصميمي وبناء بورتفوليو قوي للعمل عن بعد", category = "التصميم والجرافيك"),
                    WebsiteItem(nameAr = "دورة التسويق الرقمي وتحليل البيانات المتقدمة", nameEn = "Digital Marketing & Analytics", priceEgp = 1650, descriptionAr = "إعلانات جوجل، تحسين محركات البحث SEO، وأسرار مضاعفة المبيعات", category = "التسويق الرقمي"),
                    WebsiteItem(nameAr = "كورس العمل الحر Freelancing وبناء المعرض الشخصي", nameEn = "Freelancing Kickstart", priceEgp = 600, originalPriceEgp = 850, descriptionAr = "كيف تجذب عملاء بالدولار من منصات Upwork وFiverr وكتابة عروض مقنعة", category = "العمل الحر", badgeAr = "عملي")
                )
            ),

            WebsiteTemplate(
                id = "site_tpl_gym",
                businessType = "GYM",
                businessTypeNameAr = "جيم ولياقة بدنية",
                titleAr = "نادي فيت زون الرياضي",
                titleEn = "FitZone Gym",
                taglineAr = "تمرن بقوة مع مدربين معتمدين وأجهزة حديثة",
                descriptionAr = "قالب موقع جاهز لنشاط جيم ولياقة بدنية مع قائمة الخدمات والأسعار وطلبات وحجز عبر واتساب وخريطة ومواعيد العمل.",
                primaryColorHex = "#E53935",
                secondaryColorHex = "#FF8A65",
                darkBgHex = "#12090A",
                priceEgp = 0,
                isPremium = false,
                isPublished = true,
                featureToggles = WebsiteFeatureToggles(
                    featureWhatsappOrders = true,
                    featureBookingSystem = true,
                    featureProductCatalog = true,
                    featureCustomerReviews = true,
                    featureFaqSection = true,
                    featureGoogleMaps = true,
                    featureDirectCalling = true
                ),
                sampleHeadline = "طاقتك تبدأ من هنا",
                sampleSubheadline = "اشتراكات مرنة وحصص جماعية وتدريب شخصي",
                sampleAbout = "جيم متكامل بأحدث الأجهزة ومدربين معتمدين وبرامج تناسب كل المستويات.",
                sampleAddress = "شارع الجمهورية، طنطا",
                sampleWorkingHours = "يومياً من 6 صباحاً حتى 12 منتصف الليل",
                samplePhone = "01012345678",
                sampleWhatsapp = "201012345678",
                sampleItems = listOf(
                    WebsiteItem(nameAr = "اشتراك شهري مفتوح", nameEn = "Monthly Pass", priceEgp = 450, originalPriceEgp = 550, descriptionAr = "دخول غير محدود لكل الأجهزة والحصص", category = "الاشتراكات", badgeAr = "الأوفر"),
                    WebsiteItem(nameAr = "اشتراك 3 شهور", nameEn = "3 Months", priceEgp = 1200, originalPriceEgp = 1500, descriptionAr = "وفّر أكتر مع اشتراك ربع سنوي", category = "الاشتراكات"),
                    WebsiteItem(nameAr = "تدريب شخصي PT (8 حصص)", nameEn = "Personal Training", priceEgp = 1600, descriptionAr = "برنامج مخصص مع مدرب معتمد", category = "التدريب"),
                    WebsiteItem(nameAr = "حصة كروس فيت جماعية", nameEn = "CrossFit Class", priceEgp = 120, descriptionAr = "حصة جماعية عالية الشدة 60 دقيقة", category = "الحصص", badgeAr = "جديد")
                )
            ),

            WebsiteTemplate(
                id = "site_tpl_electronics",
                businessType = "ELECTRONICS",
                businessTypeNameAr = "موبايلات وإلكترونيات",
                titleAr = "متجر تك زون للإلكترونيات",
                titleEn = "TechZone Electronics",
                taglineAr = "أحدث الأجهزة بأسعار منافسة وضمان رسمي",
                descriptionAr = "قالب موقع جاهز لنشاط موبايلات وإلكترونيات مع قائمة الخدمات والأسعار وطلبات وحجز عبر واتساب وخريطة ومواعيد العمل.",
                primaryColorHex = "#1E88E5",
                secondaryColorHex = "#64B5F6",
                darkBgHex = "#07101A",
                priceEgp = 0,
                isPremium = false,
                isPublished = true,
                featureToggles = WebsiteFeatureToggles(
                    featureWhatsappOrders = true,
                    featureBookingSystem = true,
                    featureProductCatalog = true,
                    featureCustomerReviews = true,
                    featureFaqSection = true,
                    featureGoogleMaps = true,
                    featureDirectCalling = true
                ),
                sampleHeadline = "كل جديد في التكنولوجيا عندنا",
                sampleSubheadline = "موبايلات ولابتوبات وإكسسوارات بضمان",
                sampleAbout = "متجر إلكترونيات يقدم أحدث الأجهزة والإكسسوارات مع صيانة وضمان.",
                sampleAddress = "شارع عبد العزيز، القاهرة",
                sampleWorkingHours = "يومياً من 10 صباحاً حتى 11 مساءً",
                samplePhone = "01012345678",
                sampleWhatsapp = "201012345678",
                sampleItems = listOf(
                    WebsiteItem(nameAr = "موبايل ذكي 256 جيجا", nameEn = "Smartphone 256GB", priceEgp = 12500, originalPriceEgp = 13900, descriptionAr = "شاشة AMOLED وكاميرا احترافية وبطارية تدوم", category = "موبايلات", badgeAr = "الأكثر مبيعاً"),
                    WebsiteItem(nameAr = "لابتوب للدراسة والعمل", nameEn = "Laptop", priceEgp = 18900, descriptionAr = "معالج سريع وذاكرة 16 جيجا", category = "لابتوبات"),
                    WebsiteItem(nameAr = "سماعات لاسلكية بخاصية العزل", nameEn = "Wireless Earbuds", priceEgp = 850, originalPriceEgp = 1100, descriptionAr = "صوت نقي وبطارية 24 ساعة", category = "إكسسوارات", badgeAr = "خصم"),
                    WebsiteItem(nameAr = "شاحن سريع 65 واط", nameEn = "Fast Charger", priceEgp = 350, descriptionAr = "شحن سريع آمن لكل الأجهزة", category = "إكسسوارات")
                )
            ),

            WebsiteTemplate(
                id = "site_tpl_photo",
                businessType = "PHOTO",
                businessTypeNameAr = "استوديو تصوير",
                titleAr = "استوديو لقطة للتصوير",
                titleEn = "Laqta Photo Studio",
                taglineAr = "نحوّل لحظاتك لصور تعيش معاك العمر",
                descriptionAr = "قالب موقع جاهز لنشاط استوديو تصوير مع قائمة الخدمات والأسعار وطلبات وحجز عبر واتساب وخريطة ومواعيد العمل.",
                primaryColorHex = "#8E24AA",
                secondaryColorHex = "#BA68C8",
                darkBgHex = "#120A16",
                priceEgp = 0,
                isPremium = false,
                isPublished = true,
                featureToggles = WebsiteFeatureToggles(
                    featureWhatsappOrders = true,
                    featureBookingSystem = true,
                    featureProductCatalog = true,
                    featureCustomerReviews = true,
                    featureFaqSection = true,
                    featureGoogleMaps = true,
                    featureDirectCalling = true
                ),
                sampleHeadline = "كل لقطة حكاية",
                sampleSubheadline = "تصوير أفراح وبورتريه ومنتجات بجودة عالية",
                sampleAbout = "استوديو تصوير احترافي بإضاءة ومعدات حديثة وفريق يهتم بأدق التفاصيل.",
                sampleAddress = "شارع التحرير، الجيزة",
                sampleWorkingHours = "يومياً من 11 صباحاً حتى 9 مساءً",
                samplePhone = "01012345678",
                sampleWhatsapp = "201012345678",
                sampleItems = listOf(
                    WebsiteItem(nameAr = "جلسة تصوير بورتريه", nameEn = "Portrait Session", priceEgp = 900, originalPriceEgp = 1100, descriptionAr = "ساعة تصوير و15 صورة معدلة", category = "جلسات", badgeAr = "الأكثر طلباً"),
                    WebsiteItem(nameAr = "تصوير فرح كامل", nameEn = "Wedding Package", priceEgp = 7500, descriptionAr = "تغطية كاملة مع ألبوم وفيديو قصير", category = "الأفراح"),
                    WebsiteItem(nameAr = "تصوير منتجات للمتاجر", nameEn = "Product Photography", priceEgp = 1500, descriptionAr = "20 منتج بخلفية بيضاء وتعديل", category = "منتجات"),
                    WebsiteItem(nameAr = "جلسة عائلية", nameEn = "Family Session", priceEgp = 1200, descriptionAr = "صور عائلية في الاستوديو أو بالخارج", category = "جلسات")
                )
            ),

            WebsiteTemplate(
                id = "site_tpl_travel",
                businessType = "TRAVEL",
                businessTypeNameAr = "سياحة وسفر",
                titleAr = "شركة رحلات أفق للسياحة",
                titleEn = "Ofoq Travel",
                taglineAr = "رحلات منظمة بأسعار ممتازة وتجربة بدون تعقيد",
                descriptionAr = "قالب موقع جاهز لنشاط سياحة وسفر مع قائمة الخدمات والأسعار وطلبات وحجز عبر واتساب وخريطة ومواعيد العمل.",
                primaryColorHex = "#00ACC1",
                secondaryColorHex = "#4DD0E1",
                darkBgHex = "#06141A",
                priceEgp = 0,
                isPremium = false,
                isPublished = true,
                featureToggles = WebsiteFeatureToggles(
                    featureWhatsappOrders = true,
                    featureBookingSystem = true,
                    featureProductCatalog = true,
                    featureCustomerReviews = true,
                    featureFaqSection = true,
                    featureGoogleMaps = true,
                    featureDirectCalling = true
                ),
                sampleHeadline = "سافر وأحنا نرتب كل حاجة",
                sampleSubheadline = "رحلات داخلية وعمرة وبرامج سياحية خارجية",
                sampleAbout = "شركة سياحة تنظم رحلات داخلية وخارجية وتحجز الطيران والفنادق وتجهز التأشيرات.",
                sampleAddress = "شارع مكرم عبيد، مدينة نصر",
                sampleWorkingHours = "من السبت للخميس 10 صباحاً حتى 8 مساءً",
                samplePhone = "01012345678",
                sampleWhatsapp = "201012345678",
                sampleItems = listOf(
                    WebsiteItem(nameAr = "رحلة شرم الشيخ 4 أيام", nameEn = "Sharm 4 Days", priceEgp = 3900, originalPriceEgp = 4500, descriptionAr = "إقامة بفندق 4 نجوم مع إفطار ومواصلات", category = "رحلات داخلية", badgeAr = "الأكثر حجزاً"),
                    WebsiteItem(nameAr = "رحلة الأقصر وأسوان 5 أيام", nameEn = "Luxor Aswan", priceEgp = 5200, descriptionAr = "برنامج زيارات مع مرشد سياحي", category = "رحلات داخلية"),
                    WebsiteItem(nameAr = "برنامج عمرة اقتصادي", nameEn = "Umrah Package", priceEgp = 32000, descriptionAr = "طيران وإقامة وتنقلات", category = "عمرة وحج"),
                    WebsiteItem(nameAr = "حجز تذاكر طيران", nameEn = "Flight Booking", priceEgp = 0, descriptionAr = "نحجز لك أفضل سعر متاح - اتصل للسعر", category = "خدمات")
                )
            ),

            WebsiteTemplate(
                id = "site_tpl_lawyer",
                businessType = "LAWYER",
                businessTypeNameAr = "محاماة واستشارات قانونية",
                titleAr = "مكتب العدل للمحاماة",
                titleEn = "Al-Adl Law Office",
                taglineAr = "استشارات وقضايا بخبرة وسرية تامة",
                descriptionAr = "قالب موقع جاهز لنشاط محاماة واستشارات قانونية مع قائمة الخدمات والأسعار وطلبات وحجز عبر واتساب وخريطة ومواعيد العمل.",
                primaryColorHex = "#5D4037",
                secondaryColorHex = "#A1887F",
                darkBgHex = "#0E0A08",
                priceEgp = 0,
                isPremium = false,
                isPublished = true,
                featureToggles = WebsiteFeatureToggles(
                    featureWhatsappOrders = true,
                    featureBookingSystem = true,
                    featureProductCatalog = true,
                    featureCustomerReviews = true,
                    featureFaqSection = true,
                    featureGoogleMaps = true,
                    featureDirectCalling = true
                ),
                sampleHeadline = "حقك محفوظ بإذن الله",
                sampleSubheadline = "استشارات قانونية وقضايا مدنية وتجارية وأسرة",
                sampleAbout = "مكتب محاماة يقدم استشارات وتمثيل قانوني في القضايا المدنية والتجارية والأحوال الشخصية.",
                sampleAddress = "شارع قصر العيني، القاهرة",
                sampleWorkingHours = "من السبت للخميس 10 صباحاً حتى 6 مساءً",
                samplePhone = "01012345678",
                sampleWhatsapp = "201012345678",
                sampleItems = listOf(
                    WebsiteItem(nameAr = "استشارة قانونية (30 دقيقة)", nameEn = "Consultation", priceEgp = 500, descriptionAr = "جلسة استشارة مع محامٍ متخصص", category = "استشارات"),
                    WebsiteItem(nameAr = "تأسيس شركة وعقود", nameEn = "Company Setup", priceEgp = 3500, descriptionAr = "إجراءات التأسيس وصياغة العقود", category = "شركات", badgeAr = "مطلوب"),
                    WebsiteItem(nameAr = "قضايا الأحوال الشخصية", nameEn = "Family Law", priceEgp = 0, descriptionAr = "تمثيل وإجراءات - اتصل للتسعير", category = "قضايا"),
                    WebsiteItem(nameAr = "صياغة ومراجعة العقود", nameEn = "Contract Review", priceEgp = 800, descriptionAr = "مراجعة قانونية دقيقة قبل التوقيع", category = "عقود")
                )
            ),

            WebsiteTemplate(
                id = "site_tpl_bakery",
                businessType = "BAKERY",
                businessTypeNameAr = "مخابز وحلويات",
                titleAr = "مخبز وحلويات الفرن البلدي",
                titleEn = "Local Oven Bakery",
                taglineAr = "مخبوزات طازجة وتورت حسب الطلب",
                descriptionAr = "قالب موقع جاهز لنشاط مخابز وحلويات مع قائمة الخدمات والأسعار وطلبات وحجز عبر واتساب وخريطة ومواعيد العمل.",
                primaryColorHex = "#F57C00",
                secondaryColorHex = "#FFB74D",
                darkBgHex = "#140C05",
                priceEgp = 0,
                isPremium = false,
                isPublished = true,
                featureToggles = WebsiteFeatureToggles(
                    featureWhatsappOrders = true,
                    featureBookingSystem = true,
                    featureProductCatalog = true,
                    featureCustomerReviews = true,
                    featureFaqSection = true,
                    featureGoogleMaps = true,
                    featureDirectCalling = true
                ),
                sampleHeadline = "طازج من الفرن كل يوم",
                sampleSubheadline = "عيش وفطائر وتورت لكل مناسباتك",
                sampleAbout = "مخبز وحلواني يقدم مخبوزات طازجة يومياً وتورت ومعجنات حسب الطلب.",
                sampleAddress = "شارع الهرم، الجيزة",
                sampleWorkingHours = "يومياً من 6 صباحاً حتى 11 مساءً",
                samplePhone = "01012345678",
                sampleWhatsapp = "201012345678",
                sampleItems = listOf(
                    WebsiteItem(nameAr = "تورتة شوكولاتة فاخرة", nameEn = "Chocolate Cake", priceEgp = 450, originalPriceEgp = 520, descriptionAr = "طبقات كيك وغاناش شوكولاتة بلجيكي", category = "تورت", badgeAr = "الأكثر طلباً"),
                    WebsiteItem(nameAr = "بوكس معجنات مشكلة", nameEn = "Pastry Box", priceEgp = 180, descriptionAr = "12 قطعة من الفطائر والمعجنات", category = "معجنات"),
                    WebsiteItem(nameAr = "كرواسون بالزبدة (6 قطع)", nameEn = "Croissant x6", priceEgp = 120, descriptionAr = "هش وطازج من الفرن", category = "مخبوزات"),
                    WebsiteItem(nameAr = "تورتة مناسبات حسب الطلب", nameEn = "Custom Cake", priceEgp = 0, descriptionAr = "صمم تورتتك - اتصل للتسعير", category = "تورت")
                )
            ),

            WebsiteTemplate(
                id = "site_tpl_cafe",
                businessType = "CAFE",
                businessTypeNameAr = "كافيهات ومشروبات",
                titleAr = "كافيه أرابيكا المختص",
                titleEn = "Arabica Specialty Cafe",
                taglineAr = "قهوة مختصة وأجواء هادئة للعمل واللمّة",
                descriptionAr = "قالب موقع جاهز لنشاط كافيهات ومشروبات مع قائمة الخدمات والأسعار وطلبات وحجز عبر واتساب وخريطة ومواعيد العمل.",
                primaryColorHex = "#6D4C41",
                secondaryColorHex = "#BCAAA4",
                darkBgHex = "#100B08",
                priceEgp = 0,
                isPremium = false,
                isPublished = true,
                featureToggles = WebsiteFeatureToggles(
                    featureWhatsappOrders = true,
                    featureBookingSystem = true,
                    featureProductCatalog = true,
                    featureCustomerReviews = true,
                    featureFaqSection = true,
                    featureGoogleMaps = true,
                    featureDirectCalling = true
                ),
                sampleHeadline = "قهوتك المفضلة في مكان هادئ",
                sampleSubheadline = "قهوة مختصة وحلويات ومساحة للعمل",
                sampleAbout = "كافيه يقدم قهوة مختصة محمصة طازجة وحلويات ومساحة هادئة للعمل والاجتماعات.",
                sampleAddress = "شارع الجامعة، المنصورة",
                sampleWorkingHours = "يومياً من 8 صباحاً حتى 1 بعد منتصف الليل",
                samplePhone = "01012345678",
                sampleWhatsapp = "201012345678",
                sampleItems = listOf(
                    WebsiteItem(nameAr = "لاتيه بنكهة الكراميل", nameEn = "Caramel Latte", priceEgp = 85, descriptionAr = "إسبريسو مع حليب مخفوق وكراميل", category = "القهوة الساخنة", badgeAr = "الأكثر طلباً"),
                    WebsiteItem(nameAr = "آيس سبانش لاتيه", nameEn = "Spanish Latte Iced", priceEgp = 95, descriptionAr = "بارد ومنعش بحليب مكثف", category = "المشروبات الباردة"),
                    WebsiteItem(nameAr = "كولد برو 12 ساعة", nameEn = "Cold Brew", priceEgp = 90, descriptionAr = "قهوة منقوعة باردة ناعمة", category = "المشروبات الباردة", badgeAr = "جديد"),
                    WebsiteItem(nameAr = "كرواسون لوز مع قهوة", nameEn = "Almond Croissant Combo", priceEgp = 120, originalPriceEgp = 140, descriptionAr = "كرواسون لوز طازج مع مشروب", category = "حلويات", badgeAr = "كومبو")
                )
            ),

            WebsiteTemplate(
                id = "site_tpl_hotel",
                businessType = "HOTEL",
                businessTypeNameAr = "فنادق وشاليهات",
                titleAr = "فندق وشاليهات الواحة",
                titleEn = "Oasis Hotel & Chalets",
                taglineAr = "إقامة مريحة بإطلالة رائعة وخدمة تليق بك",
                descriptionAr = "قالب موقع جاهز لنشاط فنادق وشاليهات مع قائمة الخدمات والأسعار وطلبات وحجز عبر واتساب وخريطة ومواعيد العمل.",
                primaryColorHex = "#00897B",
                secondaryColorHex = "#80CBC4",
                darkBgHex = "#06120F",
                priceEgp = 0,
                isPremium = false,
                isPublished = true,
                featureToggles = WebsiteFeatureToggles(
                    featureWhatsappOrders = true,
                    featureBookingSystem = true,
                    featureProductCatalog = true,
                    featureCustomerReviews = true,
                    featureFaqSection = true,
                    featureGoogleMaps = true,
                    featureDirectCalling = true
                ),
                sampleHeadline = "إقامتك عندنا راحة كاملة",
                sampleSubheadline = "غرف وشاليهات مع إفطار وخدمات متكاملة",
                sampleAbout = "فندق وشاليهات يقدم غرفاً مريحة وإفطاراً وخدمات ترفيهية ومواقف مجانية.",
                sampleAddress = "طريق الساحل الشمالي، مرسى مطروح",
                sampleWorkingHours = "استقبال على مدار 24 ساعة",
                samplePhone = "01012345678",
                sampleWhatsapp = "201012345678",
                sampleItems = listOf(
                    WebsiteItem(nameAr = "غرفة مزدوجة بإفطار", nameEn = "Double Room", priceEgp = 1400, originalPriceEgp = 1700, descriptionAr = "غرفة مكيفة مع إفطار وواي فاي", category = "الغرف", badgeAr = "الأكثر حجزاً"),
                    WebsiteItem(nameAr = "جناح عائلي", nameEn = "Family Suite", priceEgp = 2600, descriptionAr = "غرفتين وصالة لحد 5 أفراد", category = "الأجنحة"),
                    WebsiteItem(nameAr = "شاليه بإطلالة البحر", nameEn = "Sea View Chalet", priceEgp = 3200, originalPriceEgp = 3800, descriptionAr = "شاليه كامل بمطبخ وشرفة", category = "الشاليهات"),
                    WebsiteItem(nameAr = "باقة شهر العسل", nameEn = "Honeymoon Package", priceEgp = 4500, descriptionAr = "ليلتين مع عشاء وتزيين الغرفة", category = "العروض", badgeAr = "عرض")
                )
            )
        )
    }
}
