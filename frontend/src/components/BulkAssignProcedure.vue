<script setup lang="ts">
import { ref } from "vue"
import AssignProcedure from "@/components/AssignProcedure.vue"
import { InfoStatus } from "@/components/enumInfoStatus"
import InfoModal from "@/components/InfoModal.vue"
import LoadingSpinner from "@/components/LoadingSpinner.vue"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import { Procedure } from "@/domain/procedure"
import { PublicationState } from "@/domain/publicationStatus"
import DocumentUnitService from "@/services/documentUnitService"

const props = defineProps<{
  documentationUnits: DocumentUnitListEntry[] | undefined
}>()
const emit =
  defineEmits<
    (
      e: "updateSelectionErrors",
      error: string,
      documentationUnitIdsWithErrors: string[],
    ) => void
  >()

const hasBulkAssignError = ref(false)
const isLoading = ref(false)

const assignProcedure = async (procedure: Procedure) => {
  hasBulkAssignError.value = false
  if (!areSelectedDocUnitsValid()) {
    return
  }

  isLoading.value = true

  const documentationUnitIds =
    props.documentationUnits?.map((docUnit) => docUnit.uuid!) ?? []
  const { error } = await DocumentUnitService.bulkAssignProcedure(
    procedure.label,
    documentationUnitIds,
  )

  isLoading.value = false
  hasBulkAssignError.value = !!error
}

const areSelectedDocUnitsValid = () => {
  if (!props.documentationUnits || props.documentationUnits.length === 0) {
    emit(
      "updateSelectionErrors",
      "Wählen Sie mindestens eine Dokumentationseinheit aus",
      [],
    )
    return false
  }
  const pendingDocUnitIds = props.documentationUnits
    .filter(
      (docUnit) =>
        docUnit.status?.publicationStatus ===
        PublicationState.EXTERNAL_HANDOVER_PENDING,
    )
    .map((docUnit) => docUnit.uuid!)
  if (pendingDocUnitIds.length > 0) {
    emit(
      "updateSelectionErrors",
      "Nehmen Sie die Fremdanlage(n) an, um sie zu einem Vorgang hinzuzufügen",
      pendingDocUnitIds,
    )
    return false
  }
  return true
}
</script>

<template>
  <InfoModal
    v-if="hasBulkAssignError"
    class="mb-16"
    data-testid="bulk-assign-procedure-error"
    description="Bitte laden Sie die Seite neu."
    :status="InfoStatus.ERROR"
    title="Die Dokumentationseinheit(en) konnten nicht zum Vorgang hinzugefügt werden."
  />
  <div class="flex justify-end">
    <LoadingSpinner v-if="isLoading" class="mt-10 mr-4" size="small" />
    <AssignProcedure @assign-procedure="assignProcedure" />
  </div>
</template>
