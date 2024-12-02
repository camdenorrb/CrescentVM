import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    application
    kotlin("jvm") version "1.8.0"
}

group = "dev.twelveoclock.lang"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(kotlin("bom")))
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    implementation("org.capnproto:runtime:0.1.14")
    implementation("com.guardsquare:proguard-core:9.0.8")
    //implementation("tech.poder.ir:PoderTechIR:+")
    testImplementation(kotlin("test-junit5"))
}

application {
    mainClass.set("dev.twelveoclock.lang.crescent.Main")
    applicationDefaultJvmArgs = listOf(
        "--add-modules=jdk.incubator.foreign"
    )
}

tasks {

    val javaVersion = JavaVersion.VERSION_17.toString()

    withType<KotlinCompile> {
        //sourceCompatibility = javaVersion
        //targetCompatibility = javaVersion
        kotlinOptions.jvmTarget = javaVersion
        //kotlinOptions.languageVersion = "1.6"
        //kotlinOptions.apiVersion = "1.6"
        //kotlinOptions.useFir = true
    }

    withType<JavaCompile> {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    test {
        useJUnitPlatform()
        jvmArgs("--add-modules=jdk.incubator.foreign")
    }
}