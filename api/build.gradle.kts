import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.dependency.mgmt)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    application
    eclipse
    idea
    alias(libs.plugins.shadow)
}

application {
    mainClass.set("com.zenyte.MainKt")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))
    }
}

dependencies {
    runtimeOnly(libs.mysql)
    implementation(libs.hikari)
    implementation(libs.okhttp)
    implementation(libs.guava)
    implementation(libs.jackson.kotlin)
    implementation(libs.googleauth)
    implementation(libs.aws.ses)
    implementation(libs.gson)
    implementation(libs.commons.lang3)
    implementation(libs.kotlin.logging)
    implementation(libs.lettuce)
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = libs.versions.java.get()
}

tasks.withType<Test> {
    useJUnitPlatform()
}
