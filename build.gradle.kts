// Root build configuration for Symbolic Connection

plugins {
    id("com.android.application") version "8.2.2" apply false
    id("com.android.library") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

ext {
    set("compose_version", "1.5.8")
    set("material3_version", "1.2.0")
    set("kotlin_version", "1.9.10")
}

