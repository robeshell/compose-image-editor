pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// 多模块:库(:imageeditor) + 示例 app(:sample)。
// 被宿主项目以 git submodule 形式包含时,本文件被宿主 settings 忽略,
// 宿主用 include(":imageeditor") + projectDir 指到 imageeditor/imageeditor 即可。
rootProject.name = "compose-image-editor"
include(":imageeditor")
include(":sample")
