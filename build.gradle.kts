plugins {
    java
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "io.github.pellse"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
}

extra["springCloudVersion"] = "2024.0.1"
extra["springAiVersion"] = "1.0.0-M7"

dependencies {
    implementation("org.jspecify:jspecify:1.0.0")
    implementation("io.github.pellse:assembler:0.7.10")
    implementation("io.github.pellse:assembler-cache-caffeine:0.7.10")
    implementation ("com.github.ben-manes.caffeine:caffeine:3.2.0")
    implementation("com.tailrocks.graphql:graphql-datetime-spring-boot-starter:6.0.0")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-graphql")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.cloud:spring-cloud-stream")
    implementation("org.springframework.cloud:spring-cloud-stream-binder-kafka-reactive")
    implementation("org.springframework.cloud:spring-cloud-starter-consul-discovery")
    implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.ai:spring-ai-starter-model-openai")
    testImplementation("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.postgresql:r2dbc-postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.graphql:spring-graphql-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:r2dbc:1.20.6")
    testImplementation("org.testcontainers:postgresql:1.20.6")
    testImplementation("org.testcontainers:mongodb:1.20.6")
    testImplementation("org.testcontainers:kafka:1.20.6")
    testImplementation("org.testcontainers:consul:1.20.6")
    testImplementation("org.testcontainers:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
        mavenBom("org.springframework.ai:spring-ai-bom:${project.extra["springAiVersion"]}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
