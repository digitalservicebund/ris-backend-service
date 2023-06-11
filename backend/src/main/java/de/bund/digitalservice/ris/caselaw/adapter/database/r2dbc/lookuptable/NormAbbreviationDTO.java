package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table("norm_abbreviation")
public class NormAbbreviationDTO implements Persistable<UUID> {
  @Id UUID id;
  String abbreviation;
  LocalDate decisionDate;
  Integer documentId;
  String documentNumber;
  String officialLetterAbbreviation;
  String officialLongTitle;
  String officialShortTitle;
  Character source;
  @Transient List<DocumentTypeNewDTO> documentTypes;
  @Transient List<RegionDTO> regions;
  @Transient boolean newEntity = false;

  @Override
  public boolean isNew() {
    return id == null || newEntity;
  }
}
