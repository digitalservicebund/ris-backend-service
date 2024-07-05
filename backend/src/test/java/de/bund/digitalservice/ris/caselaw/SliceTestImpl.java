package de.bund.digitalservice.ris.caselaw;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;

@SuppressWarnings("java:S1068")
public class SliceTestImpl<T> implements Slice<T> {

  private int number;
  private int size;
  private int numberOfElements;
  private List<T> content;
  private SortTestImpl sort;
  private PageableTestImpl pageable;
  private boolean last;
  private int totalPages;
  private int totalElements;
  private boolean first;
  private boolean empty;

  @Override
  public int getNumber() {
    return number;
  }

  @Override
  public int getSize() {
    return size;
  }

  @Override
  public int getNumberOfElements() {
    return numberOfElements;
  }

  @Override
  public List<T> getContent() {
    return content;
  }

  public int getTotalPages() {
    return totalPages;
  }

  public int getTotalElements() {
    return totalElements;
  }

  @Override
  public boolean hasContent() {
    return content != null && !content.isEmpty();
  }

  @Override
  public Sort getSort() {
    return sort;
  }

  @Override
  public boolean isFirst() {
    return first;
  }

  @Override
  public boolean isLast() {
    return last;
  }

  @Override
  public boolean hasNext() {
    return !last;
  }

  @Override
  public boolean hasPrevious() {
    return !first;
  }

  @Override
  public Pageable nextPageable() {
    return pageable.next();
  }

  @Override
  public Pageable previousPageable() {
    return pageable.previousOrFirst();
  }

  @Override
  public <U> Slice<U> map(Function<? super T, ? extends U> converter) {
    List<? extends U> newList = content.stream().map(converter).toList();
    return (Slice<U>) new SliceImpl<>(newList);
  }

  @NotNull
  @Override
  public Iterator<T> iterator() {
    return content.iterator();
  }
}
