package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface NormElementRepository {

  List<NormElement> findAllByDocumentCategoryLabelR();
}
