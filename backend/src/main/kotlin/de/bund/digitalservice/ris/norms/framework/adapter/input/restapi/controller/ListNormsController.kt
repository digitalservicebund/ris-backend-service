package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import de.bund.digitalservice.ris.norms.application.port.input.ListNormsUseCase
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.ApiConfiguration
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.OpenApiConfiguration
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.encodeEli
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.encodeGuid
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping(ApiConfiguration.API_BASE_PATH)
@Tag(name = OpenApiConfiguration.NORMS_TAG)
class ListNormsController(private val listNormsService: ListNormsUseCase) {

  @GetMapping(path = ["/norms", "/open/norms"])
  @Operation(
      summary = "Get a list of norms filtered by a query",
      description = "If no query is provided, all available norms are listed.")
  @ApiResponses(
      ApiResponse(responseCode = "200", description = "Successful response with list of norms"),
  )
  fun listNorms(
      @Parameter(
          name = "q",
          example = "Koordinierung der Systeme",
          description =
              "Searches for a substring in the following properties of a norm:\n" +
                  "  - officialLongTitle\n" +
                  "  - officialShortTitle\n" +
                  "  - unofficialLongTitle\n" +
                  "  - unofficialShortTitle\n" +
                  "\nThe search term is used as is without any postprocessing and is case sensitive.",
      )
      @RequestParam
      q: String?,
  ): Mono<ResponseEntity<PaginatedNormListResponseSchema>> {
    val query = ListNormsUseCase.Query(searchTerm = q)

    return listNormsService
        .listNorms(query)
        .collectList()
        .map { normDataList -> PaginatedNormListResponseSchema.fromUseCaseData(normDataList) }
        .map { paginationData -> ResponseEntity.ok(paginationData) }
  }

  data class PaginatedNormListResponseSchema
  private constructor(val data: List<NormDataResponseSchema>) {
    companion object {
      fun fromUseCaseData(data: List<ListNormsUseCase.NormData>): PaginatedNormListResponseSchema =
          PaginatedNormListResponseSchema(
              data.map { NormDataResponseSchema.fromUseCaseData(it) },
          )
    }
  }

  data class NormDataResponseSchema
  private constructor(val guid: String, val officialLongTitle: String, val eli: String) {
    companion object {
      fun fromUseCaseData(data: ListNormsUseCase.NormData) =
          NormDataResponseSchema(
              encodeGuid(data.guid),
              data.officialLongTitle,
              encodeEli(data.eli),
          )
    }
  }
}
