package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.CorrectionType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "correction", schema = "incremental_migration")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorrectionDTO {
  @Id
  @GeneratedValue()
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "type", nullable = false)
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private CorrectionType type;

  @Column(name = "description")
  private String description;

  @Column(name = "date")
  private LocalDate date;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "correction_id", nullable = false)
  @OrderBy("rank")
  @Singular
  private List<CorrectionBorderNumberDTO> borderNumbers;

  @Column(name = "content")
  private String content;

  @Column(name = "rank")
  private Long rank;
}
