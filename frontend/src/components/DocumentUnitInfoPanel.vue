<script setup lang="ts" generic="TDocument">
import dayjs from "dayjs"
import { storeToRefs } from "pinia"
import Button from "primevue/button"
import { useToast } from "primevue/usetoast"
import { computed, Ref, ref, toRaw, watchEffect } from "vue"
import { useRoute } from "vue-router"
import AssignProcessStep from "@/components/AssignProcessStep.vue"
import CurrentAndPreviousProcessStepBadge from "@/components/CurrentAndPreviousProcessStepBadge.vue"
import IconBadge from "@/components/IconBadge.vue"
import SaveButton from "@/components/SaveDocumentUnitButton.vue"
import { useInternalUser } from "@/composables/useInternalUser"
import { useStatusBadge } from "@/composables/useStatusBadge"
import { DocumentationUnit } from "@/domain/documentationUnit"
import DocumentationUnitProcessStep from "@/domain/documentationUnitProcessStep"
import { ResponseError } from "@/services/httpClient"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { isDecision } from "@/utils/typeGuards"
import IconError from "~icons/ic/baseline-error"
import IconPerson from "~icons/ic/baseline-person"
import IconApprovalDelegation from "~icons/material-symbols/approval-delegation-outline"

const store = useDocumentUnitStore()
const { documentUnit } = storeToRefs(store) as {
  documentUnit: Ref<DocumentationUnit>
}

const route = useRoute()

const isInternalUser = useInternalUser()

const fileNumberInfo = computed(() => {
  return documentUnit.value.coreData.fileNumbers?.[0] || ""
})

const decisionDateInfo = computed(() => {
  return documentUnit.value.coreData.decisionDate
    ? dayjs(documentUnit.value.coreData.decisionDate).format("DD.MM.YYYY")
    : ""
})

const hasPendingDuplicateWarning = computed(() => {
  if (isDecision(documentUnit.value)) {
    return (documentUnit.value.managementData.duplicateRelations ?? []).some(
      (warning) => warning.status === "PENDING",
    )
  }
  return false
})

const courtInfo = computed(() => {
  return documentUnit.value.coreData.court?.label || ""
})

const formattedInfo = computed(() => {
  const parts = [
    courtInfo.value,
    fileNumberInfo.value,
    decisionDateInfo.value,
  ].filter((part) => part.trim() !== "")
  return parts.join(", ")
})

const statusBadge = ref(useStatusBadge(documentUnit.value.status).value)

const isRouteWithSaveButton = computed(
  () =>
    route.path.includes("categories") ||
    route.path.includes("attachments") ||
    route.path.includes("references") ||
    route.path.includes("managementdata"),
)

const hasErrorStatus = computed(() => documentUnit.value.status?.withError)
const managementDataRoute = computed(() => ({
  name: "caselaw-documentUnit-documentNumber-managementdata",
  params: { documentNumber: documentUnit.value.documentNumber },
}))

const processStepsEnabled = isDecision(documentUnit.value)

const showProcessStepDialog = ref(false)
const toast = useToast()

async function handleAssignProcessStep(
  documentationUnitProcessStep: DocumentationUnitProcessStep,
): Promise<ResponseError | undefined> {
  documentUnit.value!.currentDocumentationUnitProcessStep =
    documentationUnitProcessStep
  const response = await store.updateDocumentUnit()
  if (response.error) {
    return response.error
  }

  toast.add({
    severity: "success",
    summary: "Weitergeben erfolgreich",
    life: 5_000,
  })
  showProcessStepDialog.value = false
  return undefined
}

watchEffect(() => {
  statusBadge.value = useStatusBadge(documentUnit.value.status).value
})
</script>

<template>
  <div
    class="sticky top-0 z-30 flex h-[64px] flex-row items-center border-b border-solid border-gray-400 bg-blue-100 px-24 py-12"
    data-testid="document-unit-info-panel"
  >
    <h1 class="ris-body1-bold">{{ documentUnit.documentNumber }}</h1>
    <span v-if="formattedInfo.length > 0" class="m-4"> | </span>
    <span
      class="overflow-hidden text-ellipsis whitespace-nowrap"
      data-testid="document-unit-info-panel-items"
    >
      {{ formattedInfo }}</span
    >
    <div class="flex flex-row gap-12">
      <IconBadge
        v-if="statusBadge"
        :background-color="statusBadge.backgroundColor"
        class="ml-12"
        :icon="toRaw(statusBadge.icon)"
        :label="statusBadge.label"
      />
      <IconBadge
        v-if="hasErrorStatus"
        background-color="bg-red-300"
        :icon="IconError"
        icon-color="text-red-900"
        label="Fehler"
      />

      <CurrentAndPreviousProcessStepBadge
        v-if="processStepsEnabled"
        :current-process-step="
          documentUnit.currentDocumentationUnitProcessStep?.processStep
        "
        :previous-process-step="documentUnit.previousProcessStep"
      />
      <IconBadge
        v-if="
          documentUnit.currentDocumentationUnitProcessStep &&
          processStepsEnabled
        "
        background-color="bg-white"
        border-color="border-gray-800"
        class="px-8"
        data-testid="info-panel-process-step-initials"
        :icon="IconPerson"
        :label="
          documentUnit.currentDocumentationUnitProcessStep?.user?.initials ||
          '-'
        "
      />
    </div>

    <span class="flex-grow"></span>
    <div
      v-if="hasPendingDuplicateWarning"
      class="flex items-center gap-12 whitespace-nowrap"
    >
      <IconBadge
        background-color="bg-red-300"
        class="ml-12"
        data-testid="duplicate-icon"
        :icon="IconError"
        icon-color="text-red-900"
        label="Dublettenverdacht"
      />
      <RouterLink
        v-if="isInternalUser"
        class="ris-link1-bold text-red-900"
        :to="managementDataRoute"
      >
        Bitte prüfen</RouterLink
      >
      <span v-if="isRouteWithSaveButton && isInternalUser">|</span>
    </div>

    <SaveButton
      v-if="isRouteWithSaveButton"
      aria-label="Speichern Button"
      data-testid="document-unit-save-button"
    >
      <Button
        v-if="processStepsEnabled"
        v-tooltip.bottom="'Dokumentationseinheit weitergeben'"
        aria-label="Dokumentationseinheit weitergeben"
        severity="secondary"
        size="small"
        @click="showProcessStepDialog = true"
      >
        <template #icon>
          <IconApprovalDelegation />
        </template>
      </Button>
    </SaveButton>
    <AssignProcessStep
      v-model:visible="showProcessStepDialog"
      :documentation-unit="documentUnit"
      :handle-assign-process-step="handleAssignProcessStep"
    />
  </div>
</template>
