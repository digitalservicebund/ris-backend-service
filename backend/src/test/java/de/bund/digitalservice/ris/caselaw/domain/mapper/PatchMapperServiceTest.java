package de.bund.digitalservice.ris.caselaw.domain.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.fge.jsonpatch.ReplaceOperation;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Import({PatchMapperService.class, ObjectMapper.class})
@ExtendWith(SpringExtension.class)
class PatchMapperServiceTest {

  @Autowired private PatchMapperService patchMapperService;
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
    JsonPatch patch = new JsonPatch(operations);

    documentUnit = patchMapperService.applyPatchToEntity(patch, documentUnit, DocumentUnit.class);
    Assertions.assertEquals(documentUnit.coreData().appraisalBody(), "newValue");
  }
}
