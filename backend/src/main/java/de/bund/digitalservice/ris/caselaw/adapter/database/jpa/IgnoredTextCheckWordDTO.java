package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ignored_text_check_word")
@Entity
public class IgnoredTextCheckWordDTO {

  @Id @GeneratedValue private UUID id;

  @Column(name = "documentation_unit_id")
  private UUID documentationUnitId;

  @Column
  @Size(max = 255)
  private String word;

  @Column(name = "juris_id")
  @ToString.Include
  private Integer jurisId;

  @Column(name = "created_at", updatable = false)
  @CreationTimestamp
  Instant createdAt;

  @ManyToOne
  @JoinColumn(name = "documentation_office_id", referencedColumnName = "id")
  private DocumentationOfficeDTO documentationOffice;
}
