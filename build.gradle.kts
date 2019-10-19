import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootWar
import org.springframework.boot.gradle.tasks.run.BootRun

//updated at 16.07.2019
val flywayVersion = "5.2.4"

plugins {
    //updated at 16.07.2019
    val kotlinVersion = "1.3.41"
    kotlin("jvm") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.jpa") version kotlinVersion

    //updated at 16.07.2019
    val springVersion = "2.1.6.RELEASE"
    id("org.springframework.boot") version springVersion
    id("io.spring.dependency-management") version "1.0.8.RELEASE"
    // Required for Kotlin integration for spring
    // See https://kotlinlang.org/docs/reference/compiler-plugins.html#kotlin-spring-compiler-plugin
    id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
    //fixes no-arg constructor errors
    id("org.jetbrains.kotlin.plugin.noarg") version kotlinVersion

    val flywayVersion = "5.2.4"
    id("org.flywaydb.flyway") version flywayVersion

    id("war")
}

group = "ru.kuchanov.gp"
version = "0.1.1"

repositories {
    mavenCentral()

    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    //spring
    implementation("org.springframework.boot:spring-boot-starter-web")
    runtime("org.springframework.boot:spring-boot-starter-tomcat")
    //info
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    //email
    implementation("org.springframework.boot:spring-boot-starter-mail")
    //data
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    //security
    implementation("org.springframework.boot:spring-boot-starter-security")
    //oauth2
    implementation("org.springframework.security.oauth:spring-security-oauth2:2.3.6.RELEASE")
    implementation("org.springframework.security:spring-security-oauth2-client")
    implementation("org.springframework.security.oauth.boot:spring-security-oauth2-autoconfigure:2.1.6.RELEASE")
    //logs
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    //testing
    implementation("org.springframework.boot:spring-boot-starter-test")
    //spring END

    //DB
    //updated at 16.07.2019
    implementation("org.postgresql:postgresql:42.2.6")
    //DB migration
    implementation("org.flywaydb:flyway-core:$flywayVersion")

    //rx
    implementation("io.reactivex.rxjava2:rxjava:2.2.1")
    implementation("io.reactivex.rxjava2:rxkotlin:2.2.0")

    //Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.4.0") { exclude(module = "okhttp") }
    implementation("com.squareup.okhttp3:okhttp:3.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:3.11.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.4.0")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.4.0")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.6")

    implementation("commons-io:commons-io:2.6")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

//to be able to run with task args to define correct properties file
//i.e. `bootRun -Dspring.profiles.active=dev`
tasks.withType<BootRun> {
    @Suppress("UNCHECKED_CAST")
    systemProperties(System.getProperties() as Map<String, Any?>)
}

//artifact
tasks.withType<BootWar> {
    project.logger.lifecycle("property " + project.hasProperty("suffix"))
    //we need to specify context path here if we use war file in external tomcat
    baseName = "api"
    version = ""
}
