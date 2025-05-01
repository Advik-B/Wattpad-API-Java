plugins {
    id("java")
    // Apply the shadow plugin to build fat JARs
    id("com.github.johnrengelman.shadow") version "8.1.1" // Use a recent version
}

group = "dev.advik"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
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

    // Automatically add Javadoc and Sources jars when publishing
    withJavadocJar()
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
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Main-Class" to "dev.advik.Main" // Set the main class for the fat JAR
        )
    }
    // You might want to configure the output JAR name if needed
    // archiveBaseName.set("my-fat-jar-name")
    // archiveClassifier.set("") // Set to empty string to avoid "-all" suffix if desired
}