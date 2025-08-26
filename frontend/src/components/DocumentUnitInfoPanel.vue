<script setup lang="ts" generic="TDocument">
import dayjs from "dayjs"
import Button from "primevue/button"
import { useToast } from "primevue/usetoast"
import { computed, ref, toRaw, watchEffect } from "vue"
import { useRoute } from "vue-router"
import CurrentAndLastProcessStepBadge from "@/components/CurrentAndLastProcessStepBadge.vue"
import IconBadge from "@/components/IconBadge.vue"
import SaveButton from "@/components/SaveDocumentUnitButton.vue"
import UpdateProcessStepDialog from "@/components/UpdateProcessStepDialog.vue"
import { useFeatureToggle } from "@/composables/useFeatureToggle"
import { useInternalUser } from "@/composables/useInternalUser"
import { useStatusBadge } from "@/composables/useStatusBadge"
import { DocumentationUnit } from "@/domain/documentationUnit"
import { isDecision } from "@/utils/typeGuards"
import IconError from "~icons/ic/baseline-error"
import IconPerson from "~icons/ic/baseline-person"
import IconApprovalDelegation from "~icons/material-symbols/approval-delegation-outline"

const props = defineProps<{
  documentUnit: DocumentationUnit
}>()

const route = useRoute()

const isInternalUser = useInternalUser()

const fileNumberInfo = computed(() => {
  return props.documentUnit.coreData.fileNumbers?.[0] || ""
})

const decisionDateInfo = computed(() => {
  return props.documentUnit.coreData.decisionDate
    ? dayjs(props.documentUnit.coreData.decisionDate).format("DD.MM.YYYY")
    : ""
})

const hasPendingDuplicateWarning = computed(() => {
  if (isDecision(props.documentUnit)) {
    return (props.documentUnit.managementData.duplicateRelations ?? []).some(
      (warning) => warning.status === "PENDING",
    )
  }
  return false
})

const courtInfo = computed(() => {
  return props.documentUnit.coreData.court?.label || ""
})

const formattedInfo = computed(() => {
  const parts = [
    courtInfo.value,
    fileNumberInfo.value,
    decisionDateInfo.value,
  ].filter((part) => part.trim() !== "")
  return parts.join(", ")
})

const statusBadge = ref(useStatusBadge(props.documentUnit.status).value)

const isRouteWithSaveButton = computed(
  () =>
    route.path.includes("categories") ||
    route.path.includes("attachments") ||
    route.path.includes("references") ||
    route.path.includes("managementdata"),
)

const hasErrorStatus = computed(() => props.documentUnit.status?.withError)
const managementDataRoute = computed(() => ({
  name: "caselaw-documentUnit-documentNumber-managementdata",
  params: { documentNumber: props.documentUnit.documentNumber },
}))

const processStepsEnabled = useFeatureToggle("neuris.process-steps")

const showProcessStepDialog = ref(false)
const toast = useToast()

async function onProcessStepUpdated() {
  toast.add({
    severity: "success",
    summary: "Weitergeben erfolgreich",
    life: 5_000,
  })
  showProcessStepDialog.value = false
}

watchEffect(() => {
  statusBadge.value = useStatusBadge(props.documentUnit.status).value
})
</script>

<template>
  <div
    class="sticky top-0 z-30 flex h-[64px] flex-row items-center border-b border-solid border-gray-400 bg-blue-100 px-24 py-12"
    data-testid="document-unit-info-panel"
  >
    <h1 class="ris-body1-bold">{{ props.documentUnit.documentNumber }}</h1>
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
      <CurrentAndLastProcessStepBadge
        v-if="processStepsEnabled"
        :process-steps="props.documentUnit.processSteps"
      />
      <IconBadge
        v-if="
          props.documentUnit.currentProcessStep &&
          props.documentUnit.currentProcessStep.user
        "
        background-color="bg-white"
        border-color="border-gray-800"
        :icon="IconPerson"
        :label="props.documentUnit.currentProcessStep.user.name"
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
    <UpdateProcessStepDialog
      v-if="processStepsEnabled"
      v-model:visible="showProcessStepDialog"
      @on-cancelled="showProcessStepDialog = false"
      @on-process-step-updated="onProcessStepUpdated"
    />
  </div>
</template>
