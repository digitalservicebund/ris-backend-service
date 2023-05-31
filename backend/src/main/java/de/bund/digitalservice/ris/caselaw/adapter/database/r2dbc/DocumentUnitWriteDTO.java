package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.FieldOfLawDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("doc_unit")
@EqualsAndHashCode(callSuper = true)
public class DocumentUnitWriteDTO extends DocumentUnitMetadataDTO {
  public static final DocumentUnitWriteDTO EMPTY = new DocumentUnitWriteDTO();

  // RUBRIKEN
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

  @Transient List<DocumentUnitMetadataDTO> proceedingDecisions;
  @Transient List<FileNumberDTO> deviatingFileNumbers;
  @Transient List<IncorrectCourtDTO> incorrectCourts;
  @Transient List<DeviatingEcliDTO> deviatingEclis;
  @Transient List<DeviatingDecisionDateDTO> deviatingDecisionDates;
  @Transient List<KeywordDTO> keywords;
  @Transient List<FieldOfLawDTO> fieldsOfLaw;
  @Transient List<DocumentUnitNormDTO> norms;
}
