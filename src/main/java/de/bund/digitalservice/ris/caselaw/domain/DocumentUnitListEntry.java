package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("doc_unit")
public record DocumentUnitListEntry(
    @Id Long id,
    UUID uuid,
    @Column("documentnumber") String documentNumber,
    Instant creationtimestamp,
    String filename,
    @Column("aktenzeichen") String fileNumber) {}
