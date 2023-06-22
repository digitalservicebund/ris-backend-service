package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Builder(toBuilder = true)
@Table("exporter_html_report")
public record ExporterHtmlReportDTO(
    @Id UUID id, UUID documentUnitId, String html, Instant receivedDate) {}
