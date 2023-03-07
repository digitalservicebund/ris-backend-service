package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitLink;

import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("document_unit_link")
public class DocumentUnitLinkDTO {
  BigInteger parentDocumentUnitId;
  BigInteger childDocumentUnitId;
}
