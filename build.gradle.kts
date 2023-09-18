import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("jvm") version "1.9.10"
}

group = "org.magmaoffenburg.roboviz"
version = "1.8.5"
application {
    mainClass.set("org.magmaoffenburg.roboviz.MainKt")
    applicationDefaultJvmArgs = listOf("--add-exports=java.desktop/sun.awt=ALL-UNNAMED")
}

val javaVersion by extra(17)
val joglVersion by extra("2.4.0")
val log4jVersion by extra("2.20.0")

subprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
        maven(url = "https://www.jogamp.org/deployment/maven/")
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersion))
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}

kotlin {
    jvmToolchain(javaVersion)
}

repositories {
    mavenCentral()
    maven(url = "https://www.jogamp.org/deployment/maven/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.github.weisj:darklaf-core:3.0.2")
    implementation("org.jogamp.jogl:jogl-all-main:$joglVersion")
    implementation("org.jogamp.gluegen:gluegen-rt-main:$joglVersion")
    implementation("org.apache.commons:commons-compress:1.24.0")
    implementation("org.apache.logging.log4j:log4j-api-kotlin:1.2.0")
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")

    implementation(project(":jsgl"))
}

tasks.jar {
    manifest {
        // We need to set Multi-Release to true so that log4j can determine the correct class names
        attributes("Multi-Release" to "true")
    }
}

tasks.withType<ShadowJar> {
    archiveFileName.set("${project.name}.jar")
}

tasks.register<Copy>("binDir") {
    dependsOn(tasks.withType<ShadowJar>())

    val binPath = layout.projectDirectory.dir("bin")
    from(
        layout.buildDirectory.file("libs/${project.name}.jar"),
        layout.projectDirectory.file("config.txt"),
        layout.projectDirectory.file("scripts/roboviz.sh"),
        layout.projectDirectory.file("scripts/roboviz.bat"),
        layout.projectDirectory.file("LICENSE.md"),
        layout.projectDirectory.file("NOTICE.md"),
        layout.projectDirectory.file("CHANGELOG.md")
    )
    into(binPath)
}

// Creates an application bundle for macOS
tasks.register("macOSApp") {
    dependsOn(tasks.withType<ShadowJar>())

    doLast {
        copy {
            from(layout.projectDirectory.file("macos/Info.plist"))
            into(layout.buildDirectory.dir("macos/${project.name}.app/Contents"))
            filter(ReplaceTokens::class, "tokens" to mapOf(
                "CFBundleExecutable" to "launcher",
                "CFBundleIconFile" to "icon",
                "CFBundleIdentifier" to group.toString(),
                "CFBundleName" to project.name,
                "CFBundleShortVersionString" to version.toString(),
                "CFBundleVersion" to version.toString(),
            ))
        }
        copy {
            from(layout.projectDirectory.file("scripts/roboviz.sh"))
            into(layout.buildDirectory.dir("macos/${project.name}.app/Contents/MacOS"))
            rename(".*", "launcher")
        }
        copy {
            from(layout.projectDirectory.file("macos/icon.icns"))
            into(layout.buildDirectory.dir("macos/${project.name}.app/Contents/Resources"))
        }
        copy {
            from(layout.buildDirectory.file("libs/${project.name}.jar"))
            into(layout.buildDirectory.dir("macos/${project.name}.app/Contents/MacOS"))
        }
    }
}
