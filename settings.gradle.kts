pluginManagement {
    plugins {
        id("net.fabricmc.fabric-loom-remap") version providers.gradleProperty("loom_version")
    }

    repositories {
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "remember-my-mods"
