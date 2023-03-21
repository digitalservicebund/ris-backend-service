package de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldOfLaw {
  public static final FieldOfLaw EMPTY = new FieldOfLaw();
  Long id;
  Integer childrenCount;
  String identifier;
  String text;
  List<String> linkedFields;
  List<Keyword> keywords;
  List<Norm> norms;
  List<FieldOfLaw> children;
  Integer score;
}
