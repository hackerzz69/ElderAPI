import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.gradleup.shadow")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(libs.okhttp)
    implementation(libs.commons.text)
    implementation(libs.jsoup)
    implementation(libs.gson)
    implementation(libs.lettuce)
    implementation(libs.kotlin.logging)
}

tasks.register<ShadowJar>("modelJar") {
    minimize()
    archiveBaseName.set("api-model")
    from(sourceSets.main.get().output) {
        include("com/zenyte/api/model/**")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
        freeCompilerArgs = listOf("-Xinline-classes")
    }
}
