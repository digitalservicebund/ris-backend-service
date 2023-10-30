package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * @deprecated use {@link de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingEcliDTO}
 *     instead
 *     <p>Abweichender <a href="https://de.wikipedia.org/wiki/European_Case_Law_Identifier">ECLI</a>
 *     einer Dokumentationseinheit.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("deviating_ecli")
@Deprecated
public class DeviatingEcliDTO {
  @Id Long id;
  Long documentUnitId;
  String ecli;
}
