package de.bund.digitalservice.ris.caselaw.adapter.database.jpa.appeal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Immutable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Immutable
@Table(schema = "incremental_migration", name = "appeal_status")
public class AppealStatusDTO {

  @Id @GeneratedValue private UUID id;

  @Column String value;
}
