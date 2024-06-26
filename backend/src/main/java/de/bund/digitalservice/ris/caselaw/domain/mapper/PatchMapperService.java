package de.bund.digitalservice.ris.caselaw.domain.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import org.springframework.stereotype.Service;

/** A service to enable partial updates of object by patch */
@Service
public class PatchMapperService {

  private final ObjectMapper objectMapper;

  public PatchMapperService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * Applies a JSON Patch to an entity and returns the updated entity.
   *
   * @param patch the JSON Patch to apply
   * @param targetEntity the entity to update
   * @param entityType the class of the entity
   * @param <T> the type of the entity
   * @return the updated entity
   * @throws JsonProcessingException if an error occurs while processing JSON
   * @throws JsonPatchException if an error occurs while applying the patch
   */
  public <T> T applyPatchToEntity(JsonPatch patch, T targetEntity, Class<T> entityType)
      throws JsonProcessingException, JsonPatchException {
    JsonNode patched = patch.apply(objectMapper.convertValue(targetEntity, JsonNode.class));
    return objectMapper.treeToValue(patched, entityType);
  }
}
