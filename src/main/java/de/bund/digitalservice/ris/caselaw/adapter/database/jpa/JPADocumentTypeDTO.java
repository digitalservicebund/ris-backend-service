package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "lookuptable_documenttype")
public class JPADocumentTypeDTO {
  @Id private long id;

  @Column(name = "change_date_mail")
  private String changeDateMail;

  @Column(name = "change_date_client")
  private String changeDateClient;

  @Column(name = "change_indicator")
  private char changeIndicator;

  private String version;

  @Column(name = "juris_shortcut")
  private String jurisShortcut;

  @Column(name = "document_type")
  private char documentType;

  private String multiple;
  private String label;
  private String superlabel1;
  private String superlabel2;
}
