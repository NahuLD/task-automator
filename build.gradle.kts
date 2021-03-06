plugins {
    java
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.3.0"
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = sourceCompatibility
}

group = "me.nahu.taskautomator"
version = "0.1.2"

repositories {
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }

    maven {
        name = "spigotmc"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }

    maven {
        name = "papermc"
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }

    maven {
        name = "aikar-repo"
        url = uri("https://repo.aikar.co/content/groups/aikar/")
    }

    maven {
        name = "themoep-repo"
        url = uri("https://repo.minebench.de/")
    }

    maven {
        name = "enginehub-repo"
        url = uri("https://maven.enginehub.org/repo/")
    }

    maven {
        name = "chatmenuapi-repo"
        url = uri("https://dl.bintray.com/nahuld/minevictus/")
    }

    maven {
        name = "placeholderapi"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }

    mavenCentral()
    jcenter()
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:20.1.0")
    compileOnly("me.clip:placeholderapi:2.10.9")
    implementation("co.aikar:acf-bukkit:0.5.0-SNAPSHOT")
    implementation("de.themoep:minedown:1.6.2-SNAPSHOT")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    arrayOf(
            "co.aikar.commands",
            "co.aikar.locales",
            "de.themoep.minedown"
    ).forEach { relocate(it, "${project.group}.shadow.$it") }
}

bukkit {
    name = "TaskAutomator"
    description = "Automate tasks!"
    main = "me.nahu.taskautomator.TaskAutomatorPlugin"
    authors = listOf("NahuLD")
}