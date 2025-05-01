plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("maven-publish")
}

group = "io.github.advik-b"
version = "1.1"

repositories {
    mavenCentral()
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Advik-B/Wattpad-API.jar")
            credentials {
                username = (project.findProperty("gpr.user") ?: System.getenv("USERNAME")) as String?
                password = (project.findProperty("gpr.token") ?: System.getenv("TOKEN")) as String?
            }
        }
    }
}

dependencies {
    // HTTP Client (OkHttp is robust and Android-friendly)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // JSON Parsing (Gson is standard and works on Android)
    implementation("com.google.code.gson:gson:2.10.1")

    // HTML Parsing (Jsoup is the standard Java equivalent to BeautifulSoup)
    implementation("org.jsoup:jsoup:1.17.2")

}

java {
    sourceCompatibility = JavaVersion.VERSION_11 // Good balance for library compatibility
    targetCompatibility = JavaVersion.VERSION_11

    withSourcesJar()
}


// Add manifest attributes for JAR
tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Main-Class" to "dev.advik.Main" // Adjust this if your main class is different
        )
    }
}

tasks.shadowJar {
    archiveBaseName.set(project.name) // Base name for the JAR file
    archiveClassifier.set("") // Set classifier to empty string to avoid '-all' suffix (optional)
    archiveVersion.set(project.version.toString())

    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Main-Class" to "dev.advik.Main" // Specify the main class here
        )
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = group as String
            version = version
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Advik-B/Wattpad-API.jar")
            credentials {
                username = (project.findProperty("gpr.user") ?: System.getenv("USERNAME")) as String?
                password = (project.findProperty("gpr.token") ?: System.getenv("TOKEN")) as String?
            }
        }
    }
}