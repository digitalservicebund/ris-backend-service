package de.bund.digitalservice.ris.caselaw.domain.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatchException;
import de.bund.digitalservice.ris.caselaw.domain.MergeableJsonPatch;
import de.bund.digitalservice.ris.caselaw.domain.RisJsonPatch;
import java.util.List;
import java.util.UUID;

/** A service to enable partial updates of object by patch */
public interface PatchMapperService {
  RisJsonPatch calculatePatch(UUID uuid, Long documentationUnitVersion, Long newVersion);

  List<String> removePatchForSamePath(MergeableJsonPatch patch, MergeableJsonPatch patch1);

  void savePatch(RisJsonPatch patch, UUID uuid, Long version);

  <T> T applyPatchToEntity(
      MergeableJsonPatch patch, T existingDocumentationUnit, Class<T> documentUnitClass)
      throws JsonProcessingException, JsonPatchException;
}
