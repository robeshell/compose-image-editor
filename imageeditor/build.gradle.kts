plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.vanniktech.maven.publish)
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
}

// 发布配置(Maven Central via Central Portal + 本地/JitPack)。
// 凭据与签名密钥走 ~/.gradle/gradle.properties 或环境变量,不入库。见 PUBLISHING.md。
// JitPack 不签名(无密钥时 publishToMavenLocal 自动跳过签名),仍可正常构建。
mavenPublishing {
    publishToMavenCentral()
    // 仅在提供了签名密钥时签名:本地/JitPack(无密钥)跳过,发 Central(有密钥)启用。
    if (providers.gradleProperty("signingInMemoryKey").isPresent ||
        providers.gradleProperty("signing.keyId").isPresent
    ) {
        signAllPublications()
    }

    coordinates("io.github.robeshell", "compose-image-editor", "0.1.0")

    pom {
        name.set("Compose Image Editor")
        description.set("Compose-first WeChat-style image editor for Android: crop, draw, mosaic, text, stickers.")
        url.set("https://github.com/robeshell/compose-image-editor")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("robeshell")
                name.set("robeshell")
                url.set("https://github.com/robeshell")
            }
        }
        scm {
            url.set("https://github.com/robeshell/compose-image-editor")
            connection.set("scm:git:git://github.com/robeshell/compose-image-editor.git")
            developerConnection.set("scm:git:ssh://git@github.com/robeshell/compose-image-editor.git")
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

    testImplementation(libs.junit)
}
