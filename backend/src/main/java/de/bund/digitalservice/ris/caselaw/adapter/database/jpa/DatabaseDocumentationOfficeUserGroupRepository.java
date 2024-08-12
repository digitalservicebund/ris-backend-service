package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseDocumentationOfficeUserGroupRepository
    extends JpaRepository<DocumentationOfficeUserGroupDTO, UUID> {

  @NotNull
  List<DocumentationOfficeUserGroupDTO> findAll();
}
