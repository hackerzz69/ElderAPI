import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    application
    alias(libs.plugins.shadow)
}

application {
    // make sure this matches your entrypoint object (BootKt or MainKt)
    mainClass.set("com.zenyte.discord.BootKt")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))
    }
}

dependencies {
    // JSON + utils
    implementation(libs.gson)
    implementation(libs.kotlin.logging)
    implementation(libs.classgraph)
    implementation(libs.kotlinx.coroutines.core)

    // Discord
    implementation(libs.jda)

    // Logging backend
    implementation(libs.logback.classic)

    // HTTP / API client
    implementation(libs.okhttp)
    implementation(libs.jackson.kotlin)

    // Unit tests
    testImplementation(kotlin("test"))
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("discord")
    archiveVersion.set("")     // omit version from filename
    archiveClassifier.set("")  // no "-all" suffix
    mergeServiceFiles()
    minimize()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = libs.versions.java.get()
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}
