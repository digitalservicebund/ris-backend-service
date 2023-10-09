package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import de.bund.digitalservice.ris.caselaw.adapter.S3AsyncMockClient
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByGuidOutputPort
import de.bund.digitalservice.ris.norms.application.service.ImportNormService
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.NormsService
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.PostgresTestcontainerIntegrationTest
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.NormDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.juris.JurisConverter
import de.bund.digitalservice.ris.norms.framework.adapter.output.s3.FilesService
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.time.Duration
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.AutoConfigureDataR2dbc
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.dialect.PostgresDialect
import org.springframework.http.MediaType
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult

@ExtendWith(SpringExtension::class)
@Import(
    FlywayConfig::class,
    ImportNormService::class,
    JurisConverter::class,
    FilesService::class,
    S3AsyncMockClient::class,
    NormsService::class,
)
@TestPropertySource(properties = ["local.file-storage=.local-storage"])
@WebFluxTest(controllers = [ImportNormController::class])
@WithMockUser
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureDataR2dbc
class ImportNormControllerIntegrationTest : PostgresTestcontainerIntegrationTest() {
  @Autowired lateinit var webClient: WebTestClient

  @Autowired private lateinit var client: DatabaseClient

  @Autowired lateinit var normsService: NormsService

  private lateinit var template: R2dbcEntityTemplate

  @BeforeAll
  fun setup() {
    template = R2dbcEntityTemplate(client, PostgresDialect.INSTANCE)
  }

  @AfterEach
  fun cleanUp() {
    template.delete(NormDto::class.java).all().block(Duration.ofSeconds(1))
  }

  private fun download(url: URL, path: Path, token: String) {
    val connection = url.openConnection()
    connection.setRequestProperty("Authorization", "Bearer $token")
    connection.connect()

    try {
      BufferedInputStream(connection.getInputStream()).use { inputStream ->
        BufferedOutputStream(Files.newOutputStream(path, StandardOpenOption.CREATE)).use {
            outputStream ->
          val data = ByteArray(1024)
          var bytesRead: Int
          while (inputStream.read(data, 0, data.size).also { bytesRead = it } != -1) {
            outputStream.write(data, 0, bytesRead)
          }
        }
      }
    } finally {
      (connection as? java.net.HttpURLConnection)?.disconnect()
    }
  }

  fun getLocalJurisTestFileFolderPath(versionTag: String): String {
    val tmpDir = System.getProperty("java.io.tmpdir")
    val folderPath = Paths.get(tmpDir, "ris-norms_juris-test-files", versionTag)

    if (Files.notExists(folderPath)) {
      Files.createDirectories(folderPath)
    }

    return folderPath.toString()
  }

  private fun loadJurisTestFile(fileName: String): ByteArray {
    val VERSION_TAG = "v0.15.0"
    val folderPath = getLocalJurisTestFileFolderPath(VERSION_TAG)
    val filePath = Paths.get(folderPath, fileName)

    if (Files.notExists(filePath)) {
      val password = System.getenv("GH_PACKAGES_REPOSITORY_TOKEN")
      val remoteUrl =
          URL(
              "https://raw.githubusercontent.com/digitalservicebund/ris-norms-juris-converter/${VERSION_TAG}/src/test/resources/juris/$fileName")
      download(remoteUrl, filePath, password)
    }

    val fileContent = Files.readAllBytes(filePath)
    return fileContent
  }

  @Test
  fun `it correctly imports a juris norms zip`() {
    val fileName = "Tierarznei.3-0_multi.zip"
    val jurisNorm = loadJurisTestFile(fileName)

    val response =
        webClient
            .mutateWith(SecurityMockServerConfigurers.csrf())
            .post()
            .uri("/api/v1/norms")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .bodyValue(jurisNorm)
            .exchange()
            .expectStatus()
            .isCreated
            .returnResult<ImportNormController.ResponseSchema>()

    val guidString = response.responseBody.map { it.guid }.blockFirst()
    val guid = UUID.fromString(guidString)

    val norm = normsService.getNormByGuid(GetNormByGuidOutputPort.Query(guid)).block()
    assertThat(norm?.guid).isEqualTo(guid)

    assertThat(
            norm?.documentation?.any { article ->
              article.marker == "Art 1" &&
                  article.heading ==
                      "Verordnung über die Verwendung antibiotisch wirksamer Arzneimittel"
            })
        .isTrue()
    assertThat(
            norm?.documentation?.any { article ->
              article.marker == "Art 2" &&
                  article.heading ==
                      "Änderung der Verordnung über Stoffe mit pharmakologischer Wirkung"
            })
        .isTrue()
    assertThat(
            norm?.documentation?.any { article ->
              article.marker == "Art 3" && article.heading == "Inkrafttreten, Außerkrafttreten"
            })
        .isTrue()

    if (norm != null) {
      assertThat(
              norm
                  .getFirstMetadatum(MetadataSectionName.NORM, MetadatumType.OFFICIAL_LONG_TITLE)
                  ?.value)
          .isEqualTo("Verordnung zur Anpassung von Rechtsverordnungen an das Tierarzneimittelrecht")
      assertThat(
              norm
                  .getFirstMetadatum(MetadataSectionName.NORM_PROVIDER, MetadatumType.ENTITY)
                  ?.value)
          .isEqualTo("DEU")
      assertThat(
              norm
                  .getFirstMetadatum(MetadataSectionName.NORM_PROVIDER, MetadatumType.DECIDING_BODY)
                  ?.value)
          .isEqualTo("BT")
      assertThat(
              norm
                  .getFirstMetadatum(
                      MetadataSectionName.NORM_PROVIDER, MetadatumType.RESOLUTION_MAJORITY)
                  ?.value)
          .isEqualTo(true)
    }
  }
}
