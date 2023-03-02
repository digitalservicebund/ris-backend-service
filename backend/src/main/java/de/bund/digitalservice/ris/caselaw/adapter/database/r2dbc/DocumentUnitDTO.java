package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.SubjectFieldDTO;
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
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("doc_unit")
public class DocumentUnitDTO {
  public static final DocumentUnitDTO EMPTY = new DocumentUnitDTO();

  @Id Long id;
  UUID uuid;
  String documentnumber;
  Instant creationtimestamp;
  DataSourceDTO dataSource;

  // Original file
  Instant fileuploadtimestamp;
  String s3path;
  String filetype;
  String filename;

  // RUBRIKEN
  // - Stammdaten
  @Column("gerichtstyp")
  String courtType;

  @Column("document_type_id")
  Long documentTypeId; // points to lookup table row id

  @Transient DocumentTypeDTO documentTypeDTO;

  @Column("vorgang")
  String procedure;

  @Column("ecli")
  String ecli;

  @Column("spruchkoerper")
  String appraisalBody;

  @Column("decision_date")
  Instant decisionDate;

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
  @Transient List<FileNumberDTO> fileNumbers;
  @Transient List<FileNumberDTO> deviatingFileNumbers;
  @Transient List<IncorrectCourtDTO> incorrectCourts;
  @Transient List<DeviatingEcliDTO> deviatingEclis;
  @Transient List<DeviatingDecisionDateDTO> deviatingDecisionDates;
  @Transient List<SubjectFieldDTO> fieldsOfLaw;
}
