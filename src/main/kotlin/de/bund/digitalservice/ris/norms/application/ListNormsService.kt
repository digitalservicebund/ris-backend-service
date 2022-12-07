package de.bund.digitalservice.ris.norms.application

import de.bund.digitalservice.ris.norms.application.port.input.ListNormsUseCase
import de.bund.digitalservice.ris.norms.application.port.output.GetAllNormsOutputPort
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class ListNormsService(private val getAllNormsPort: GetAllNormsOutputPort) : ListNormsUseCase {

    override fun listNorms(): Flux<ListNormsUseCase.NormData> {
        return getAllNormsPort
            .getAllNorms()
            .map({ norm -> ListNormsUseCase.NormData(norm.guid, norm.officialLongTitle) })
    }
}
