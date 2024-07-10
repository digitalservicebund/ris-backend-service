package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.fge.jsonpatch.diff.JsonDiff;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitPatchRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitPatchDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.MergeableJsonPatch;
import de.bund.digitalservice.ris.caselaw.domain.RisJsonPatch;
import de.bund.digitalservice.ris.caselaw.domain.mapper.PatchMapperService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DatabasePatchMapperService implements PatchMapperService {
  private final ObjectMapper objectMapper;
  private final DatabaseDocumentationUnitPatchRepository repository;

  public DatabasePatchMapperService(
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
   */
  @Override
  public <T> T applyPatchToEntity(MergeableJsonPatch patch, T targetEntity, Class<T> entityType)
      throws JsonProcessingException, JsonPatchException {
    return objectMapper.treeToValue(applyPatch(patch, targetEntity), entityType);
  }

  @Override
  public JsonPatch findDiff(DocumentUnit source, DocumentUnit target) {

    return JsonDiff.asJsonPatch(
        objectMapper.convertValue(source, JsonNode.class),
        objectMapper.convertValue(target, JsonNode.class));
  }

  @Override
  public MergeableJsonPatch getDiffPatch(DocumentUnit existed, DocumentUnit updated) {
    return objectMapper.convertValue(findDiff(existed, updated), MergeableJsonPatch.class);
  }

  private <T> JsonNode applyPatch(MergeableJsonPatch patch, T targetEntity)
      throws JsonPatchException {
    return patch.apply(objectMapper.convertValue(targetEntity, JsonNode.class));
  }

  @Override
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

  @Override
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

  @Override
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
