import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    application
    id("com.gradleup.shadow")
}

application {
    mainClass.set("com.zenyte.discord.MainKt")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation("io.github.classgraph:classgraph:4.8.179")
    implementation("net.dv8tion:JDA:5.0.0-beta.24")
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("discord")
    archiveVersion.set("")
    archiveClassifier.set("")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
    }
}
