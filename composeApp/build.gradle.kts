import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    jvm()
    
    js {
        browser()
        binaries.executable()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(compose.materialIconsExtended)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)

            // 1. Firebase (Kita potong sifat pemaksaan versinya menggunakan exclude)
            implementation("com.google.firebase:firebase-admin:9.2.0") {
                exclude(group = "com.google.api-client", module = "google-api-client")
            }

            // 2. Google OAuth (Kembali ke versi yang stabil dan serasi)
            implementation("com.google.api-client:google-api-client:1.34.1")
            implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
            implementation("com.google.apis:google-api-services-oauth2:v2-rev20200213-1.32.1")
        }
    }
}

android {
    namespace = "id.my.natsir"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "id.my.natsir"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "id.my.natsir.MainKt"

        // --- TAMBAHKAN BLOK JVM ARGS INI ---
        jvmArgs(
            "-Dio.netty.noUnsafe=true", // Melarang Netty standar memakai Unsafe memory
            "-Dio.grpc.netty.shaded.io.netty.noUnsafe=true", // Melarang Netty bawaan gRPC/Firestore memakai Unsafe
            "--add-opens=java.base/java.nio=ALL-UNNAMED", // Membuka akses refleksi library java nio
            "--add-opens=java.base/sun.misc=ALL-UNNAMED"  // Membuka akses refleksi sun misc
        )

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Exe)
            packageName = "HDMI-POS"
            packageVersion = "1.0.0"
            modules("jdk.httpserver")

            description = "HDMI POS System - Solusi Kasir Premium"
            copyright = "© 2026 PT. Hawari Digital Multimedia Inovasi. All rights reserved."
            vendor = "PT. Hawari Digital Multimedia Inovasi"

            macOS {
                bundleID = "id.my.natsir.hdmpos"
                iconFile.set(project.file("src/commonMain/composeResources/drawable/icon.icns"))

                // Menyertakan izin keamanan khusus saat jpackage membungkus file .dmg
                entitlementsFile.set(project.file("entitlements.plist"))
            }

            windows {
                perUserInstall = false
                upgradeUuid = "pilih-uuid-acak-bebas-di-sini-untuk-update-nanti"
                iconFile.set(project.file("src/commonMain/composeResources/drawable/icon.ico"))
            }
        }
    }
}
