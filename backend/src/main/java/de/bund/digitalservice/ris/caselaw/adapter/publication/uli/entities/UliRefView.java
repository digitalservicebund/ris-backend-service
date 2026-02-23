package de.bund.digitalservice.ris.caselaw.adapter.publication.uli.entities;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalPeriodicalDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.jspecify.annotations.Nullable;

@Entity
@Immutable
@Table(name = "ref_view_uli", schema = "uli")
@Getter
@NoArgsConstructor
public class UliRefView {

  @Id
  @Column(name = "document_number")
  private String documentNumber;

  @Column(name = "author")
  private String author;

  @Column(name = "citation")
  private String citation;

  @Nullable
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "document_type_id")
  private DocumentTypeDTO documentType; // global lookuptable?

  @Column(name = "document_type_raw_value")
  private String documentTypeRawValue;

  @Nullable
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "legal_periodical_id")
  private LegalPeriodicalDTO legalPeriodical; // global lookuptable?

  @Column(name = "legal_periodical_raw_value")
  private String legalPeriodicalRawValue;
}
