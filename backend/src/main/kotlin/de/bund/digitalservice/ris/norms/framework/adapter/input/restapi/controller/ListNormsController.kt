package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import de.bund.digitalservice.ris.norms.application.port.input.ListNormsUseCase
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.ApiConfiguration
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.encodeEli
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.encodeGuid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping(ApiConfiguration.API_BASE_PATH)
class ListNormsController(private val listNormsService: ListNormsUseCase) {

    @GetMapping
    fun listNorms(@RequestParam q: String?): Mono<ResponseEntity<PaginatedNormListResponseSchema>> {
        val query = ListNormsUseCase.Query(searchTerm = q)

        return listNormsService
            .listNorms(query)
            .collectList()
            .map { normDataList ->
                PaginatedNormListResponseSchema.fromUseCaseData(normDataList)
            }
            .map { paginationData -> ResponseEntity.ok(paginationData) }
            .onErrorReturn(ResponseEntity.internalServerError().build())
    }

    data class PaginatedNormListResponseSchema
    private constructor(val data: List<NormDataResponseSchema>) {
        companion object {
            fun fromUseCaseData(data: List<ListNormsUseCase.NormData>): PaginatedNormListResponseSchema = PaginatedNormListResponseSchema(
                data.map { NormDataResponseSchema.fromUseCaseData(it) },
            )
        }
    }

    data class NormDataResponseSchema
    private constructor(val guid: String, val officialLongTitle: String, val eli: String) {
        companion object {
            fun fromUseCaseData(data: ListNormsUseCase.NormData) = NormDataResponseSchema(
                encodeGuid(data.guid),
                data.officialLongTitle,
                encodeEli(data.eli),
            )
        }
    }
}
