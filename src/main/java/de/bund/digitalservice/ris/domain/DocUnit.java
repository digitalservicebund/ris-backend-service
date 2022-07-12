package de.bund.digitalservice.ris.domain;

import java.time.Instant;
import java.util.Calendar;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocUnit {
  public static final DocUnit EMPTY = new DocUnit();

  public static DocUnit createNew(DocUnitCreationInfo docUnitCreationInfo, int documentNumber) {
    return DocUnit.builder()
        .uuid(UUID.randomUUID())
        .creationtimestamp(Instant.now())
        .documentnumber(
            docUnitCreationInfo.getDocumentationCenterAbbreviation()
                + docUnitCreationInfo.getDocumentType()
                + Calendar.getInstance().get(Calendar.YEAR)
                + String.format("%06d", documentNumber))
        .build();
  }

  @Id Long id; // remove this, no longer necessary, uuid should be @Id TODO
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
