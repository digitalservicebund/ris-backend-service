import com.adarshr.gradle.testlogger.theme.ThemeType
import com.diffplug.spotless.FormatterFunc
import com.github.jk1.license.filter.DependencyFilter
import com.github.jk1.license.filter.LicenseBundleNormalizer
import com.github.jk1.license.render.CsvReportRenderer
import com.github.jk1.license.render.ReportRenderer
import io.franzbecker.gradle.lombok.task.DelombokTask
import org.flywaydb.gradle.task.FlywayMigrateTask
import java.io.Serializable

plugins {
    java
    jacoco
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "7.0.2"
    id("org.sonarqube") version "6.0.1.5171"
    id("com.github.jk1.dependency-license-report") version "2.9"
    id("com.gorylenko.gradle-git-properties") version "2.4.2"
    id("com.adarshr.test-logger") version "4.0.0"
    id("se.patrikerdes.use-latest-versions") version "0.2.18"
    id("com.github.ben-manes.versions") version "0.52.0"
    id("io.franzbecker.gradle-lombok") version "5.0.0"
    id("org.flywaydb.flyway") version "11.3.3"
    id("io.sentry.jvm.gradle") version "5.2.0"
}

group = "de.bund.digitalservice"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
    maven {
        setUrl("https://plugins.gradle.org/m2/")
    }
    maven {
        url = uri("https://maven.pkg.github.com/digitalservicebund/neuris-juris-xml-export")
        credentials {
            username = System.getenv("GH_PACKAGES_REPOSITORY_USER")
            password = System.getenv("GH_PACKAGES_REPOSITORY_TOKEN")
        }
    }
    //  mavenLocal() // for local testing of library when publishing to maven local.
}

sourceSets {
    create("migration") {
        java.srcDir("src/main/java")
        java.include("db/migration/**/*.java")
    }
}

jacoco {
    toolVersion = "0.8.12"
}

lombok {
    version = "1.18.36"
}

springBoot {
    buildInfo()
}

testlogger {
    theme = ThemeType.MOCHA
}

configurations {
    compileOnly {
        extendsFrom(annotationProcessor.get())
    }
}

spotless {
    java {
        removeUnusedImports()
        googleJavaFormat()
        // Wildcard imports can't be resolved by spotless itself.
        custom("Refuse wildcard imports", object : Serializable, FormatterFunc {
            override fun apply(input: String) : String {
                if (input.contains("\nimport .*\\*;".toRegex())) {
                    throw GradleException("No wildcard imports allowed.")
                }
                return input
            }
        })
    }
    format("misc") {

        target(
            "**/*.js",
            "**/*.json",
            "**/*.md",
            "**/*.properties",
            "**/*.sh",
            "**/*.sql",
            "**/*.yaml",
            "**/*.yml"
        )

        targetExclude(
            "frontend/**",
            "**/?/**",
            "**/*.sql",
            "**/dist/**",
            "**/static/**",
            "**/gradle.properties",
            "**/gradle-wrapper.properties",
            "**/jaxb.properties",
            "**/sentry-debug-meta.properties"
        )
        // spotless:off
        prettier(
            mapOf(
                "prettier" to "3.3.3",
                "prettier-plugin-properties" to "0.3.0",
                "prettier-plugin-sh" to "0.14.0",
                "prettier-plugin-sql" to "0.18.0"
            )
        ).config(
            mapOf(
                "keySeparator" to "=", // for prettier-plugin-properties
                "language" to "postgresql" // for prettier-plugin-sql
            )
        )
        // spotless:on
    }
}

licenseReport {
    allowedLicensesFile = File("$projectDir/../allowed-licenses.json")
    renderers = arrayOf<ReportRenderer>(CsvReportRenderer("backend-licence-report.csv"))
    filters = arrayOf<DependencyFilter>(LicenseBundleNormalizer())
}

sonar {
    properties {
        property("sonar.projectKey", "digitalservicebund_ris-backend-service")
        property("sonar.organization", "digitalservicebund")
        property("sonar.host.url", "https://sonarcloud.io")
        property(
            "sonar.coverage.exclusions",
            "**/config/**,**/S3AsyncMockClient.java,**/Application.java"
        )
    }
}

