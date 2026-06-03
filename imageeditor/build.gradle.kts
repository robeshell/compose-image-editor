plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    `maven-publish`
}

android {
    namespace = "com.otq.imageeditor"
    compileSdk = 35

    defaultConfig {
        minSdk = 27
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions { jvmTarget = "11" }

    buildFeatures { compose = true }

    // 供 maven-publish / JitPack 使用的发布变体
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

// JitPack:发布 release AAR。JitPack 会以 tag 作为版本号。
// 消费坐标(多模块):com.github.robeshell.compose-image-editor:imageeditor:<tag>
afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.github.robeshell.compose-image-editor"
                artifactId = "imageeditor"
                version = "0.1.0"
            }
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.coil.compose)

    // 编辑能力:裁剪(Apache-2.0) + 标注(MIT)。仅 implementation,不进公开签名。
    implementation(libs.android.image.cropper)
    implementation(libs.photoeditor)
}
