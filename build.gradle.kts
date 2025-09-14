plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.shadow) apply false
    java
}

allprojects {
    group = "com.zenyte"
    version = "1.0"
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
        // Oracle repo required for mysql:mysql-connector-j:8.4.0
        maven("https://repo.mysql.com/")
    }
}

project(":discord") {
    dependencies {
        implementation(project(":common"))
    }
}

project(":api") {
    // only :api needs the fat jar
    apply(plugin = "com.gradleup.shadow")

    dependencies {
        implementation(project(":common"))
    }
}
