package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi

import de.bund.digitalservice.ris.norms.application.port.input.ListNormsUseCase
import encodeGuid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping(ApiConfiguration.API_BASE_PATH)
class ListNormsController(private val listNormsService: ListNormsUseCase) {

    @GetMapping
    fun getAllNorms(): Mono<ResponseEntity<PaginatedNormListResponseSchema>> =
        listNormsService
            .listNorms()
            .collectList()
            .map({ normDataList -> PaginatedNormListResponseSchema.fromUseCaseData(normDataList) })
            .map({ paginationData -> ResponseEntity.ok(paginationData) })
            .onErrorReturn(ResponseEntity.internalServerError().build())

    data class PaginatedNormListResponseSchema
    private constructor(val data: List<NormDataResponseSchema>) {
        companion object {
            fun fromUseCaseData(data: List<ListNormsUseCase.NormData>): PaginatedNormListResponseSchema {
                val foo = data.map { NormDataResponseSchema.fromUseCaseData(it) }
                return PaginatedNormListResponseSchema(foo)
            }
        }
    }

    data class NormDataResponseSchema private constructor(val guid: String, val officialLongTitle: String) {
        companion object {
            fun fromUseCaseData(data: ListNormsUseCase.NormData): NormDataResponseSchema {
                return NormDataResponseSchema(encodeGuid(data.guid), data.officialLongTitle)
            }
        }
    }
}
