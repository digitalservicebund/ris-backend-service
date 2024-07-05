package de.bund.digitalservice.ris.caselaw.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import com.github.fge.jsonpatch.AddOperation;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.fge.jsonpatch.ReplaceOperation;
import de.bund.digitalservice.ris.caselaw.adapter.DatabasePatchMapperService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitPatchRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitPatchDTO;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.MergeableJsonPatch;
import de.bund.digitalservice.ris.caselaw.domain.RisJsonPatch;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({DatabasePatchMapperService.class, ObjectMapper.class})
class PatchMapperServiceTest {

  @Autowired private PatchMapperService service;

  @MockBean private DatabaseDocumentationUnitPatchRepository repository;
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void testApplyPatchToEntity() throws JsonPatchException, JsonProcessingException {
    List<JsonPatchOperation> operations = new ArrayList<>();
    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .coreData(CoreData.builder().appraisalBody("initial value").build())
            .uuid(UUID.randomUUID())
            .documentNumber("ABCDE20220001")
            .build();

    JsonNode valueToReplace = mapper.readTree("\"newValue\"");
    JsonPatchOperation replaceOp =
        new ReplaceOperation(JsonPointer.of("coreData", "appraisalBody"), valueToReplace);
    operations.add(replaceOp);
    MergeableJsonPatch patch = new MergeableJsonPatch();
    patch.addOperations(operations);

    documentUnit = service.applyPatchToEntity(patch, documentUnit, DocumentUnit.class);
    Assertions.assertEquals(documentUnit.coreData().appraisalBody(), "newValue");
  }

  @Test
  void testRemovePatchForSamePath_withNoSamePathPatches_returnFalseAndDontChangePatchList()
      throws JsonPointerException {
    JsonPatchOperation addOperationFE =
        new AddOperation(new JsonPointer("/fileNumber/1"), new TextNode("abc"));
    MergeableJsonPatch patchFE = new MergeableJsonPatch(List.of(addOperationFE));
    JsonPatchOperation addOperationBE =
        new AddOperation(new JsonPointer("/decisionDate"), new TextNode("20.03.2014"));
    MergeableJsonPatch patchBE = new MergeableJsonPatch(List.of(addOperationBE));

    List<String> result = service.removePatchForSamePath(patchFE, patchBE);

    assertThat(result).isEmpty();
    assertThat(patchFE.getOperations()).hasSize(1);
  }

  @Test
  void testRemovePatchForSamePath_withSamePathPatches_returnTrueAndRemovePatches()
      throws JsonPointerException {
    JsonPatchOperation addOperationFE =
        new AddOperation(new JsonPointer("/fileNumber/1"), new TextNode("abc"));
    MergeableJsonPatch patchFE = new MergeableJsonPatch(new ArrayList<>(List.of(addOperationFE)));
    JsonPatchOperation addOperationBE =
        new AddOperation(new JsonPointer("/fileNumber/1"), new TextNode("xyz"));
    MergeableJsonPatch patchBE = new MergeableJsonPatch(new ArrayList<>(List.of(addOperationBE)));

    List<String> result = service.removePatchForSamePath(patchFE, patchBE);

    assertThat(result).containsExactly("/fileNumber/1");
    assertThat(patchFE.getOperations()).hasSize(0);
  }

  @Test
  void
      testRemovePatchForSamePath_withSamePathPatchesAndOtherInBE_returnTrueAndRemoveOnlySamePathPatches()
          throws JsonPointerException {
    JsonPatchOperation addOperationFE =
        new AddOperation(new JsonPointer("/fileNumber/1"), new TextNode("abc"));
    JsonPatchOperation replaceOperationFE =
        new AddOperation(new JsonPointer("/decisionDate"), new TextNode("20.01.2000"));
    MergeableJsonPatch patchFE =
        new MergeableJsonPatch(new ArrayList<>(List.of(addOperationFE, replaceOperationFE)));
    JsonPatchOperation addOperationBE =
        new AddOperation(new JsonPointer("/fileNumber/1"), new TextNode("xyz"));
    MergeableJsonPatch patchBE = new MergeableJsonPatch(new ArrayList<>(List.of(addOperationBE)));

    List<String> result = service.removePatchForSamePath(patchFE, patchBE);

    assertThat(result).containsExactly("/fileNumber/1");
    assertThat(patchFE.getOperations()).containsExactly(replaceOperationFE);
  }

