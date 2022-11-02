package de.bund.digitalservice.ris.caselaw.domain.lookuptable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;
import lombok.Data;

@Data
public class CourtXML {
  @JacksonXmlProperty(isAttribute = true)
  long id;

  @JacksonXmlProperty(isAttribute = true, localName = "aenddatum_mail")
  String changeDateMail;

  @JacksonXmlProperty(isAttribute = true, localName = "aenddatum_client")
  String changeDateClient;

  @JacksonXmlProperty(isAttribute = true, localName = "aendkz")
  char changeIndicator;

  @JacksonXmlProperty(isAttribute = true)
  String version;

  @JsonProperty(value = "gertyp")
  String courtType;

  @JsonProperty(value = "gerort")
  String courtLocation;

  @JsonProperty(value = "bereich")
  String field;

  @JsonProperty(value = "supra") // übergeordnetes Gericht
  String superiorcourt;

  @JsonProperty(value = "ausland")
  String foreignCountry;

  @JsonProperty(value = "region")
  String region;

  @JsonProperty(value = "buland")
  String federalState;

  @JsonProperty(value = "gehoertzu")
  String belongsto;

  @JsonProperty(value = "strasse")
  String street;

  @JsonProperty(value = "plz")
  String zipcode;

  @JsonProperty(value = "postort")
  String maillocation;

  @JsonProperty(value = "telefon")
  String phone;

  @JsonProperty(value = "telefax")
  String fax;

  @JsonProperty(value = "postfach")
  String postofficebox;

  @JsonProperty(value = "postfachplz")
  String postofficeboxzipcode;

  @JsonProperty(value = "postfachort")
  String postofficeboxlocation;

  @JsonProperty(value = "email")
  String email;

  @JsonProperty(value = "internet")
  String internet;

  @JsonProperty(value = "istzweigstellezu")
  String isbranchofficeto;

  @JsonProperty(value = "fruehname")
  String fruehname; // TODO

  @JsonProperty(value = "spaetname")
  String spaetname; // TODO

  @JsonProperty(value = "offizname")
  String offizname; // TODO

  @JsonProperty(value = "tradname")
  String tradname; // TODO

  @JsonProperty(value = "bestzweigstelle")
  String bestzweigstelle; // TODO

  @JsonProperty(value = "aufgehzweigstelle")
  String aufgehzweigstelle; // TODO

  @JsonProperty(value = "ansprechpartner")
  String contactperson;

  @JsonProperty(value = "liefertlrs")
  String deliverslrs;

  @JsonProperty(value = "bemerkung")
  String remark;

  @JsonProperty(value = "zusatz")
  String additional;

  @JsonProperty(value = "bestehdatum")
  String existencedate;

  @JsonProperty(value = "aufhebdatum")
  String cancellationdate;

  @JacksonXmlElementWrapper(useWrapping = false)
  @JsonProperty(value = "synonym")
  List<CourtSynonymXML> synonyms;

  @JacksonXmlElementWrapper(useWrapping = false)
  @JsonProperty(value = "spruchkoerper")
  List<CourtAppraisalBodyXML> appraisalbodies;
}
