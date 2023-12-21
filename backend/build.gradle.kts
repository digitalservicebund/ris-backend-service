import com.adarshr.gradle.testlogger.theme.ThemeType
import com.github.jk1.license.filter.DependencyFilter
import com.github.jk1.license.filter.LicenseBundleNormalizer
import com.github.jk1.license.render.CsvReportRenderer
import com.github.jk1.license.render.ReportRenderer
import io.franzbecker.gradle.lombok.task.DelombokTask
import org.flywaydb.gradle.task.FlywayMigrateTask
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    java
    jacoco
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.spring") version "1.9.22"
    id("org.springframework.boot") version "3.1.5"
    id("io.spring.dependency-management") version "1.1.0"
    id("com.diffplug.spotless") version "6.22.0"
    id("org.sonarqube") version "4.4.1.3373"
    id("com.github.jk1.dependency-license-report") version "2.5"
    id("com.gorylenko.gradle-git-properties") version "2.4.1"
    id("com.adarshr.test-logger") version "4.0.0"
    id("com.github.ben-manes.versions") version "0.50.0"
    id("org.jetbrains.dokka") version "1.9.10"
    id("io.franzbecker.gradle-lombok") version "5.0.0"
    id("org.flywaydb.flyway") version "10.1.0"
}

group = "de.bund.digitalservice"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

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
    maven {
        url = uri("https://maven.pkg.github.com/digitalservicebund/ris-norms-juris-converter")
        credentials {
            username = System.getenv("GH_PACKAGES_REPOSITORY_USER")
            password = System.getenv("GH_PACKAGES_REPOSITORY_TOKEN")
        }
    }
}

sourceSets {
    create("migration") {
        java.srcDir("src/main/java")
        java.include("db/migration/**/*.java")
    }
}

jacoco {
    toolVersion = "0.8.8"
}

