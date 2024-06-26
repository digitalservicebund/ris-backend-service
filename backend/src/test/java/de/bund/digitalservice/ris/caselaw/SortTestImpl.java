package de.bund.digitalservice.ris.caselaw;

import java.util.Collections;
import org.springframework.data.domain.Sort;

public class SortTestImpl extends Sort {
  private boolean empty;
  private boolean sorted;
  private boolean unsorted;

  protected SortTestImpl() {
    super(Collections.emptyList());
  }
}
