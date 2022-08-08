package de.bund.digitalservice.ris.domain;

import org.springframework.data.annotation.Id;

public record XmlMail(@Id Long id, Long documentUnitId, String mailSubject, String xml)
    implements ExportObject {}
