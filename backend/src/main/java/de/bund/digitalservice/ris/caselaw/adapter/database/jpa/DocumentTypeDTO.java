package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "document_type", schema = "incremental_migration")
public class DocumentTypeDTO {
  @Id @GeneratedValue private UUID id;

  @Column
  @NotBlank
  @Size(min = 1, max = 32)
  private String abbreviation;

  @ManyToOne
  @JoinColumn(name = "document_category_id")
  @Valid
  @NotNull
  private DocumentCategoryDTO category;

  @Column private String label;

  @Column(name = "super_label_1")
  private String superLabel1;

  @Column(name = "super_label_2")
  private String superLabel2;

  @Column @NotNull private Boolean multiple;

  @Transient @ToString.Include private String jurisID;
}
