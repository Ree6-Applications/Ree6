plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "de.presti"
version = "1.9.9"
val artifactId = "Ree6"
description = "Ree6 is an open-Source Discord Bot."

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    mavenLocal()
    maven(url = "https://m2.dv8tion.net/releases")
    maven(url = "https://jitpack.io")
    maven(url = "https://repo.maven.apache.org/maven2/")
    maven(url = "https://twitter4j.org/maven2")
}

dependencies {
    //Testing stuff
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.1")

    //Discord stuff
    implementation("net.dv8tion:JDA:5.0.0-alpha.20")
    implementation("ch.qos.logback:logback-classic:1.4.3")
    implementation("org.slf4j:slf4j-api:2.0.3")
    implementation("com.github.walkyst:lavaplayer-fork:1.3.98.4")
    implementation("com.sedmelluq:lava-common:1.1.2")

    //Reddit API Wrapper
    implementation("com.github.masecla22:Reddit4J:master-SNAPSHOT")

    //Twitter API Wrapper
    implementation("com.github.Twitter4J.Twitter4J:twitter4j-core:4.0.7")
    implementation("com.github.Twitter4J.Twitter4J:twitter4j-async:4.0.7")
    implementation("com.github.Twitter4J.Twitter4J:twitter4j-stream:4.0.7")
    implementation("com.github.Twitter4J.Twitter4J:twitter4j-http2-support:4.0.7")

    //Instagram API Wrapper
    implementation("com.github.instagram4j:instagram4j:2.0.7")

    //Spotify API Wrapper / Spotify stuff
    implementation("se.michaelthelin.spotify:spotify-web-api-java:7.2.1")
    implementation("de.ree6:JLyrics:dfb1d50975")

    //Twitter API Wrapper
    implementation("com.github.twitch4j:twitch4j:1.12.0")

    //Nekos API Wrapper
    implementation("com.github.DxsSucuk:Nekos4J:1.0.1")

    //google stuff
    implementation("com.google.cloud:google-cloud-vision:3.1.2")
    implementation("com.google.api-client:google-api-client:2.0.0")
    implementation("com.google.oauth-client:google-oauth-client:1.34.1")
    implementation("com.google.http-client:google-http-client-jackson2:1.42.2")
    implementation("com.google.api-client:google-api-client-jackson2:2.0.0")
    implementation("com.google.api-client:google-api-client-java6:2.0.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
    implementation("com.google.apis:google-api-services-youtube:v3-rev20220926-2.0.0")
    implementation("com.google.code.gson:gson:2.9.1")

    //bouncy-castle
    implementation("org.bouncycastle:bcpkix-jdk15on:1.70")
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")

    //mind-scape-hq
    implementation("com.mindscapehq:raygun4java:3.0.0")
    implementation("com.mindscapehq:core:3.0.0")

    //File stuff
    implementation("org.json:json:20220924")
    implementation("com.fasterxml.jackson.core:jackson-core:2.13.4")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.4")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.13.4")
    implementation("me.carleslc.Simple-YAML:Simple-Yaml:1.8.2")

    //Database stuff
    implementation("org.mariadb.jdbc:mariadb-java-client:3.0.8")
    implementation("com.zaxxer:HikariCP:5.0.1")

    //commons stuff
    implementation("commons-validator:commons-validator:1.7") {
        exclude(group = "commons-collections", module = "commons-collections")
    }
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("org.apache.commons:commons-digester3:3.2")
    implementation("commons-io:commons-io:2.11.0")
    implementation("commons-codec:commons-codec:1.15")

    //Http stuff
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    //discord webhooks
    implementation("club.minnced:discord-webhooks:0.8.2")

    //reflections
    implementation("org.reflections:reflections:0.10.2")

    //glassfish
    implementation("org.glassfish.jersey.media:jersey-media-json-jackson:3.0.8")
    implementation("org.glassfish.jersey.core:jersey-client:3.0.8")

    //Misc
    implementation("org.jsoup:jsoup:1.15.3")
    implementation("net.iharder:base64:2.3.9")
}

application {
    mainClass.set("de.presti.ree6.main.Main")
}

tasks.withType<Test> { useJUnitPlatform() }

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}
