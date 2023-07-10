package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import de.bund.digitalservice.ris.caselaw.domain.EmailPublishState;
import java.time.Instant;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Builder(toBuilder = true)
@Table(name = "xml_publication")
public record XmlPublicationDTO(
    @Id Long id,
    Long documentUnitId,
    String receiverAddress,
    String mailSubject,
    String xml,
    String statusCode,
    String statusMessages,
    String fileName,
    Instant publishDate,
    EmailPublishState emailPublishState) {}
