package de.bund.digitalservice.ris.domain;

import java.time.Instant;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import lombok.*;
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
  String docketNumber;

  String gerichtstyp;
  String dokumenttyp;
  String vorgang;
  String ecli;
  String spruchkoerper;
  String entscheidungsdatum;
  String gerichtssitz;
  String rechtskraft;
  String eingangsart;
  String dokumentationsstelle;
  String region;

  // - Kurz- & Langtexte
  String entscheidungsname;
  String titelzeile;
  String leitsatz;
  String orientierungssatz;
  String tenor;
  String gruende;
  String tatbestand;
  String entscheidungsgruende;

  @Transient List<PreviousDecision> previousDecisions;

  public DocUnitDTO setPreviousDecisions(List<PreviousDecision> previousDecisions) {
    this.previousDecisions = previousDecisions;
    return this;
  }

  public boolean hasFileAttached() {
    return s3path != null;
  }
}
