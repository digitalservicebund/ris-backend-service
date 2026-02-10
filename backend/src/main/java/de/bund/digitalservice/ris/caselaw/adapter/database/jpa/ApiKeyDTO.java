package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "api_key")
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ApiKeyDTO {
  @Id @GeneratedValue private UUID id;

  @Getter
  @Column(name = "api_key")
  private String apiKey;

  @Column(name = "user_account")
  @Getter
  private String userAccount;

  @ManyToOne
  @JoinColumn(name = "documentation_office")
  private DocumentationOfficeDTO documentationOffice;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "valid_until")
  @Getter
  private Instant validUntil;

  private boolean invalidated;
}
