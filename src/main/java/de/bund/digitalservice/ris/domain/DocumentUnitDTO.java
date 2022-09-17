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
public class DocumentUnitDTO {
  public static final DocumentUnitDTO EMPTY = new DocumentUnitDTO();

  public static DocumentUnitDTO createNew(
      DocumentUnitCreationInfo documentUnitCreationInfo, int documentNumber) {
    return DocumentUnitDTO.builder()
        .uuid(UUID.randomUUID())
        .creationtimestamp(Instant.now())
        .documentnumber(
            documentUnitCreationInfo.getDocumentationCenterAbbreviation()
                + documentUnitCreationInfo.getDocumentType()
                + Calendar.getInstance().get(Calendar.YEAR)
                + String.format("%06d", documentNumber))
        .build();
  }

  public static DocumentUnitDTO buildFromDocumentUnit(DocumentUnit documentUnit) {
    return DocumentUnitDTO.builder()
        .id(documentUnit.id())
        .uuid(documentUnit.uuid())
        .documentnumber(documentUnit.documentnumber())
        .creationtimestamp(documentUnit.creationtimestamp())
        .fileuploadtimestamp(documentUnit.fileuploadtimestamp())
        .s3path(documentUnit.s3path())
        .filetype(documentUnit.filetype())
        .filename(documentUnit.filename())
        .fileNumber(documentUnit.coreData().fileNumber())
        .courtType(documentUnit.coreData().courtType())
        .category(documentUnit.coreData().category())
        .procedure(documentUnit.coreData().procedure())
        .ecli(documentUnit.coreData().ecli())
        .appraisalBody(documentUnit.coreData().appraisalBody())
        .decisionDate(documentUnit.coreData().decisionDate())
        .courtLocation(documentUnit.coreData().courtLocation())
        .legalEffect(documentUnit.coreData().legalEffect())
        .inputType(documentUnit.coreData().inputType())
        .center(documentUnit.coreData().center())
        .region(documentUnit.coreData().region())
        .previousDecisions(documentUnit.previousDecisions())
        .decisionName(documentUnit.texts().decisionName())
        .headline(documentUnit.texts().headline())
        .guidingPrinciple(documentUnit.texts().guidingPrinciple())
        .headnote(documentUnit.texts().headnote())
        .tenor(documentUnit.texts().tenor())
        .reasons(documentUnit.texts().reasons())
        .caseFacts(documentUnit.texts().caseFacts())
        .decisionReasons(documentUnit.texts().decisionReasons())
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

  public DocumentUnitDTO setPreviousDecisions(List<PreviousDecision> previousDecisions) {
    this.previousDecisions = previousDecisions;
    return this;
  }

  public boolean hasFileAttached() {
    return s3path != null;
  }
}
