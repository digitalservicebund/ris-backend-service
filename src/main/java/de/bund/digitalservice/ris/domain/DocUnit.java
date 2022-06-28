package de.bund.digitalservice.ris.domain;

import java.time.Instant;
import java.util.Calendar;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocUnit {
  public static final DocUnit EMPTY = new DocUnit();

  public static DocUnit createNew(DocUnitCreationInfo docUnitCreationInfo, int documentNumber) {
    DocUnit docUnit = new DocUnit();
    docUnit.setUuid(UUID.randomUUID());
    docUnit.setCreationtimestamp(Instant.now());
    docUnit.setDocumentnumber(
        docUnitCreationInfo.getDocumentationCenterAbbreviation()
            + docUnitCreationInfo.getDocumentType()
            + Calendar.getInstance().get(Calendar.YEAR)
            + String.format("%06d", documentNumber));
    return docUnit;
  }

  @Id Long id;
  UUID uuid;
  String documentnumber;
  Instant creationtimestamp;

  // Original file
  Instant fileuploadtimestamp;
  String s3path;
  String filetype;
  String filename;

  // RUBRIKEN
  // - Stammdaten
  String aktenzeichen;
  String gerichtstyp;
  String dokumenttyp;
  String vorgang;
  String ecli;
  String spruchkoerper;
  String entscheidungsdatum;
  String gerichtssitz;
  String rechtskraft;
  String eingangsart;
  String dokumentationsstelle;
  String region;

  // - Kurz- & Langtexte
  String entscheidungsname;
  String titelzeile;
  String leitsatz;
  String orientierungssatz;
  String tenor;
  String gruende;
  String tatbestand;
  String entscheidungsgruende;

  public boolean hasFileAttached() {
    return s3path != null;
  }
}
