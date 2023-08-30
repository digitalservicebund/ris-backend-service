package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import de.bund.digitalservice.ris.OpenApiConfiguration
import de.bund.digitalservice.ris.exceptions.exception.NotFoundWithInstanceException
import de.bund.digitalservice.ris.norms.application.port.input.LoadNormAsXmlUseCase
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.ApiConfiguration
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import java.net.URI
import org.springframework.http.MediaType.APPLICATION_XML
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping(ApiConfiguration.API_BASE_PATH)
@Tag(name = OpenApiConfiguration.NORMS_TAG)
class LoadNormAsXmlController(private val loadNormAsXmlService: LoadNormAsXmlUseCase) {

  @GetMapping(
      path =
          [
              "/norms/xml/eli/{printAnnouncementGazette}/{announcementYear}/s{printAnnouncementPage}",
              "/open/norms/xml/eli/{printAnnouncementGazette}/{announcementYear}/s{printAnnouncementPage}",
          ],
  )
  @Operation(
      summary = "Load a single as XML in LegalDocML.de format by eli",
      description = "Retrieves a single norm in LegalDocML.de xml format using its ELI")
  @ApiResponses(
      ApiResponse(responseCode = "200", description = "Norm was found"),
      ApiResponse(responseCode = "404", description = "No norm found for this query"),
  )
  fun loadNormAsXml(
      @Parameter(
          name = "printAnnouncementGazette",
          description = "First part of the ELI, the gazette of the print announcement version",
          required = true,
          example = "bgbl-1")
      @PathVariable
      printAnnouncementGazette: String,
      @Parameter(
          name = "announcementYear",
          description = "Second part of the ELI, the publication year",
          required = true,
          example = "1976")
      @PathVariable
      announcementYear: String,
      @Parameter(
          name = "printAnnouncementPage",
          description = "Third part of the ELI, the page of the print announcement version",
          required = true,
          example = "3341")
      @PathVariable
      printAnnouncementPage: String,
  ): Mono<ResponseEntity<String>> {
    val query =
        LoadNormAsXmlUseCase.Query(
            printAnnouncementGazette, announcementYear, printAnnouncementPage)

    return loadNormAsXmlService
        .loadNormAsXml(query)
        .map { normAsXml -> ResponseEntity.ok().contentType(APPLICATION_XML).body(normAsXml) }
        .switchIfEmpty(
            Mono.error(
                NotFoundWithInstanceException(
                    URI(
                        encodeUri(
                            "gazette/${query.printAnnouncementGazette}/year/${query.announcementOrCitationYear}/page/${query.printAnnouncementPage}")),
                ),
            ),
        )
  }

  private fun encodeUri(component: String): String {
    return component.replace(" ", "%20").replace("(", "%28").replace(")", "%29")
  }
}
