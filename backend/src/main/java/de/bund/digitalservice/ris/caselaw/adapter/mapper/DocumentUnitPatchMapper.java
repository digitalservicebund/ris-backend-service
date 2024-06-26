package de.bund.digitalservice.ris.caselaw.adapter.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import org.springframework.stereotype.Service;

@Service
public class DocumentUnitPatchMapper {

  private final ObjectMapper objectMapper;

  public DocumentUnitPatchMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public DocumentUnit applyPatchToDocumentUnit(JsonPatch patch, DocumentUnit targetDocumentUnit)
      throws JsonProcessingException, JsonPatchException {
    JsonNode patched = patch.apply(objectMapper.convertValue(targetDocumentUnit, JsonNode.class));
    return objectMapper.treeToValue(patched, DocumentUnit.class);
  }
}
