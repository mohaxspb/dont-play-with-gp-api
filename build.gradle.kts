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
version = "1.0.0"

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
    //data
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    //security
    implementation("org.springframework.boot:spring-boot-starter-security")
    //oauth2
    implementation("org.springframework.security.oauth:spring-security-oauth2:2.3.0.RELEASE")
    //testing
    implementation("org.springframework.boot:spring-boot-starter-test")
    //spring END

    //DB
    //updated at 16.07.2019
    implementation("org.postgresql:postgresql:42.2.6")
    //DB migration
    implementation("org.flywaydb:flyway-core:$flywayVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

//to be able to run with task args to define correct properties file
//i.e. `bootRun -Dspring.profiles.active=dev`
tasks.withType<BootRun> {
    systemProperties(System.getProperties() as Map<String, Any?>)
}

//artifact
tasks.withType<BootWar> {
    project.logger.lifecycle("property " + project.hasProperty("suffix"))
    baseName = "dont-play-with-google-play"
    version = ""
}
