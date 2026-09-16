plugins {
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.spring") version "2.1.10"
    groovy
}

group = "pl.joblin"
version = "0.1.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

val victoolsVersion = "4.37.0"

sourceSets {
    create("schemaGenerator") {
        kotlin {
            srcDir("src/schemaGenerator/kotlin")
        }
    }
}

configurations {
    named("schemaGeneratorImplementation") {
        extendsFrom(configurations.implementation.get())
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.retry:spring-retry")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.networknt:json-schema-validator:1.5.6")

    "schemaGeneratorImplementation"("com.github.victools:jsonschema-generator:$victoolsVersion")
    "schemaGeneratorImplementation"("com.github.victools:jsonschema-module-jackson:$victoolsVersion")
    "schemaGeneratorImplementation"("com.github.victools:jsonschema-module-jakarta-validation:$victoolsVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.spockframework:spock-core:2.4-M5-groovy-4.0")
    testImplementation("org.spockframework:spock-spring:2.4-M5-groovy-4.0")
    testImplementation("org.apache.groovy:groovy:4.0.24")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val compileKotlinTask = tasks.named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileKotlin")
val compileSchemaGeneratorKotlin = tasks.named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileSchemaGeneratorKotlin")

compileSchemaGeneratorKotlin.configure {
    dependsOn(compileKotlinTask)
    libraries.from(configurations["schemaGeneratorCompileClasspath"])
    libraries.from(compileKotlinTask.map { it.destinationDirectory })
}

val generateOfferIngestSchema by tasks.registering(JavaExec::class) {
    group = "build"
    description = "Generate offer ingest JSON Schema from Kotlin data classes"
    dependsOn(compileSchemaGeneratorKotlin)
    val outDir = layout.buildDirectory.dir("generated/resources/META-INF/joblin")
    inputs.files(compileKotlinTask.map { it.destinationDirectory })
    inputs.files(compileSchemaGeneratorKotlin.map { it.destinationDirectory })
    outputs.dir(outDir)
    classpath = sourceSets["schemaGenerator"].runtimeClasspath +
        files(compileKotlinTask.map { it.destinationDirectory })
    mainClass.set("pl.joblin.schema.GenerateOfferIngestSchemaKt")
    argumentProviders.add {
        listOf(outDir.get().asFile.absolutePath)
    }
}

tasks.named<ProcessResources>("processResources") {
    dependsOn(generateOfferIngestSchema)
    from(layout.buildDirectory.dir("generated/resources"))
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    dependsOn(generateOfferIngestSchema)
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("joblin.jar")
    dependsOn(generateOfferIngestSchema)
}
