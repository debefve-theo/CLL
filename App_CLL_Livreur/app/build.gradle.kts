import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.sonarqube)
}

android {
    namespace = "com.example.app_cll_livreur"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.app_cll_livreur"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }
        val googleMapsApiKey = localProperties.getProperty("GOOGLE_MAPS_API_KEY")
            ?: "YOUR_DEFAULT_API_KEY"
        buildConfigField("String", "MAPS_API_KEY", "\"$googleMapsApiKey\"")
        manifestPlaceholders["MAPS_API_KEY"] = googleMapsApiKey

    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    lint {
        abortOnError = false           // ne plus échouer la build sur erreurs
        disable.add("MissingTranslation") // désactive purement la règle
    }
}

sonarqube {
    properties {
        property("sonar.projectKey",   "CLL")
        property("sonar.projectName",  "CLL")
        property("sonar.host.url",     "http://localhost:9000")
        // on passe le token en paramètre CLI, pas ici

        // chemins relatifs au module app/
        property("sonar.sources",      "src/main/java")
        property("sonar.androidLint.reportPaths", "build/reports/lint-results.xml")

        // Chemin (ou liste de chemins) vers les classes compilées
        property(
            "sonar.java.binaries",
            "build/intermediates/javac/debug/compileDebugJavaWithJavac"
        )
        // Si vous avez du Kotlin, ajoutez aussi :
        // property("sonar.java.binaries", "build/tmp/kotlin-classes/debug")
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.maps)
    implementation(libs.maps.utils)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.play.services.location)
    implementation(libs.mlkit.barcode.scanning)
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}