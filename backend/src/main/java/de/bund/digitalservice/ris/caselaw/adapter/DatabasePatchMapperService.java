package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gravity9.jsonpatch.AddOperation;
import com.gravity9.jsonpatch.JsonPatch;
import com.gravity9.jsonpatch.JsonPatchException;
import com.gravity9.jsonpatch.JsonPatchOperation;
import com.gravity9.jsonpatch.RemoveOperation;
import com.gravity9.jsonpatch.diff.JsonDiff;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitPatchRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitPatchDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.RisJsonPatch;
import de.bund.digitalservice.ris.caselaw.domain.mapper.PatchMapperService;
import java.util.ArrayList;
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
  public <T> T applyPatchToEntity(JsonPatch patch, T targetEntity, Class<T> entityType)
      throws JsonProcessingException, JsonPatchException {
    return objectMapper.treeToValue(applyPatch(patch, targetEntity), entityType);
  }

  private <T> JsonNode applyPatch(JsonPatch patch, T targetEntity) throws JsonPatchException {
    return patch.apply(objectMapper.convertValue(targetEntity, JsonNode.class));
  }

  @Override
  public JsonPatch findDiff(DocumentUnit source, DocumentUnit target) {

    return JsonDiff.asJsonPatch(
        objectMapper.convertValue(source, JsonNode.class),
        objectMapper.convertValue(target, JsonNode.class));
  }

  @Override
  public JsonPatch getDiffPatch(DocumentUnit existed, DocumentUnit updated) {
    return objectMapper.convertValue(findDiff(existed, updated), JsonPatch.class);
  }

  @Override
  public JsonPatch removePatchForSamePath(JsonPatch patch1, JsonPatch patch2) {
    Map<String, List<JsonPatchOperation>> pathList1 =
        patch1.getOperations().stream().collect(Collectors.groupingBy(JsonPatchOperation::getPath));
    Map<String, List<JsonPatchOperation>> pathList2 =
        patch2.getOperations().stream().collect(Collectors.groupingBy(JsonPatchOperation::getPath));

    List<JsonPatchOperation> operations = new ArrayList<>(patch1.getOperations());
    for (Entry<String, List<JsonPatchOperation>> entry : pathList2.entrySet()) {
      if (pathList1.containsKey(entry.getKey())) {
        List<JsonPatchOperation> toRemove = pathList1.get(entry.getKey());
        toRemove.forEach(operations::remove);
      }
    }

    return new JsonPatch(operations);
  }

  @Override
  public RisJsonPatch removeExistPatches(RisJsonPatch toFrontend, JsonPatch patch) {
    List<String> operationAsStringList =
        patch.getOperations().stream().map(JsonPatchOperation::toString).toList();
    List<JsonPatchOperation> operations =
        toFrontend.patch().getOperations().stream()
            .filter(operation -> !operationAsStringList.contains(operation.toString()))
            .toList();

    return new RisJsonPatch(
        toFrontend.documentationUnitVersion(), new JsonPatch(operations), toFrontend.errorPaths());
  }

  @Override
  public void savePatch(JsonPatch patch, UUID documentationUnitId, Long documentationUnitVersion) {
    List<JsonPatchOperation> patchWithoutVersion =
        patch.getOperations().stream().filter(op -> !op.getPath().equals("/version")).toList();

    try {
      String patchJson = objectMapper.writeValueAsString(new JsonPatch(patchWithoutVersion));
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
  public JsonPatch calculatePatch(UUID documentationUnitId, Long frontendDocumentationUnitVersion) {
    List<JsonPatchOperation> operations = new ArrayList<>();

    repository
        .findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThan(
            documentationUnitId, frontendDocumentationUnitVersion)
        .forEach(
            patch -> {
              try {
                JsonPatch jsonPatch = objectMapper.readValue(patch.getPatch(), JsonPatch.class);
                operations.addAll(jsonPatch.getOperations());
              } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
              }
            });

    return new JsonPatch(operations);
  }

  @Override
  public RisJsonPatch handlePatchForSamePath(
      DocumentUnit existingDocumentationUnit, JsonPatch patch1, JsonPatch patch2) {
    Map<String, List<JsonPatchOperation>> pathList1 =
        patch1.getOperations().stream().collect(Collectors.groupingBy(JsonPatchOperation::getPath));
    Map<String, List<JsonPatchOperation>> pathList2 =
        patch2.getOperations().stream().collect(Collectors.groupingBy(JsonPatchOperation::getPath));

    List<String> errorPaths = new ArrayList<>();
    List<JsonPatchOperation> operations = new ArrayList<>(patch1.getOperations());
    for (Entry<String, List<JsonPatchOperation>> entry : pathList2.entrySet()) {
      if (pathList1.containsKey(entry.getKey())) {
        List<JsonPatchOperation> toRemove = pathList1.get(entry.getKey());
        toRemove.forEach(
            patch -> {
              if (patch instanceof AddOperation) {
                operations.add(new RemoveOperation(entry.getKey()));
              } else if (patch instanceof RemoveOperation) {
                JsonNode node =
                    objectMapper.convertValue(existingDocumentationUnit, JsonNode.class);
                JsonNode value = node.at(JsonPointer.valueOf(entry.getKey()));
                operations.add(new AddOperation(entry.getKey(), value));
              }
              operations.remove(patch);
            });

        errorPaths.add(entry.getKey());
      }
    }

    return new RisJsonPatch(0L, new JsonPatch(operations), errorPaths);
  }
}
