package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.fge.jsonpatch.Patch;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MergeableJsonPatch implements JsonSerializable, Patch {
  private List<JsonPatchOperation> operations;

  public MergeableJsonPatch() {
    operations = new ArrayList<>();
  }

  @JsonCreator
  public MergeableJsonPatch(List<JsonPatchOperation> operations) {
    this.operations = operations;
  }

  public List<JsonPatchOperation> getOperations() {
    return operations;
  }

  public void addOperations(List<JsonPatchOperation> operations) {
    this.operations.addAll(operations);
  }

  @Override
  public String toString() {
    return operations.toString();
  }

  @Override
  public void serialize(final JsonGenerator jgen, final SerializerProvider provider)
      throws IOException {
    jgen.writeStartArray();

    for (final JsonPatchOperation op : operations) op.serialize(jgen, provider);

    jgen.writeEndArray();
  }

  @Override
  public void serializeWithType(
      final JsonGenerator jgen, final SerializerProvider provider, final TypeSerializer typeSer)
      throws IOException {
    serialize(jgen, provider);
  }

  @Override
  public JsonNode apply(JsonNode node) throws JsonPatchException {
    JsonNode ret = node;

    for (final JsonPatchOperation operation : operations) ret = operation.apply(ret);

    return ret;
  }
}
