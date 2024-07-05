package de.bund.digitalservice.ris.caselaw.domain.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.JsonPatchOperation;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitPatchRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitPatchDTO;
import de.bund.digitalservice.ris.caselaw.domain.MergeableJsonPatch;
import de.bund.digitalservice.ris.caselaw.domain.RisJsonPatch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/** A service to enable partial updates of object by patch */
@Service
public class PatchMapperService {

  private final ObjectMapper objectMapper;
  private final DatabaseDocumentationUnitPatchRepository repository;

  public PatchMapperService(
      ObjectMapper objectMapper, DatabaseDocumentationUnitPatchRepository repository) {
    this.objectMapper = objectMapper;
    this.repository = repository;
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
  public <T> T applyPatchToEntity(MergeableJsonPatch patch, T targetEntity, Class<T> entityType)
      throws JsonProcessingException, JsonPatchException {
    return objectMapper.treeToValue(applyPatch(patch, targetEntity), entityType);
  }

  public <T> JsonNode applyPatch(MergeableJsonPatch patch, T targetEntity)
      throws JsonPatchException {
    return patch.apply(objectMapper.convertValue(targetEntity, JsonNode.class));
  }

  public void savePatch(
      RisJsonPatch patch, UUID documentationUnitId, Long documentationUnitVersion) {
    try {
      String patchJson = objectMapper.writeValueAsString(patch.patch());
      DocumentationUnitPatchDTO dto =
          DocumentationUnitPatchDTO.builder()
              .documentationUnitId(documentationUnitId)
              .documentationUnitVersion(
                  documentationUnitVersion == null ? 1 : documentationUnitVersion)
              .patch(patchJson)
              .build();
      repository.save(dto);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public RisJsonPatch calculatePatch(
      UUID documentationUnitId,
      Long frontendDocumentationUnitVersion,
      Long newDocumentationUnitVersion) {
    MergeableJsonPatch mergedPatch = new MergeableJsonPatch();
    repository
        .findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
            documentationUnitId, frontendDocumentationUnitVersion)
        .forEach(
            patch -> {
              try {
                MergeableJsonPatch jsonPatch =
                    objectMapper.readValue(patch.getPatch(), MergeableJsonPatch.class);
                mergedPatch.addOperations(jsonPatch.getOperations());
              } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
              }
            });

    return new RisJsonPatch(newDocumentationUnitVersion, mergedPatch, Collections.emptyList());
  }

  public List<String> removePatchForSamePath(MergeableJsonPatch patch1, MergeableJsonPatch patch2) {
    Map<String, List<JsonPatchOperation>> pathList1 =
        patch1.getOperations().stream()
            .collect(
                Collectors.groupingBy(
                    op -> {
                      JsonNode operation = objectMapper.convertValue(op, JsonNode.class);
                      return operation.get("path").textValue();
                    }));
    Map<String, List<JsonPatchOperation>> pathList2 =
        patch2.getOperations().stream()
            .collect(
                Collectors.groupingBy(
                    op -> {
                      JsonNode operation = objectMapper.convertValue(op, JsonNode.class);
                      return operation.get("path").textValue();
                    }));

    List<String> errorPaths = new ArrayList<>();
    for (Entry<String, List<JsonPatchOperation>> entry : pathList2.entrySet()) {
      if (pathList1.containsKey(entry.getKey())) {
        patch1.getOperations().removeAll(pathList1.get(entry.getKey()));
        errorPaths.add(entry.getKey());
      }
    }

    return errorPaths;
  }
}
