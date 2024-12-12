package de.bund.digitalservice.ris.caselaw.domain.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.gravity9.jsonpatch.JsonPatch;
import com.gravity9.jsonpatch.JsonPatchOperation;
import com.gravity9.jsonpatch.ReplaceOperation;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PatchMapperServiceTest {

  @Test
  void containsOnlyVersionInPatch_withVersionOnly_shouldReturnTrue() {
    JsonNode valueToReplace = new TextNode("0");
    JsonPatchOperation replaceOp = new ReplaceOperation("/version", valueToReplace);

    Assertions.assertTrue(
        PatchMapperService.containsOnlyVersionInPatch(new JsonPatch(List.of(replaceOp))));
  }

  @Test
  void containsOnlyVersionInPatch_withVersionAndOtherOps_shouldReturnFalse() {
    JsonNode valueToReplace = new TextNode("0");
    List<JsonPatchOperation> replaceOps = new ArrayList<>();
    replaceOps.add(new ReplaceOperation("/version", valueToReplace));
    replaceOps.add(new ReplaceOperation("/fileNumber", valueToReplace));
    Assertions.assertFalse(
        PatchMapperService.containsOnlyVersionInPatch(new JsonPatch(replaceOps)));
  }
}
