package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "ref_view_active_citation_uli_caselaw", schema = "uli")
@Getter
@NoArgsConstructor
public class UliActiveCitationRefView {

  @Id
  @Column(name = "id")
  private String id;

  // ULI Document
  @Column(name = "source_document_number")
  private String sourceDocumentNumber;

  // caselaw Document
  @Column(name = "target_document_number")
  private String targetDocumentNumber;
}
