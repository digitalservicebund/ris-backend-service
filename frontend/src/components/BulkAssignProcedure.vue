<script setup lang="ts">
import { useToast } from "primevue/usetoast"
import { ref } from "vue"
import AssignProcedure from "@/components/AssignProcedure.vue"
import { InfoStatus } from "@/components/enumInfoStatus"
import InfoModal from "@/components/InfoModal.vue"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import { Procedure } from "@/domain/procedure"
import { PublicationState } from "@/domain/publicationStatus"
import DocumentUnitService from "@/services/documentUnitService"

const props = defineProps<{
  documentationUnits: DocumentUnitListEntry[] | undefined
}>()
const emit = defineEmits<{
  (
    e: "updateSelectionErrors",
    error: string | undefined,
    documentationUnitIdsWithErrors: string[],
  ): void
  (e: "procedureAssigned"): void
}>()

const hasBulkAssignError = ref(false)
const toast = useToast()

const assignProcedure = async (procedure?: Procedure) => {
  hasBulkAssignError.value = false
  if (!areSelectedDocUnitsValid()) {
    return
  }

  if (!procedure) {
    return
  }
  const documentationUnitIds =
    props.documentationUnits?.map((docUnit) => docUnit.uuid!) ?? []
  const { error } = await DocumentUnitService.bulkAssignProcedure(
    procedure.label,
    documentationUnitIds,
  )

  if (!error) {
    emit("procedureAssigned")
    const isPlural = documentationUnitIds.length > 1
    const verb = isPlural ? "en sind" : " ist"
    toast.add({
      severity: "success",
      summary: "Hinzufügen erfolgreich",
      detail: `Die Dokumentationseinheit${verb} jetzt im Vorgang ${procedure.label}.`,
      life: 5_000,
    })
  }

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
    const noun = pendingDocUnitIds.length > 1 ? "Fremdanlagen" : "Fremdanlage"
    emit(
      "updateSelectionErrors",
      `Nehmen Sie die ${noun} an, um sie zu einem Vorgang hinzuzufügen`,
      pendingDocUnitIds,
    )
    return false
  }
  emit("updateSelectionErrors", undefined, [])
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
  <AssignProcedure class="justify-end" @assign-procedure="assignProcedure" />
</template>
