plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.gonews"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.gonews"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)

    // Biarkan ini, karena ini mengacu pada versi yang didefinisikan di libs.versions.toml
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)

    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Retrofit & GSON
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    // Glide for image loading
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")

    // AndroidX UI Libraries (Pastikan versi ini konsisten dengan yang Anda inginkan)
    implementation ("androidx.cardview:cardview:1.0.0")
    implementation ("androidx.recyclerview:recyclerview:1.3.2")
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation ("com.google.android.material:material:1.12.0")

    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3") // Gunakan versi terbaru yang
    implementation ("com.google.android.material:material:1.12.0") // Gunakan versi terbaru yang kompatibel
    implementation ("com.airbnb.android:lottie:6.6.6")
    implementation ("de.hdodenhof:circleimageview:3.1.0")

    // ViewPager2
    implementation ("androidx.viewpager2:viewpager2:1.0.0")
    // Glide (untuk memuat gambar dari URL)
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
}