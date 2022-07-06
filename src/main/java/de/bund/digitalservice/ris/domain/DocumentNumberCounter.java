package de.bund.digitalservice.ris.domain;

import java.util.Calendar;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentNumberCounter {

  public static DocumentNumberCounter buildInitial() {
    DocumentNumberCounter counter = new DocumentNumberCounter();
    counter.nextnumber = 1;
    counter.currentyear = Calendar.getInstance().get(Calendar.YEAR);
    return counter;
  }

  @Id Long id;
  Integer nextnumber;
  Integer currentyear;
}