lombok {
    version = "1.18.26"
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
    kotlin { ktfmt() }
    java {
        removeUnusedImports()
        googleJavaFormat()
        custom("Refuse wildcard imports") {
            // Wildcard imports can't be resolved by spotless itself.
            // This will require the developer themselves to adhere to best practices.
            if (it.contains("\nimport .*\\*;".toRegex())) {
                throw AssertionError("Do not use wildcard imports. 'spotlessApply' cannot resolve this issue.")
            }
            it
        }
    }
    format("misc") {
        target("**/*.js", "**/*.json", "**/*.md", "**/*.properties", "**/*.sh", "**/*.sql", "**/*.yaml", "**/*.yml")
        targetExclude("frontend/**", "**/dist/**", "**/static/**")
        // spotless:off
        prettier(
            mapOf(
                "prettier" to "2.8.4",
                "prettier-plugin-properties" to "0.2.0",
                "prettier-plugin-sh" to "0.12.8",
                "prettier-plugin-sql" to "0.13.0"
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
    filters = arrayOf<DependencyFilter>(LicenseBundleNormalizer("$projectDir/license-normalizer-bundle.json", true))
}

sonar {
    properties {
        property("sonar.projectKey", "digitalservicebund_ris-backend-service")
        property("sonar.organization", "digitalservicebund")
        property("sonar.host.url", "https://sonarcloud.io")
        property(
            "sonar.coverage.exclusions",
            "**/src/fields/**,**/config/**,**/S3AsyncMockClient.java,**/Application.java,**/NormsMemoryRepository.kt"
        )
    }
}

dependencies {
    val springSecurityVersion = "6.1.2"
    val springWebVersion = "6.0.11"
    // CVE-2023-6481
    val logbackVersion = "1.4.14"
    val r2dbcVersion = "1.0.0.RELEASE"
    val jacksonModuleVersion = "2.15.2"
    val testContainersVersion = "1.19.0"

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    // => CVE-2023-34035, CVE-2023-34034
    implementation("org.springframework.security:spring-security-web:$springSecurityVersion")
    implementation("org.springframework.security:spring-security-config:$springSecurityVersion")
    // => CVE-2023-34034
    implementation("org.springframework.security:spring-security-core:$springSecurityVersion")
    implementation("org.springframework.security:spring-security-oauth2-resource-server:$springSecurityVersion")
    implementation("org.springframework:spring-web:$springWebVersion")
    implementation("org.springframework:spring-webflux:$springWebVersion")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.2.0")
    implementation("org.springframework.boot:spring-boot-starter-webflux") {
        exclude(group = "io.netty", module = "netty-tcnative-classes")
        because("CVE-2021-43797, not using Tomcat")
    }
    implementation("org.springframework.cloud:spring-cloud-starter-kubernetes-client-config:2.1.3")
    // CVE-2023-31582
    implementation("org.bitbucket.b_c:jose4j:0.9.3")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.session:spring-session-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // => CVE-2023-1370
    implementation("net.minidev:json-smart:2.5.0")
    // CVE-2022-3171
    implementation("com.google.protobuf:protobuf-java:3.25.0")
    // => CVE-2021-37136, CVE-2021-37137, CVE-2021-43797
    implementation("io.netty:netty-all:4.1.100.Final") {
        exclude(group = "io.netty", module = "netty-tcnative-classes")
        because("CVE-2021-43797, not using Tomcat")
    }
    implementation("io.projectreactor.netty:reactor-netty-core:1.1.8")
    implementation("org.yaml:snakeyaml:2.2")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("ch.qos.logback:logback-core:$logbackVersion")
    implementation("org.postgresql:r2dbc-postgresql:1.0.1.RELEASE")
    implementation("io.r2dbc:r2dbc-spi:$r2dbcVersion")
    implementation("io.r2dbc:r2dbc-pool:$r2dbcVersion")
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("com.sendinblue:sib-api-v3-sdk:7.0.0")
    // CVE-2022-4244
    implementation("org.codehaus.plexus:plexus-utils:4.0.0")
    implementation(platform("software.amazon.awssdk:bom:2.21.14"))
    implementation("software.amazon.awssdk:netty-nio-client")
    implementation("software.amazon.awssdk:s3")
    // CVE-2022-42004, CVE-2022-42003
    implementation("com.fasterxml.jackson:jackson-bom:$jacksonModuleVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonModuleVersion")
    implementation("com.github.spullara.mustache.java:compiler:0.9.10")
    // CVE-2022-40153
    implementation("com.fasterxml.woodstox:woodstox-core:6.5.0")
    implementation("org.docx4j:docx4j-JAXB-MOXy:11.4.9")
    implementation("jakarta.mail:jakarta.mail-api:2.0.1")
    implementation("com.sun.activation:jakarta.activation:2.0.1")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api") {
        version {
            strictly("3.0.1")
        }
    }
    implementation("org.freehep:freehep-graphicsio-emf:2.4")
    // package served by private repo, requires authentication:
    implementation("de.bund.digitalservice:neuris-juris-xml-export:0.8.14") {
        exclude(group = "org.slf4j", module = "slf4j-simple")
    }
    // for local development:
    // implementation(files("../../neuris-juris-xml-export/build/libs/neuris-juris-xml-export-0.8.13.jar"))
    implementation("com.icegreen:greenmail:2.0.0")
    implementation("de.bund.digitalservice:ris-norms-juris-converter:0.19.2")
    // for local development:
    // implementation(files("ris-norms-juris-converter-0.19.2.jar"))
    // implementation("org.apache.commons:commons-text:1.10.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.15.2")
    implementation("io.micrometer:micrometer-registry-prometheus:1.11.2")
    implementation("io.micrometer:micrometer-core:1.11.4")
    implementation(platform("io.sentry:sentry-bom:6.33.1"))
    implementation("io.sentry:sentry-spring-boot-starter-jakarta")
    implementation("io.sentry:sentry-logback")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    // => CVE-2023-20883
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:20220608.1")
    // => CVE-2023-2976
    implementation("com.google.guava:guava:32.1.3-jre")
    // Manually updating to 1.1.13 because parents already latest version (CVE-2023-34062)
    implementation("io.projectreactor.netty:reactor-netty-http:1.1.13")
    var flywayCore = "org.flywaydb:flyway-core:9.22.2"
    implementation(flywayCore)
    "migrationImplementation"(flywayCore)
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jsoup:jsoup:1.16.1")
    implementation("io.getunleash:unleash-client-java:9.1.0")

    testImplementation("com.ninja-squad:springmockk:4.0.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.mockito", module = "mockito-core")
    }
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("io.projectreactor:reactor-test:3.5.3")
    // => CVE-2023-34034
    testImplementation("org.springframework.security:spring-security-test:6.1.2")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.2.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.7.0")
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testImplementation("org.testcontainers:postgresql:$testContainersVersion")
    testImplementation("org.jeasy:easy-random-core:5.0.0")
    testImplementation("com.google.code.gson:gson:2.10.1")

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
            "filesystem:src/main/resources/db/migration/",
            "classpath:db/migration"
        )
        dependsOn("compileMigrationJava")
    }

    jar {
        enabled = false
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
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
        dependsOn("integrationTest")
        executionData(fileTree(project.buildDir.absolutePath).include("jacoco/*.exec"))
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

    getByName<BootBuildImage>("bootBuildImage") {
        val containerRegistry = System.getenv("CONTAINER_REGISTRY") ?: "ghcr.io"
        val containerImageName = System.getenv("CONTAINER_IMAGE_NAME") ?: "digitalservicebund/${rootProject.name}"
        val containerImageVersion = System.getenv("CONTAINER_IMAGE_VERSION") ?: "latest"

        imageName.set("${containerRegistry}/${containerImageName}:${containerImageVersion}")
        builder.set("paketobuildpacks/builder-jammy-tiny@sha256:61b59d061af9dbb117952dbc916dc2e0af87fd2e8b5ee24ff1573a1e3fffe0aa")
        publish.set(false)
        docker {
            publishRegistry {
                username.set(System.getenv("CONTAINER_REGISTRY_USER") ?: "")
                password.set(System.getenv("CONTAINER_REGISTRY_PASSWORD") ?: "")
                url.set("https://${containerRegistry}")
            }
        }
    }

    val delombok by registering(DelombokTask::class) {
        dependsOn(compileJava)
        mainClass.set("lombok.launch.Main")
        val outputDir by extra { file("$buildDir/delombok") }
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

    withType<DokkaTask> {
        dokkaSourceSets.configureEach {
            perPackageOption {
                matchingRegex.set(".*caselaw.*")
                suppress.set(true)
            }
        }
    }
}
