package de.bund.digitalservice.ris.caselaw.domain.court;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(value = {"synonym", "spruchkoerper"})
public class CourtXML {
  @JacksonXmlProperty(isAttribute = true)
  long id;

  @JacksonXmlProperty(isAttribute = true, localName = "aenddatum_mail")
  String changeDateMail;

  @JacksonXmlProperty(isAttribute = true, localName = "aenddatum_client")
  String changeDateClient;

  @JacksonXmlProperty(isAttribute = true, localName = "aendkz")
  Character changeIndicator;

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

  @JsonProperty(value = "istzweigstellezu") // Zweigstelle des Gerichts
  String isbranchofficeto;

  @JsonProperty(value = "fruehname") // frühe Bezeichnung des Gerichts
  String earlycourtname;

  @JsonProperty(value = "spaetname") // späte Bezeichnung des Gerichts
  String latecourtname;

  @JsonProperty(value = "offizname") // aktuelle offizielle Bezeichnung des Gerichts
  String currentofficialcourtname;

  @JsonProperty(value = "tradname") // traditionelle Bezeichnung des Gerichts
  String traditionalcourtname;

  @JsonProperty(value = "bestzweigstelle") // bestehende Zweigstelle?
  String existingbranchoffice;

  @JsonProperty(value = "aufgehzweigstelle") // aufgegebene Zweigstelle?
  String abandonedbranchoffice;

  @JsonProperty(value = "ansprechpartner")
  String contactperson;

  @JsonProperty(value = "liefertlrs")
  String deliverslrs;

  @JsonProperty(value = "bemerkung")
  String remark;

  @JsonProperty(value = "zusatz") // allgemeine Zusatzangaben
  String additional;

  @JsonProperty(value = "bestehdatum") // seit wann das Gericht besteht
  String existencedate;

  @JsonProperty(value = "aufhebdatum") // Aufhebedatum
  String cancellationdate;
}
