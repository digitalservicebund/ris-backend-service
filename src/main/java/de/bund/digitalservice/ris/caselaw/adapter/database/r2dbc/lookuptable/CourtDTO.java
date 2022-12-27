package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

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
  String changedatemail;
  String changedateclient;
  Character changeindicator;
  String version;
  String courttype;
  String courtlocation;
  String field;
  String superiorcourt;
  String foreigncountry;
  String region;
  String federalstate;
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
  String deliverslrs;
  String remark;
  String additional;
  String existencedate;
  String cancellationdate;
}
