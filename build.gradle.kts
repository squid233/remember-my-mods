plugins {
    id("net.fabricmc.fabric-loom-remap")
}

val minecraft_version: String by rootProject
val loader_version: String by rootProject

val mod_version: String by rootProject
val maven_group: String by rootProject
val archives_base_name: String by rootProject

val fabric_api_version: String by rootProject

val targetJdkVersion: String by rootProject

group = maven_group
version = mod_version

base {
    archivesName = archives_base_name
}

repositories {
    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.
}

loom {
    splitEnvironmentSourceSets()

    mods {
        register("remember-my-mods") {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets["client"])
        }
    }
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:$minecraft_version")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:$loader_version")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabric_api_version")
}

tasks.named<ProcessResources>("processResources") {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to inputs.properties["version"]!!)
    }
}

tasks.withType<JavaCompile> {
    options.release = targetJdkVersion.toInt()
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()

    sourceCompatibility = JavaVersion.toVersion(targetJdkVersion)
    targetCompatibility = JavaVersion.toVersion(targetJdkVersion)
}

tasks.named<Jar>("jar") {
    inputs.property("archivesName", project.base.archivesName)

    from("LICENSE") {
        rename { "${it}_${inputs.properties["archivesName"]}" }
    }
}
