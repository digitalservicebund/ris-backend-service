<script setup lang="ts">
import { storeToRefs } from "pinia"
import Button from "primevue/button"
import Menu from "primevue/menu"
import { useToast } from "primevue/usetoast"
import { ref } from "vue"
import AssignProcessStep from "@/components/AssignProcessStep.vue"
import DocumentationUnitProcessStep from "@/domain/documentationUnitProcessStep"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import { PublicationState } from "@/domain/publicationStatus"
import DocumentUnitService from "@/services/documentUnitService"
import { ResponseError } from "@/services/httpClient"
import useSessionStore from "@/stores/sessionStore"
import IconLayers from "~icons/ic/layers"
import IconApprovalDelegation from "~icons/material-symbols/approval-delegation-outline"

const props = defineProps<{
  documentationUnits: DocumentUnitListEntry[] | undefined
}>()
const emit = defineEmits<{
  (
    e: "updateSelectionErrors",
    error: string | undefined,
    documentationUnitIdsWithErrors: string[],
  ): void
  (e: "processStepAssigned"): void
}>()
const { user } = storeToRefs(useSessionStore())
const toast = useToast()
const showProcessStepDialog = ref(false)

/**
 * Flag to control Menu visibility via v-if.
 * The built-in PrimeVue `:popup="true"` prop relies on asynchronous Portal/Teleport
 * rendering, which leads to flaky and failing Playwright E2E tests.
 * Using v-if and absolute positioning makes the menu inline and stable.
 */
const menuVisible = ref(false)

async function handleAssignProcessStep(
  documentationUnitProcessStep: DocumentationUnitProcessStep,
): Promise<ResponseError | undefined> {
  const documentationUnitIds =
    props.documentationUnits?.map((docUnit) => docUnit.uuid!) ?? []

  const { error } = await DocumentUnitService.bulkAssignProcessStep(
    {
      processStep: documentationUnitProcessStep.processStep,
      user: documentationUnitProcessStep.user,
    },
    documentationUnitIds,
  )

  if (error) {
    return error
  } else {
    emit("processStepAssigned")
    showProcessStepDialog.value = false

    let detailMessage = `Die Dokumentationseinheiten sind jetzt im Schritt ${documentationUnitProcessStep.processStep?.name}`
    if (documentationUnitProcessStep.user) {
      detailMessage += ` und der Person ${documentationUnitProcessStep.user.name} zugewiesen`
    }
    detailMessage += `.`

    toast.add({
      severity: "success",
      summary: "Weitergeben erfolgreich",
      detail: detailMessage,
      life: 5000,
    })
    return undefined
  }
}

const showAssignProcessStepDialog = () => {
  if (isSelectionValid()) {
    showProcessStepDialog.value = true
  }
  // Close the menu regardless of validation success/failure
  menuVisible.value = false
}

const menuModel = ref([
  {
    label: "Weitergeben",
    component: IconApprovalDelegation,
    command: showAssignProcessStepDialog,
  },
])

const toggleMenu = () => {
  menuVisible.value = !menuVisible.value
}

const isSelectionValid = () => {
  const notSameDocumentationOffice =
    props.documentationUnits?.filter(
      (unit) =>
        unit.documentationOffice?.abbreviation !==
        user.value?.documentationOffice?.abbreviation,
    ) ?? []

  const externalHandoverPending =
    props.documentationUnits?.filter(
      (unit) =>
        unit.status?.publicationStatus ===
        PublicationState.EXTERNAL_HANDOVER_PENDING,
    ) ?? []

  const errorIds = notSameDocumentationOffice
    .map((unit) => unit.uuid!)
    .concat(externalHandoverPending.map((unit) => unit.uuid!))

  // Fremde Dokeinheiten in Selektion
  if (notSameDocumentationOffice.length > 0) {
    emit(
      "updateSelectionErrors",
      "Dokumentationseinheiten von fremden Dokstellen können nicht bearbeitet werden.",
      errorIds,
    )
    return false
  }

  // Fremdanlage in Selektion
  if (externalHandoverPending.length > 0) {
    emit(
      "updateSelectionErrors",
      "Nehmen Sie die Fremdanlage(n) im Eingang an, um sie bearbeiten zu können.",
      errorIds,
    )
    return false
  }

  // Keine Dokeinheit ausgewählt
  if (!props.documentationUnits || props.documentationUnits.length === 0) {
    emit(
      "updateSelectionErrors",
      "Wählen Sie mindestens eine Dokumentationsseinheit aus.",
      [],
    )
    return false
  }

  emit("updateSelectionErrors", undefined, [])
  return true
}
</script>

<template>
  <div class="flex flex-col gap-4">
    <div class="flex flex-row justify-end">
      <div class="relative inline-flex">
        <Button
          v-tooltip.bottom="{
            value: 'Aktionen',
            appendTo: 'body',
          }"
          aria-label="Aktionen"
          class="z-10"
          severity="secondary"
          size="small"
          @click="toggleMenu"
        >
          <template #icon>
            <IconLayers />
          </template>
        </Button>
        <Menu
          v-if="menuVisible"
          class="absolute top-full right-0 mt-1 min-w-[225px]"
          :model="menuModel"
        >
          <template #item="{ item, props: slotProps }">
            <a class="min-w-225 cursor-pointer" v-bind="slotProps.action">
              <span class="p-menuitem-icon">
                <component :is="item.component" />
              </span>
              <span class="p-menuitem-text">{{ item.label }}</span>
            </a>
          </template>
        </Menu>
      </div>
    </div>
    <AssignProcessStep
      v-model:visible="showProcessStepDialog"
      :handle-assign-process-step="handleAssignProcessStep"
    />
  </div>
</template>
