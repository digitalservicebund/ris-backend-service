package de.bund.digitalservice.ris.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreviousDecision {
  @Id Long id;

  @Column("gerichtstyp")
  String courtType;

  @Column("gerichtsort")
  String courtPlace;

  @Column("datum")
  String date;

  @Column("aktenzeichen")
  String docketNumber;

  @JsonIgnore String documentnumber;

  public PreviousDecision setDocumentnumber(String documentnumber) {
    this.documentnumber = documentnumber;
    return this;
  }
}
