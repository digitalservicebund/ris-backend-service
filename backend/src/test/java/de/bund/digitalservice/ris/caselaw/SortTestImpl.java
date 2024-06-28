package de.bund.digitalservice.ris.caselaw;

import java.util.Collections;
import org.springframework.data.domain.Sort;

@SuppressWarnings("java:S1068")
public class SortTestImpl extends Sort {
  private boolean empty;
  private boolean sorted;
  private boolean unsorted;

  protected SortTestImpl() {
    super(Collections.emptyList());
  }
}
