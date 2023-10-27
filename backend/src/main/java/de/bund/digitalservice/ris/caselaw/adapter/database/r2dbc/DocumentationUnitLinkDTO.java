package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CitationStyleDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitLinkType;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table("documentation_unit_link")
@Deprecated
public class DocumentationUnitLinkDTO {
  @Id private Long id;
  private UUID parentDocumentationUnitUuid;
  private UUID childDocumentationUnitUuid;
  private DocumentationUnitLinkType type;
  private UUID citationStyleUuid;

  @Transient private CitationStyleDTO citationStyleDTO;
}