dependencies {
    val testContainersVersion = "1.20.5"

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework.session:spring-session-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client") {
        exclude(group = "net.minidev", module = "json-smart")
    }
    implementation("org.springframework.security:spring-security-oauth2-resource-server:6.4.3")

    implementation("org.springframework.cloud:spring-cloud-starter-kubernetes-client-config:3.2.0")

    // CVE-2022-3171
    implementation("com.google.protobuf:protobuf-java:4.29.3")

    // CVE-2024-57699
    implementation("net.minidev:json-smart:2.5.2")

    // CVE-2025-24970
    implementation("io.netty:netty-handler:4.1.119.Final")

    implementation("org.postgresql:postgresql:42.7.5")

    implementation("com.sendinblue:sib-api-v3-sdk:7.0.0")
    // CVE-2022-4244
    implementation("org.codehaus.plexus:plexus-utils:4.0.2")

    implementation(platform("software.amazon.awssdk:bom:2.29.51"))
    implementation("software.amazon.awssdk:netty-nio-client")
    implementation("software.amazon.awssdk:s3")

    implementation("org.docx4j:docx4j-JAXB-ReferenceImpl:11.5.2")
    implementation("org.freehep:freehep-graphicsio-emf:2.4")

    // caselaw tranformation to LDML for the communication with the portal
    implementation("org.eclipse.persistence:org.eclipse.persistence.moxy:4.0.5")
    implementation("net.sf.saxon:Saxon-HE:12.5")

    implementation("jakarta.mail:jakarta.mail-api:2.1.3")
    implementation("org.eclipse.angus:angus-mail:2.0.3")
    implementation("com.icegreen:greenmail:2.1.3")

    // package served by private repo, requires authentication:
    implementation("de.bund.digitalservice:neuris-juris-xml-export:0.10.29") {
        exclude(group = "org.slf4j", module = "slf4j-simple")
    }
    // for local development:
    // implementation(files("../../neuris-juris-xml-export/build/libs/neuris-juris-xml-export-0.10.28.jar"))
    // or with local gradle project (look also into settings.gradle.kts)
    // implementation(project(":exporter"))

    implementation("de.bund.digitalservice:neuris-caselaw-migration-schema:0.0.48")
    // for local development:
    // implementation(files("../../ris-data-migration/schema/build/libs/schema-0.0.45.jar"))

    implementation("com.fasterxml.jackson.core:jackson-core:2.18.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.18.2")

    implementation("com.gravity9:json-patch-path:2.0.2")

    implementation("io.micrometer:micrometer-registry-prometheus:1.14.4")
    implementation("io.micrometer:micrometer-core:1.14.4")

    implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:20240325.1")

    implementation("io.getunleash:unleash-client-java:9.3.2")
    implementation("org.apache.commons:commons-text:1.13.0")
    implementation("org.jsoup:jsoup:1.18.3")

    implementation("net.javacrumbs.shedlock:shedlock-spring:6.3.0")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:6.3.0")

    // CVE-2023-3635
    implementation("com.squareup.okio:okio-jvm:3.10.2")

    val flywayCore = "org.flywaydb:flyway-core:11.3.3"
    implementation(flywayCore)
    "migrationImplementation"(flywayCore)
    runtimeOnly("org.flywaydb:flyway-database-postgresql:11.3.3")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.mockito", module = "mockito-core")
    }
    testImplementation("org.mockito:mockito-junit-jupiter:5.15.2")
    testImplementation("org.mockito:mockito-inline:5.2.0")

    testImplementation("io.projectreactor:reactor-test:3.7.3")
    testImplementation("org.springframework.security:spring-security-test:6.4.3")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.4.0")
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testImplementation("org.testcontainers:postgresql:$testContainersVersion")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
}

project.tasks.sonar {
    dependsOn("jacocoTestReport")
}

tasks {
    register<FlywayMigrateTask>("migrateDatabaseForERD") {
        url = System.getenv("DB_URL")
        user = System.getenv("DB_USER")
        password = System.getenv("DB_PASSWORD")
        locations = arrayOf(
            "filesystem:src/main/resources/db/migration/", "classpath:db/migration"
        )
        dependsOn("compileMigrationJava")
    }

    jar {
        enabled = false
    }

    withType<Test> {
        maxParallelForks = Runtime.getRuntime().availableProcessors().div(2)
    }

    getByName<Test>("test") {
        useJUnitPlatform {
            excludeTags("integration", "manual")
        }
    }

    task<Test>("integrationTest") {
        description = "Runs the integration tests."
        group = "verification"
        useJUnitPlatform {
            includeTags("integration")
            excludeTags("manual")
        }

        // So that running integration test require running unit tests first,
        // and we won"t even attempt running integration tests when there are
        // failing unit tests.
        dependsOn("test")
        finalizedBy("jacocoTestReport")
    }

    check {
        dependsOn("integrationTest")
    }

    jacocoTestReport {
        // Jacoco hooks into all tasks of type: Test automatically, but results for each of these
        // tasks are kept separately and are not combined out of the box. we want to gather
        // coverage of our unit and integration tests as a single report!
        executionData.setFrom(
            files(
                fileTree(project.layout.buildDirectory) {
                    include("jacoco/*.exec")
                },
            ),
        )
        dependsOn("integrationTest")
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
        classDirectories.setFrom(
            files(classDirectories.files.map {
                fileTree(it) {
                    exclude("**/config/**")
                }
            })
        )
    }

    bootBuildImage {
        val containerRegistry = System.getenv("CONTAINER_REGISTRY") ?: "ghcr.io"
        val containerImageName =
            System.getenv("CONTAINER_IMAGE_NAME") ?: "digitalservicebund/${rootProject.name}"
        val containerImageVersion = System.getenv("CONTAINER_IMAGE_VERSION") ?: "latest"

        imageName.set("$containerRegistry/$containerImageName:$containerImageVersion")
        builder.set("paketobuildpacks/builder-jammy-tiny")
        publish.set(false)

        docker {
            publishRegistry {
                username.set(System.getenv("CONTAINER_REGISTRY_USER") ?: "")
                password.set(System.getenv("CONTAINER_REGISTRY_PASSWORD") ?: "")
                url.set("https://$containerRegistry")
            }
        }
    }

    val delombok by registering(DelombokTask::class) {
        dependsOn(compileJava)
        mainClass.set("lombok.launch.Main")
        val outputDir by extra { file("${project.layout.buildDirectory}/delombok") }
        outputs.dir(outputDir)
        sourceSets["main"].java.srcDirs.forEach {
            inputs.dir(it)
            args(it, "-d", outputDir)
        }
        doFirst {
            outputDir.delete()
        }
    }

    javadoc {
        dependsOn(delombok)
        val outputDir: File by delombok.get().extra
        source = fileTree(outputDir)
        isFailOnError = false
    }
}
