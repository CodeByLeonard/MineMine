plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "edu.shch.mine"
version = "0.1.0"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation(project(":jdk-patches"))

    constraints {
        implementation("org.apache.commons:commons-lang3:3.20.0") {
            because("CVE-2025-48924: Upgrade Apache Commons Lang from 3.12.0 -> 3.20.0")
        }

        implementation("org.apache.logging.log4j:log4j-core:2.25.3") {
            because("CVE-2025-68161: Upgrade Log4J-Core from 2.25.2 -> 2.25.3")
        }

        implementation("io.netty:netty-codec-http:4.2.10.Final") {
            because("CVE-2025-67735: Upgrade Netty Codec HTTP from 4.2.7.Final -> 4.2.10.Final")
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
    }

    paperweight {
        reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
    }

    runServer {
        minecraftVersion("1.21.11")
        val agent = project(":jdk-patches").tasks.jar.get().archiveFile.get().asFile.absolutePath
        jvmArgs("-javaagent:$agent")
        args("--nojline")
    }
}