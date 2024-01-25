
rootProject.name = "iSLAND"
include("model")
include("main-Bukkit")
include("main-Bungee")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://hub.spigotmc.org/nexus/content/groups/public/")
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://repo.dmulloy2.net/nexus/repository/public/")
        maven("https://ci.ender.zone/plugin/repository/everything/")
    }
}
include("service:api")
include("service:bukkit-service-impl")
include("service:bungee-service-impl")
