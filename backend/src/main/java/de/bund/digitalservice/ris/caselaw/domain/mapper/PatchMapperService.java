package de.bund.digitalservice.ris.caselaw.domain.mapper;

import com.gravity9.jsonpatch.JsonPatch;
import com.gravity9.jsonpatch.JsonPatchOperation;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.RisJsonPatch;
import de.bund.digitalservice.ris.caselaw.domain.User;
import java.util.List;
import java.util.UUID;

/** A service to enable partial updates of object by patch */
public interface PatchMapperService {

  /**
   * Calculate the patch with the changes which doesn't exist in the client (frontend).
   *
   * @param uuid id of the documentation unit
   * @param documentationUnitVersion version of the documentation in the client (frontend)
   * @return a patch with for the client relevant operations
   */
  JsonPatch calculatePatch(UUID uuid, Long documentationUnitVersion);

  /**
   * Handle operations which are in both patch. Remove all operation with the same path from the
   * first patch. Add the path in the error path list. Special cases add and remove operations:
   *
   * <ol>
   *   <li>Add operation: Generate a remove operation for this path
   *   <li>Remove operation: Generate a add operation for this path with the value of the existing
   *       documentation unit
   * </ol>
   *
   * @param existingDocumentationUnit documentation unit in the database before the patch is applied
   * @param patch1 calculated patch for the client (frontend)
   * @param patch2 patch with operations which doesn't exist in the client (frontend)
   * @return extended JsonPatch {@link RisJsonPatch} with version information and error paths
   */
  RisJsonPatch handlePatchForSamePath(
      DocumentationUnit existingDocumentationUnit,
      JsonPatch patch1,
      JsonPatch patch2,
      JsonPatch patch3);

  /**
   * Save the patch for the new version. Diff of the updated documentation unit to the existing
   * documentation unit. The updated documentation unit contains the applied patch operation and the
   * automatically generated fields.
   *
   * @param patch patch to save
   * @param uuid id of the documentation unit
   * @param version version of the documentation unit which contains this patch
   */
  void savePatch(JsonPatch patch, UUID uuid, Long version);

  /**
   * Apply the patch to the existing documentation unit.
   *
   * @param patch patch to apply
   * @param existingDocumentationUnit existing documentation unit
   * @return result of the existing documentation and the applied patch
   */
  DocumentationUnit applyPatchToEntity(
      JsonPatch patch, DocumentationUnit existingDocumentationUnit);

  /**
   * Generate a patch between the two documentation units.
   *
   * @param existed old documentation unit
   * @param updated new documentation unit
   * @return patch with the diff between both values
   */
  JsonPatch getDiffPatch(DocumentationUnit existed, DocumentationUnit updated);

  /**
   * Generate a patch without operation on the same path
   *
   * @param patch1 patch to remove operations
   * @param patch2 patch to check for same path
   * @return new patch without operation on the same path
   */
  JsonPatch removePatchForSamePath(JsonPatch patch1, JsonPatch patch2);

  JsonPatch addUpdatePatch(JsonPatch toUpdate, JsonPatch toSaveJsonPatch);

  /**
   * Returns true if the JSON patch contains includes only version replace op, to prevent increasing
   * the version if not needed.
   *
   * @param patch The JSON patch to check.
   * @return true if the patch contains only version op, otherwise false.
   */
  static boolean containsOnlyVersionInPatch(JsonPatch patch) {
    if (patch == null || patch.getOperations() == null) {
      return false;
    }
    List<JsonPatchOperation> operations = patch.getOperations();
    return operations.size() == 1 && "/version".equals(operations.getFirst().getPath());
  }

  /**
   * Returns true if the JSON patch contains updates only for long texts and also that these changes
   * to long texts are only insertions of text check tags. We should not update the version if there
   * was no actual change to the long text.
   *
   * @param patch The JSON patch to check.
   * @return true if after removal of text check tags, the text is the same as the stored text.
   */
  JsonPatch removeCustomTagsAndCompareContentForDiff(
      JsonPatch patch, DocumentationUnit documentationUnit);

  /**
   * Remove all informational tags for the text check.
   *
   * @param patch with text check tags
   * @return patch without text check tags.
   */
  JsonPatch removeTextCheckTags(JsonPatch patch);

  /**
   * Saves all base64 encoded images in src attributes of img tags as attachments and replace the
   * src attribute with the respective api path
   */
  JsonPatch extractAndStoreBase64Images(
      JsonPatch patch, DocumentationUnit documentationUnit, User user);
}
