plugins {
    id("java")
}

group = "edu.shch.mine"
version = "0.1.0"

repositories {
    mavenCentral()
}

tasks {
    jar {
        manifest {
            attributes["Premain-Class"] = "edu.shch.jdk.Agent"
            attributes["Implementation-Version"] = archiveVersion
            attributes["Can-Redefine-Classes"] = "true"
            attributes["Can-Retransform-Classes"] = "true"
        }
        archiveClassifier.set("")
    }
}