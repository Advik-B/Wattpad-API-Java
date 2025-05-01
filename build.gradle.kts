plugins {
    id("java")
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

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

}

java {
    sourceCompatibility = JavaVersion.VERSION_11 // Good balance for library compatibility
//    targetCompatibility = JavaVersion.VERSION_11
    // For Android compatibility, you might target Java 8 (Android API level dependent)
//     sourceCompatibility = JavaVersion.VERSION_1_8
     targetCompatibility = JavaVersion.VERSION_1_8

    // Automatically add Javadoc and Sources jars when publishing
    withJavadocJar()
    withSourcesJar()
}

tasks.test {
    useJUnitPlatform()
}