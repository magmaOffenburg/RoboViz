val joglVersion: String by rootProject.extra
val log4jVersion: String by rootProject.extra

dependencies {
    implementation("org.jogamp.gluegen:gluegen-rt-main:$joglVersion")
    implementation("org.jogamp.jogl:jogl-all-main:$joglVersion")
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
}

group = "magmaOffenburg"
description = "jsgl"
