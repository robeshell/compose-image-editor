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

// 单模块库:仓库根即库本身,无需 include。
// 被宿主项目以 git submodule 形式包含时,本文件会被宿主的 settings 忽略,
// 宿主用 include(":imageeditor") + 自己的版本目录即可。
rootProject.name = "imageeditor"
