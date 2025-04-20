plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.4.3"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "dev.gordeev.review"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

extra["springAiVersion"] = "1.0.0-M6"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-quartz")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("com.squareup.okhttp3:okhttp:4.12.0")
	implementation("com.google.code.gson:gson:2.10.1")

	// GIT
	implementation("org.eclipse.jgit:org.eclipse.jgit:6.7.0.202309050840-r")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.flywaydb:flyway-core")
	implementation("org.postgresql:postgresql")

	implementation("org.springframework.ai:spring-ai-ollama-spring-boot-starter")
//	implementation("org.springframework.ai:spring-ai-vertex-ai-gemini-spring-boot-starter")
//	developmentOnly("org.springframework.boot:spring-boot-docker-compose")
//	developmentOnly("org.springframework.ai:spring-ai-spring-boot-docker-compose")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
	}
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// Docker image configuration
tasks.bootBuildImage {
	imageName.set("${System.getenv("DOCKER_USERNAME") ?: "username"}/code-review-server:${project.version}")

	// Optional: customize the build
	environment.set(mapOf(
		"BP_JVM_VERSION" to "21.*"
	))

	docker {
		publishRegistry {
			username.set(System.getenv("DOCKER_USERNAME") ?: "username")
			password.set(System.getenv("DOCKER_PASSWORD") ?: "password")
		}
	}
}
