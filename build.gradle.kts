plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.hibernate.orm") version "6.1.1.Final"
    id("org.graalvm.buildtools.native") version "0.10.4"
    kotlin("plugin.jpa") version "1.9.25"
    kotlin("kapt") version "1.9.25"
    id("org.springdoc.openapi-gradle-plugin") version "1.9.0"
    id("org.asciidoctor.jvm.convert") version "3.3.2"
    id("net.bytebuddy.byte-buddy-gradle-plugin") version "1.15.11"
}

group = "cz.ememsoft"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.liquibase:liquibase-core")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    implementation("org.mapstruct:mapstruct:1.6.3")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.3") {
        exclude("jakarta.xml.bind", "jakarta.xml.bind-api")
        exclude("com.fasterxml.jackson.core", "jackson-annotations")
    }
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    implementation("org.hibernate.orm:hibernate-core:6.1.1.Final")
    kapt("org.mapstruct:mapstruct-processor:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kapt {
    keepJavacAnnotationProcessors = true
    arguments {
        arg("mapstruct.defaultComponentModel", "spring")
        arg("mapstruct.unmappedTargetPolicy", "IGNORE")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

hibernate {
    enhancement {
        enableAssociationManagement = true
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

graalvmNative {
    metadataRepository {
//        version = "0.3.15"
        enabled = false
    }
    agent {
        defaultMode = "standard" // Default agent mode if one isn't specified using `-Pagent=mode_name`
        enabled = true // Enables the agent
        builtinCallerFilter = true
        builtinHeuristicFilter = true
        enableExperimentalPredefinedClasses = false
        enableExperimentalUnsafeAllocationTracing = false
        trackReflectionMetadata = true
    }
    binaries {
        all {
            buildArgs.add("-H:+UnlockExperimentalVMOptions")
            buildArgs.add("-H:+AllowIncompleteClasspath")
            buildArgs.add("-H:-SupportPredefinedClasses")
            buildArgs.add("-H:+BuildReport")

            resources.autodetect()
            quickBuild = false
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