  @Test
  void testCalculatePath_withoutBackendPatches() {
    UUID documentationUnitId = UUID.randomUUID();
    DocumentationUnitPatchDTO patchDTO1 =
        DocumentationUnitPatchDTO.builder()
            .documentationUnitId(documentationUnitId)
            .documentationUnitVersion(1L)
            .patch("[{\"op\":\"add\",\"path\":\"/appraisalBody\",\"value\":\"appraisal body\"}]")
            .build();
    DocumentationUnitPatchDTO patchDTO2 =
        DocumentationUnitPatchDTO.builder()
            .documentationUnitId(documentationUnitId)
            .documentationUnitVersion(2L)
            .patch("[{\"op\":\"add\",\"path\":\"/decisionDate\",\"value\":\"20.01.2000\"}]")
            .build();
    Mockito.when(
            repository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
                documentationUnitId, 2L))
        .thenReturn(List.of());

    RisJsonPatch result = service.calculatePatch(documentationUnitId, 2L, 3L);

    assertThat(result.documentationUnitVersion()).isEqualTo(3L);
    assertThat(result.patch().getOperations()).isEmpty();
  }

  @Test
  void testCalculatePath_withOneBackendPatches() {
    UUID documentationUnitId = UUID.randomUUID();
    DocumentationUnitPatchDTO patchDTO1 =
        DocumentationUnitPatchDTO.builder()
            .documentationUnitId(documentationUnitId)
            .documentationUnitVersion(1L)
            .patch("[{\"op\":\"add\",\"path\":\"/appraisalBody\",\"value\":\"appraisal body\"}]")
            .build();
    DocumentationUnitPatchDTO patchDTO2 =
        DocumentationUnitPatchDTO.builder()
            .documentationUnitId(documentationUnitId)
            .documentationUnitVersion(2L)
            .patch("[{\"op\":\"add\",\"path\":\"/decisionDate\",\"value\":\"20.01.2000\"}]")
            .build();
    Mockito.when(
            repository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
                documentationUnitId, 1L))
        .thenReturn(List.of(patchDTO2));

    RisJsonPatch result = service.calculatePatch(documentationUnitId, 1L, 3L);

    assertThat(result.documentationUnitVersion()).isEqualTo(3L);
    assertThat(result.patch().getOperations()).hasSize(1);
  }

  @Test
  void testCalculatePath_withTwoBackendPatches() {
    UUID documentationUnitId = UUID.randomUUID();
    DocumentationUnitPatchDTO patchDTO1 =
        DocumentationUnitPatchDTO.builder()
            .documentationUnitId(documentationUnitId)
            .documentationUnitVersion(1L)
            .patch("[{\"op\":\"add\",\"path\":\"/appraisalBody\",\"value\":\"appraisal body\"}]")
            .build();
    DocumentationUnitPatchDTO patchDTO2 =
        DocumentationUnitPatchDTO.builder()
            .documentationUnitId(documentationUnitId)
            .documentationUnitVersion(2L)
            .patch("[{\"op\":\"add\",\"path\":\"/decisionDate\",\"value\":\"20.01.2000\"}]")
            .build();
    Mockito.when(
            repository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
                documentationUnitId, 0L))
        .thenReturn(List.of(patchDTO1, patchDTO2));

    RisJsonPatch result = service.calculatePatch(documentationUnitId, 0L, 3L);

    assertThat(result.documentationUnitVersion()).isEqualTo(3L);
    assertThat(result.patch().getOperations()).hasSize(2);
  }
}
