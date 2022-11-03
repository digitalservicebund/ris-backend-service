package de.bund.digitalservice.ris.caselaw.domain.lookuptable;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("lookuptable_court")
public class CourtDTO {
  public static final CourtDTO EMPTY = new CourtDTO();
  long id;
  String changeDateMail;
  String changeDateClient;
  char changeIndicator;
  String version;
  String courtType;
  String courtLocation;
  String field;
  String superiorcourt;
  String foreignCountry;
  String region;
  String federalState;
  String belongsto;
  String street;
  String zipcode;
  String maillocation;
  String phone;
  String fax;
  String postofficebox;
  String postofficeboxzipcode;
  String postofficeboxlocation;
  String email;
  String internet;
  String isbranchofficeto;
  String earlycourtname;
  String latecourtname;
  String currentofficialcourtname;
  String traditionalcourtname;
  String existingbranchoffice;
  String abandonedbranchoffice;
  String contactperson;
  String deliverslrs; // TODO
  String remark;
  String additional;
  String existencedate;
  String cancellationdate;
  List<CourtSynonymDTO> synonyms;
  List<CourtAppraisalBodyDTO> appraisalbodies;
}
