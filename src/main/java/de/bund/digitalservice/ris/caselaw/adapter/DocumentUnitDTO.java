package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.Texts;
import java.time.Instant;
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

  public static DocumentUnitDTO buildFromDocumentUnit(DocumentUnit documentUnit) {
    DocumentUnitDTOBuilder builder =
        DocumentUnitDTO.builder()
            .id(documentUnit.id())
            .uuid(documentUnit.uuid())
            .documentnumber(documentUnit.documentNumber())
            .creationtimestamp(documentUnit.creationtimestamp())
            .fileuploadtimestamp(documentUnit.fileuploadtimestamp())
            .s3path(documentUnit.s3path())
            .filetype(documentUnit.filetype())
            .filename(documentUnit.filename());

    if (documentUnit.coreData() != null) {
      CoreData coreData = documentUnit.coreData();

      builder
          .fileNumber(coreData.fileNumber())
          .category(coreData.category())
          .procedure(coreData.procedure())
          .ecli(coreData.ecli())
          .appraisalBody(coreData.appraisalBody())
          .decisionDate(coreData.decisionDate() != null ? coreData.decisionDate().toString() : null)
          .legalEffect(documentUnit.coreData().legalEffect())
          .inputType(documentUnit.coreData().inputType())
          .center(documentUnit.coreData().center())
          .region(documentUnit.coreData().region());

      if (coreData.court() != null) {
        builder
            .courtType(documentUnit.coreData().court().type())
            .courtLocation(coreData.court().location());
      }
    }

    builder.previousDecisions(
        documentUnit.previousDecisions().stream()
            .map(
                previousDecision ->
                    PreviousDecisionDTO.builder()
                        .id(previousDecision.id())
                        .documentUnitId(documentUnit.id())
                        .courtLocation(previousDecision.courtPlace())
                        .courtType(previousDecision.courtType())
                        .fileNumber(previousDecision.fileNumber())
                        .decisionDate(previousDecision.date())
                        .build())
            .toList());

    if (documentUnit.texts() != null) {
      Texts texts = documentUnit.texts();

      builder
          .decisionName(texts.decisionName())
          .headline(texts.headline())
          .guidingPrinciple(texts.guidingPrinciple())
          .headnote(texts.headnote())
          .tenor(texts.tenor())
          .reasons(texts.reasons())
          .caseFacts(texts.caseFacts())
          .decisionReasons(texts.decisionReasons());
    }

    return builder.build();
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
  String category; // <-- long id of entry in DocumentType table TODO

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

  @Transient List<PreviousDecisionDTO> previousDecisions;
}
