package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "document_type")
public class DocumentTypeDTO {
  @Id @GeneratedValue private UUID id;

  @Column private String abbreviation;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "document_category_id")
  private DocumentCategoryDTO category;

  @Column private String label;

  @Column(name = "super_label_1")
  private String superLabel1;

  @Column(name = "super_label_2")
  private String superLabel2;

  @Column private Boolean multiple;
}
