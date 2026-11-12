plugins {
    id("org.jetbrains.kotlin.jvm")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    api(project(":core:lyrics"))
    implementation(project(":provider:matching"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("matchingBenchmark") {
    group = "verification"
    description = "Runs the opt-in synthetic selection CPU benchmark."
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("io.github.whoxamxl.aalyrics.provider.selection.MatchingBenchmark")
    maxHeapSize = "256m"
}
