package de.bund.digitalservice.ris.domain;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;
import reactor.core.publisher.Mono;

@NoRepositoryBean
public interface SingleRowRepository<T, ID> extends Repository<T, ID> {

  <S extends T> Mono<S> save(S entity);

  Mono<T> findById(ID id);
}
