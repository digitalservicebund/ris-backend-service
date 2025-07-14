package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.gravity9.jsonpatch.AddOperation;
import com.gravity9.jsonpatch.JsonPatch;
import com.gravity9.jsonpatch.JsonPatchException;
import com.gravity9.jsonpatch.JsonPatchOperation;
import com.gravity9.jsonpatch.PathValueOperation;
import com.gravity9.jsonpatch.RemoveOperation;
import com.gravity9.jsonpatch.ReplaceOperation;
import com.gravity9.jsonpatch.diff.JsonDiff;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitPatchRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitPatchDTO;
import de.bund.digitalservice.ris.caselaw.domain.Attachment;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.RisJsonPatch;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitPatchException;
import de.bund.digitalservice.ris.caselaw.domain.image.ImageUtil;
import de.bund.digitalservice.ris.caselaw.domain.mapper.PatchMapperService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DatabasePatchMapperService implements PatchMapperService {
  @SuppressWarnings("java:S5852")
  private static final Pattern TEXT_CHECK_PATTERN =
      Pattern.compile("<text-check.*?>(.*?)</text-check>");

  private final ObjectMapper objectMapper;
  private final DatabaseDocumentationUnitPatchRepository repository;
  private final AttachmentService attachmentService;

  public DatabasePatchMapperService(
      ObjectMapper objectMapper,
      DatabaseDocumentationUnitPatchRepository repository,
      AttachmentService attachmentService) {
    this.objectMapper = objectMapper;
    this.repository = repository;
    this.attachmentService = attachmentService;
  }

  @Override
  public DocumentationUnit applyPatchToEntity(JsonPatch patch, DocumentationUnit targetEntity) {
    DocumentationUnit documentationUnit;

    try {
      JsonNode jsonNode = objectMapper.convertValue(targetEntity, JsonNode.class);
      JsonNode updatedNode = patch.apply(jsonNode);
      documentationUnit = objectMapper.treeToValue(updatedNode, DocumentationUnit.class);
    } catch (JsonProcessingException | JsonPatchException e) {
      throw new DocumentationUnitPatchException("Couldn't apply patch", e);
    }

    return documentationUnit;
  }

  @Override
  public JsonPatch getDiffPatch(DocumentationUnit existed, DocumentationUnit updated) {
    return JsonDiff.asJsonPatch(
        objectMapper.convertValue(existed, JsonNode.class),
        objectMapper.convertValue(updated, JsonNode.class));
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
  public JsonPatch addUpdatePatch(JsonPatch toUpdate, JsonPatch toSaveJsonPatch) {
    List<JsonPatchOperation> operations = new ArrayList<>(toUpdate.getOperations());
    operations.addAll(toSaveJsonPatch.getOperations());
    return new JsonPatch(operations);
  }

  @Override
  public void savePatch(JsonPatch patch, UUID documentationUnitId, Long documentationUnitVersion) {
    List<JsonPatchOperation> patchWithoutVersion =
        patch.getOperations().stream().filter(op -> !op.getPath().equals("/version")).toList();

    if (patchWithoutVersion.isEmpty()) {
      return;
    }

    try {
      String patchJson = objectMapper.writeValueAsString(new JsonPatch(patchWithoutVersion));
      DocumentationUnitPatchDTO dto =
          DocumentationUnitPatchDTO.builder()
              .documentationUnitId(documentationUnitId)
              .documentationUnitVersion(
                  documentationUnitVersion == null ? 0 : documentationUnitVersion)
              .patch(patchJson)
              .build();
      repository.save(dto);
    } catch (JsonProcessingException e) {
      throw new DocumentationUnitPatchException("Couldn't save patch", e);
    }
  }

  @Override
  public JsonPatch calculatePatch(UUID documentationUnitId, Long frontendDocumentationUnitVersion) {
    List<JsonPatchOperation> operations = new ArrayList<>();

    repository
        .findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
            documentationUnitId, frontendDocumentationUnitVersion)
        .forEach(
            patch -> {
              try {
                JsonPatch jsonPatch = objectMapper.readValue(patch.getPatch(), JsonPatch.class);
                operations.addAll(jsonPatch.getOperations());
              } catch (JsonProcessingException e) {
                throw new DocumentationUnitPatchException(
                    "Couldn't read patch information from database", e);
              }
            });

    return new JsonPatch(operations);
  }

  @Override
  public RisJsonPatch handlePatchForSamePath(
      DocumentationUnit existingDocumentationUnit,
      JsonPatch patch1,
      JsonPatch patch2,
      JsonPatch patch3) {
    Map<String, List<JsonPatchOperation>> pathList2 =
        patch2.getOperations().stream().collect(Collectors.groupingBy(JsonPatchOperation::getPath));
    Map<String, List<JsonPatchOperation>> pathList3 =
        patch3.getOperations().stream().collect(Collectors.groupingBy(JsonPatchOperation::getPath));

    List<String> errorPaths = new ArrayList<>();
    List<JsonPatchOperation> operations = new ArrayList<>(patch1.getOperations());
    for (Entry<String, List<JsonPatchOperation>> entry : pathList3.entrySet()) {
      if (pathList2.containsKey(entry.getKey())) {
        List<JsonPatchOperation> toRemove = pathList2.get(entry.getKey());
        log.debug("remove path '{}': {}", entry.getKey(), toRemove);
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
            });
        operations.addAll(entry.getValue());
        errorPaths.add(entry.getKey());
      } else {
        log.debug("add backend patches '{}'", entry.getValue());
        operations.addAll(entry.getValue());
      }
    }

    return new RisJsonPatch(0L, new JsonPatch(operations), errorPaths);
  }

  @Override
  public JsonPatch removeTextCheckTags(JsonPatch patch) {
    List<JsonPatchOperation> operations = new ArrayList<>();

    patch
        .getOperations()
        .forEach(
            operation -> {
              if (operation instanceof PathValueOperation valueOperation
                  && valueOperation.getValue() instanceof TextNode valueNode) {
                String value = valueNode.textValue();
                Matcher matcher = TEXT_CHECK_PATTERN.matcher(value);
                StringBuilder builder = new StringBuilder();
                while (matcher.find()) {
                  matcher.appendReplacement(builder, matcher.group(1));
                }
                matcher.appendTail(builder);
                if (operation instanceof AddOperation) {
                  operations.add(
                      new AddOperation(valueOperation.getPath(), new TextNode(builder.toString())));
                } else if (operation instanceof ReplaceOperation) {
                  operations.add(
                      new ReplaceOperation(
                          valueOperation.getPath(), new TextNode(builder.toString())));
                }
              } else {
                operations.add(operation);
              }
            });

    return new JsonPatch(operations);
  }

  /**
   * Extracts base64 images from HTML content in the given JSON patch, stores them as attachments,
   * and replaces the image sources with URLs pointing to the stored images.
   */
  @Override
  public JsonPatch extractAndStoreBase64Images(
      JsonPatch patch, DocumentationUnit documentationUnit, User user) {
    List<JsonPatchOperation> operations = new ArrayList<>();

    patch
        .getOperations()
        .forEach(
            operation -> {
              if (operation instanceof PathValueOperation valueOperation
                  && valueOperation.getValue() instanceof TextNode valueNode) {
                try {
                  String originalText = valueNode.textValue();
                  Document document = Jsoup.parse(originalText);
                  document.outputSettings().prettyPrint(false);

                  var elements = ImageUtil.extractBase64ImageTags(document);

                  for (Element base64ImageTag : elements) {
                    HttpHeaders headers = new HttpHeaders();
                    String fileName = "." + ImageUtil.getFileExtension(base64ImageTag);
                    headers.set("X-Filename", fileName);

                    MediaType contentType = MediaTypeFactory.getMediaType(fileName).orElse(null);
                    headers.setContentType(contentType);

                    var byteBuffer = ImageUtil.encodeToBytes(base64ImageTag);
                    Attachment attachment =
                        attachmentService.attachFileToDocumentationUnit(
                            documentationUnit.uuid(), byteBuffer, headers, null);

                    base64ImageTag.replaceWith(
                        ImageUtil.createImageElementWithNewSrc(
                            base64ImageTag, attachment.name(), documentationUnit.documentNumber()));
                  }
                  if (operation instanceof AddOperation) {
                    operations.add(
                        new AddOperation(
                            valueOperation.getPath(), new TextNode(document.body().html())));
                  } else if (operation instanceof ReplaceOperation) {
                    operations.add(
                        new ReplaceOperation(
                            valueOperation.getPath(), new TextNode(document.body().html())));
                  }
                } catch (Exception e) {
                  log.info("Could not process image: ", e);
                  operations.add(operation);
                }
              } else {
                operations.add(operation);
              }
            });

    return new JsonPatch(operations);
  }
}
