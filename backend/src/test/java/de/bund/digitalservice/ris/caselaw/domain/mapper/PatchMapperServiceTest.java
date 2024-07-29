package de.bund.digitalservice.ris.caselaw.domain.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.gravity9.jsonpatch.AddOperation;
import com.gravity9.jsonpatch.JsonPatch;
import com.gravity9.jsonpatch.JsonPatchOperation;
import com.gravity9.jsonpatch.RemoveOperation;
import com.gravity9.jsonpatch.ReplaceOperation;
import de.bund.digitalservice.ris.caselaw.adapter.DatabasePatchMapperService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitPatchRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitPatchDTO;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.RisJsonPatch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Disabled;
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
  void testApplyPatchToEntity() throws JsonProcessingException {
    List<JsonPatchOperation> operations = new ArrayList<>();
    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .coreData(CoreData.builder().appraisalBody("initial value").build())
            .uuid(UUID.randomUUID())
            .documentNumber("ABCDE20220001")
            .build();

    JsonNode valueToReplace = mapper.readTree("\"newValue\"");
    JsonPatchOperation replaceOp = new ReplaceOperation("/coreData/appraisalBody", valueToReplace);
    operations.add(replaceOp);
    JsonPatch patch = new JsonPatch(operations);

    documentUnit = service.applyPatchToEntity(patch, documentUnit);
    assertThat(documentUnit.coreData().appraisalBody()).isEqualTo("newValue");
  }

  @Test
  void testHandlePatchForSamePath_withNoSamePathPatches_returnFalseAndDontChangePatchList() {
    JsonPatchOperation addOperationFE =
        new AddOperation("/coreData/fileNumbers/1", new TextNode("abc"));
    JsonPatch patchFE = new JsonPatch(List.of(addOperationFE));
    JsonPatchOperation addOperationBE =
        new AddOperation("/coreData/decisionDate", new TextNode("20.03.2014"));
    JsonPatch patchBE = new JsonPatch(List.of(addOperationBE));
    JsonPatch toFEPatch = new JsonPatch(Collections.emptyList());
    DocumentUnit existingDocumentUnit =
        DocumentUnit.builder()
            .coreData(CoreData.builder().fileNumbers(List.of("123")).build())
            .build();

    RisJsonPatch result =
        service.handlePatchForSamePath(existingDocumentUnit, toFEPatch, patchFE, patchBE);

    assertThat(result.errorPaths()).isEmpty();
    assertThat(result.patch().getOperations()).hasSize(1);
  }

  @Test
  @Disabled("fix and enable")
  void testHandlePatchForSamePath_withSamePathPatchesAddOperation_returnTrueAndRemovePatches() {
    JsonPatchOperation addOperationFE =
        new AddOperation("/coreData/fileNumbers/1", new TextNode("abc"));
    JsonPatch patchFE = new JsonPatch(new ArrayList<>(List.of(addOperationFE)));
    JsonPatchOperation addOperationBE =
        new AddOperation("/coreData/fileNumbers/1", new TextNode("xyz"));
    JsonPatch patchBE = new JsonPatch(new ArrayList<>(List.of(addOperationBE)));
    JsonPatch toFEPatch = new JsonPatch(Collections.emptyList());
    DocumentUnit existingDocumentUnit =
        DocumentUnit.builder().coreData(CoreData.builder().fileNumbers(List.of()).build()).build();

    RisJsonPatch result =
        service.handlePatchForSamePath(existingDocumentUnit, toFEPatch, patchFE, patchBE);

    assertThat(result.errorPaths()).containsExactly("/coreData/fileNumbers/1");
    assertThat(result.patch().getOperations())
        .extracting("op", "path")
        .containsExactly(new Tuple("remove", "/coreData/fileNumbers/1"));
  }

  @Test
  @Disabled("fix and enable")
  void testHandlePatchForSamePath_withSamePathPatchesRemoveOperation_returnTrueAndRemovePatches() {
    JsonPatchOperation addOperationFE = new RemoveOperation("/coreData/fileNumbers/1");
    JsonPatch patchFE = new JsonPatch(new ArrayList<>(List.of(addOperationFE)));
    JsonPatchOperation addOperationBE =
        new AddOperation("/coreData/fileNumbers/1", new TextNode("xyz"));
    JsonPatch patchBE = new JsonPatch(new ArrayList<>(List.of(addOperationBE)));
    JsonPatch toFEPatch = new JsonPatch(Collections.emptyList());
    DocumentUnit existingDocumentUnit =
        DocumentUnit.builder()
            .coreData(CoreData.builder().fileNumbers(List.of("abc", "123")).build())
            .build();

    RisJsonPatch result =
        service.handlePatchForSamePath(existingDocumentUnit, toFEPatch, patchFE, patchBE);

    assertThat(result.errorPaths()).containsExactly("/coreData/fileNumbers/1");
    assertThat(result.patch().getOperations())
        .extracting("op", "path", "value")
        .containsExactly(new Tuple("add", "/coreData/fileNumbers/1", new TextNode("123")));
  }

  @Test
  @Disabled("fix and enable")
  void testHandlePatchForSamePath_withSamePathPatchesReplaceOperation_returnTrueAndRemovePatches() {
    JsonPatchOperation replaceOperationFE =
        new ReplaceOperation("/coreData/fileNumbers/1", new TextNode("abc"));
    JsonPatch patchFE = new JsonPatch(new ArrayList<>(List.of(replaceOperationFE)));
    JsonPatchOperation addOperationBE =
        new AddOperation("/coreData/fileNumbers/1", new TextNode("xyz"));
    JsonPatch patchBE = new JsonPatch(new ArrayList<>(List.of(addOperationBE)));
    JsonPatch toFEPatch = new JsonPatch(Collections.emptyList());
    DocumentUnit existingDocumentUnit =
        DocumentUnit.builder()
            .coreData(CoreData.builder().fileNumbers(List.of("123")).build())
            .build();

    RisJsonPatch result =
        service.handlePatchForSamePath(existingDocumentUnit, toFEPatch, patchFE, patchBE);

    assertThat(result.errorPaths()).containsExactly("/coreData/fileNumbers/1");
    assertThat(result.patch().getOperations()).isEmpty();
  }

  @Test
  @Disabled("fix and enable")
  void
      testHandlePatchForSamePath_withSamePathPatchesAndOtherInBE_returnTrueAndRemoveOnlySamePathPatches() {
    JsonPatchOperation addOperationFE =
        new AddOperation("/coreData/fileNumbers/1", new TextNode("abc"));
    JsonPatchOperation replaceOperationFE =
        new ReplaceOperation("/coreData/decisionDate", new TextNode("20.01.2000"));
    JsonPatch patchFE = new JsonPatch(new ArrayList<>(List.of(addOperationFE, replaceOperationFE)));
    JsonPatchOperation addOperationBE =
        new AddOperation("/coreData/fileNumbers/1", new TextNode("xyz"));
    JsonPatch patchBE = new JsonPatch(new ArrayList<>(List.of(addOperationBE)));
    JsonPatch toFEPatch = new JsonPatch(Collections.emptyList());
    DocumentUnit existingDocumentUnit =
        DocumentUnit.builder()
            .coreData(CoreData.builder().fileNumbers(List.of("123")).build())
            .build();

    RisJsonPatch result =
        service.handlePatchForSamePath(existingDocumentUnit, toFEPatch, patchFE, patchBE);

    assertThat(result.errorPaths()).containsExactly("/coreData/fileNumbers/1");
    assertThat(result.patch().getOperations())
        .extracting("op", "path")
        .containsExactly(
            new Tuple("replace", "/coreData/decisionDate"),
            new Tuple("remove", "/coreData/fileNumbers/1"));
    assertThat(result.patch().getOperations().get(0))
        .extracting("value")
        .isEqualTo(new TextNode("20.01.2000"));
  }

  @Test
  void testCalculatePath_withoutBackendPatches() {
    UUID documentationUnitId = UUID.randomUUID();
    Mockito.when(
            repository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
                documentationUnitId, 2L))
        .thenReturn(List.of());

    JsonPatch result = service.calculatePatch(documentationUnitId, 2L);

    assertThat(result.getOperations()).isEmpty();
  }

  @Test
  void testCalculatePath_withOneBackendPatches() {
    UUID documentationUnitId = UUID.randomUUID();

    DocumentationUnitPatchDTO patchDTO =
        DocumentationUnitPatchDTO.builder()
            .documentationUnitId(documentationUnitId)
            .documentationUnitVersion(2L)
            .patch("[{\"op\":\"add\",\"path\":\"/decisionDate\",\"value\":\"20.01.2000\"}]")
            .build();
    Mockito.when(
            repository.findByDocumentationUnitIdAndDocumentationUnitVersionGreaterThanEqual(
                documentationUnitId, 1L))
        .thenReturn(List.of(patchDTO));

    JsonPatch result = service.calculatePatch(documentationUnitId, 1L);

    assertThat(result.getOperations()).hasSize(1);
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

    JsonPatch result = service.calculatePatch(documentationUnitId, 0L);

    assertThat(result.getOperations()).hasSize(2);
  }
}
