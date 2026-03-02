package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

@Getter
@Entity
@Immutable
@Table(name = "ref_view_adm", schema = "references_schema")
public class AdministrativeRegulationDTO {
  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "document_number")
  private String documentNumber;

  @Column(name = "juris_abbreviation")
  private String jurisAbbreviation;

  @OneToMany(mappedBy = "source")
  private List<AdministrativeRegulationActiveCaselawReferenceDTO> activeCaselawReferences;
}
