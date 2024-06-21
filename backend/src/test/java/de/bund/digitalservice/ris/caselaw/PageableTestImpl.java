package de.bund.digitalservice.ris.caselaw;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageableTestImpl implements Pageable {
  private int pageNumber;
  private int pageSize;
  private SortTestImpl sort;
  private int offset;
  private boolean paged;
  private boolean unpaged;

  @Override
  public int getPageNumber() {
    return pageNumber;
  }

  @Override
  public int getPageSize() {
    return pageSize;
  }

  @Override
  public long getOffset() {
    return 0;
  }

  @Override
  public Sort getSort() {
    return sort;
  }

  @Override
  public Pageable next() {
    return next();
  }

  @Override
  public Pageable previousOrFirst() {
    return previousOrFirst();
  }

  @Override
  public Pageable first() {
    return first();
  }

  @Override
  public Pageable withPage(int pageNumber) {
    return null;
  }

  @Override
  public boolean hasPrevious() {
    return false;
  }
}
