pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SGC-Contar"
include(":app", ":sgc")   // <-- ajuste se as pastas tiverem outros nomes
