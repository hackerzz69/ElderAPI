import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    application
    alias(libs.plugins.shadow)
}

application {
    mainClass.set("com.zenyte.discord.MainKt")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))
    }
}

dependencies {
    implementation(libs.gson)
    implementation(libs.kotlin.logging)
    implementation("io.github.classgraph:classgraph:4.8.179")
    implementation("net.dv8tion:JDA:5.6.1")
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("discord")
    archiveVersion.set("")     // omit version from filename
    archiveClassifier.set("")  // no "-all" suffix
    mergeServiceFiles()        // handle service loader files in META-INF
    minimize()                 // shrink the jar by removing unused classes
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = libs.versions.java.get()
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}
