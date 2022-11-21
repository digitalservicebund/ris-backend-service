package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi

import de.bund.digitalservice.ris.norms.application.port.input.ListNormsUseCase
import de.bund.digitalservice.ris.norms.application.port.input.ListNormsUseCase.NormData
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
            .map({ normDataList -> PaginatedNormListResponseSchema(normDataList) })
            .map({ paginationData -> ResponseEntity.ok(paginationData) })
            .onErrorReturn(ResponseEntity.internalServerError().build())

    data class PaginatedNormListResponseSchema(val data: List<NormData>)
}
