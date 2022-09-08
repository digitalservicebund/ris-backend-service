package de.bund.digitalservice.ris.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreviousDecision {
  @Id Long id;
  String gerichtstyp;
  String gerichtsort;
  String datum;
  String aktenzeichen;
  @JsonIgnore String documentnumber;

  public PreviousDecision setDocumentnumber(String documentnumber) {
    this.documentnumber = documentnumber;
    return this;
  }
}
