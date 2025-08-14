import com.google.protobuf.gradle.id
plugins {
    id("org.jetbrains.kotlin.jvm") version "2.2.10-RC2"
    id("org.jetbrains.kotlin.plugin.allopen") version "2.2.10-RC2"
    id("com.google.devtools.ksp") version "2.2.10-RC2-2.0.2"
    id("com.gradleup.shadow") version "8.3.8"
    id("io.micronaut.application") version "4.5.4"
    id("com.google.protobuf") version "0.9.5"
    id("org.jlleitschuh.gradle.ktlint") version "13.0.0"
}

version = "0.1"
group = "com.example"

val kotlinVersion = project.properties.get("kotlinVersion")
repositories {
    mavenCentral()
}

dependencies {
    ksp("io.micronaut.validation:micronaut-validation-processor")
    ksp("io.micronaut.serde:micronaut-serde-processor")
    implementation("io.micronaut:micronaut-discovery-core")
    implementation("io.micronaut.grpc:micronaut-grpc-runtime")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("javax.annotation:javax.annotation-api")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
}

application {
    mainClass.set("com.example.ApplicationKt")
}
java {
    sourceCompatibility = JavaVersion.toVersion("21")
}

sourceSets {
    main {
        java {
            srcDirs("build/generated/source/proto/main/grpc")
            srcDirs("build/generated/source/proto/main/java")
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.2"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.61.0"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                // Apply the "grpc" plugin whose spec is defined above, without options.
                id("grpc")
            }
        }
    }
}
micronaut {
    testRuntime("kotest5")
    processing {
        incremental(true)
        annotations("com.example.*")
    }
}

tasks.named<io.micronaut.gradle.docker.MicronautDockerfile>("dockerfile") {
    baseImage("eclipse-temurin:21-jre-jammy")
}

tasks.named<io.micronaut.gradle.docker.NativeImageDockerfile>("dockerfileNative") {
    jdkVersion.set("21")
}
configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    verbose.set(true)
}

ktlint {
    filter {
        exclude { elem ->
            elem.file.path.contains(
                layout.buildDirectory
                    .dir("generated")
                    .get()
                    .toString(),
            )
        }
        include("**/kotlin/**")
    }
}
ktlint {
    version.set("1.7.1")
}
