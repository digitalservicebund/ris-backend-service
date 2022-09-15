package de.bund.digitalservice.ris.domain;

import java.time.Instant;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("doc_unit")
public class DocUnitDTO {
  public static final DocUnitDTO EMPTY = new DocUnitDTO();

  public static DocUnitDTO createNew(DocUnitCreationInfo docUnitCreationInfo, int documentNumber) {
    return DocUnitDTO.builder()
        .uuid(UUID.randomUUID())
        .creationtimestamp(Instant.now())
        .documentnumber(
            docUnitCreationInfo.getDocumentationCenterAbbreviation()
                + docUnitCreationInfo.getDocumentType()
                + Calendar.getInstance().get(Calendar.YEAR)
                + String.format("%06d", documentNumber))
        .build();
  }

  @Id Long id;
  UUID uuid;
  String documentnumber;
  Instant creationtimestamp;

  // Original file
  Instant fileuploadtimestamp;
  String s3path;
  String filetype;
  String filename;

  // RUBRIKEN
  // - Stammdaten
  @Column("aktenzeichen")
  String fileNumber;

  @Column("gerichtstyp")
  String courtType;

  @Column("dokumenttyp")
  String category;

  @Column("vorgang")
  String procedure;

  @Column("ecli")
  String ecli;

  @Column("spruchkoerper")
  String appraisalBody;

  @Column("entscheidungsdatum")
  String decisionDate;

  @Column("gerichtssitz")
  String courtLocation;

  @Column("rechtskraft")
  String legalEffect;

  @Column("eingangsart")
  String inputType;

  @Column("dokumentationsstelle")
  String center;

  @Column("region")
  String region;

  // - Kurz- & Langtexte
  @Column("entscheidungsname")
  String decisionName;

  @Column("titelzeile")
  String headline;

  @Column("leitsatz")
  String guidingPrinciple;

  @Column("orientierungssatz")
  String headnote;

  @Column("tenor")
  String tenor;

  @Column("gruende")
  String reasons;

  @Column("tatbestand")
  String caseFacts;

  @Column("entscheidungsgruende")
  String decisionReasons;

  @Transient List<PreviousDecision> previousDecisions;

  public DocUnitDTO setPreviousDecisions(List<PreviousDecision> previousDecisions) {
    this.previousDecisions = previousDecisions;
    return this;
  }

  public boolean hasFileAttached() {
    return s3path != null;
  }
}
