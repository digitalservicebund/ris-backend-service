package de.bund.digitalservice.ris.caselaw.domain.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gravity9.jsonpatch.JsonPatch;
import com.gravity9.jsonpatch.JsonPatchException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.RisJsonPatch;
import java.util.UUID;

/** A service to enable partial updates of object by patch */
public interface PatchMapperService {
  JsonPatch calculatePatch(UUID uuid, Long documentationUnitVersion);

  RisJsonPatch handlePatchForSamePath(
      DocumentUnit existingDocumentationUnit, JsonPatch patch, JsonPatch patch1);

  void savePatch(JsonPatch patch, UUID uuid, Long version);

  <T> T applyPatchToEntity(JsonPatch patch, T existingDocumentationUnit, Class<T> documentUnitClass)
      throws JsonProcessingException, JsonPatchException;

  JsonPatch findDiff(DocumentUnit existed, DocumentUnit updated);

  JsonPatch getDiffPatch(DocumentUnit existed, DocumentUnit updated);

  JsonPatch removePatchForSamePath(JsonPatch patch1, JsonPatch patch2);

  RisJsonPatch removeExistPatches(RisJsonPatch toFrontend, JsonPatch patch);
}
