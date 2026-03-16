plugins {
    id("java")
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

@Suppress("VulnerableLibrariesLocal")
dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    constraints {
        implementation("org.apache.commons:commons-lang3:3.20.0") {
            because("CVE-2025-48924: Upgrade Apache Commons Lang from 3.12.0 -> 3.20.0")
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
    }

    runServer {
        minecraftVersion("1.21.11")
    }
}