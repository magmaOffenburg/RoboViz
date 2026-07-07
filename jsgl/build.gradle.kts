val joglVersion = rootProject.extra["joglVersion"] as String
val log4jVersion = rootProject.extra["log4jVersion"] as String

dependencies {
    implementation("org.jogamp.gluegen:gluegen-rt-main:$joglVersion")
    implementation("org.jogamp.jogl:jogl-all-main:$joglVersion")
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
}

group = "magmaOffenburg"
description = "jsgl"
