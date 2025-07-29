<script setup lang="ts" generic="TDocument">
import dayjs from "dayjs"
import Button from "primevue/button"
import Dialog from "primevue/dialog"
import { computed, ref, toRaw, watchEffect } from "vue"
import { useRoute } from "vue-router"
import IconBadge from "@/components/IconBadge.vue"
import SaveButton from "@/components/SaveDocumentUnitButton.vue"
import Tooltip from "@/components/Tooltip.vue"
import { useFeatureToggle } from "@/composables/useFeatureToggle"
import { useInternalUser } from "@/composables/useInternalUser"
import { useProcessStepBadge } from "@/composables/useProcessStepBadge"
import { useStatusBadge } from "@/composables/useStatusBadge"
import { DocumentationUnit } from "@/domain/documentationUnit"
import DocumentationUnitProcessStep from "@/domain/documentationUnitProcessStep"
import ProcessStep from "@/domain/processStep"
import processStepService from "@/services/processStepService"
import { isDecision } from "@/utils/typeGuards"
import IconError from "~icons/ic/baseline-error"
import IconApprovalDelegation from "~icons/material-symbols/approval-delegation-outline"

const props = defineProps<{
  documentUnit: DocumentationUnit
  processSteps: DocumentationUnitProcessStep[]
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

watchEffect(() => {
  statusBadge.value = useStatusBadge(props.documentUnit.status).value
})

const processStepsEnabled = useFeatureToggle("neuris.process-steps")

const showMoveProcessStepModal = ref(false)

let nextProcessStep: ProcessStep | undefined

async function triggerMoveProcessStep(): Promise<void> {
  nextProcessStep = (
    await processStepService.getNextProcessStep(props.documentUnit.uuid)
  ).data
  showMoveProcessStepModal.value = true
}

async function moveProcessStep(): Promise<void> {
  if (nextProcessStep) {
    await processStepService.moveToNextProcessStep(
      props.documentUnit.uuid,
      nextProcessStep,
    )
    // TODO emit new process step?
    // if (newStep && newStep.data) props.processSteps.push(newStep.data)
  }
  showMoveProcessStepModal.value = false
}
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
    <IconBadge
      v-if="statusBadge"
      :background-color="statusBadge.backgroundColor"
      class="ml-12"
      :color="statusBadge.color"
      :icon="toRaw(statusBadge.icon)"
      :label="statusBadge.label"
    />
    <IconBadge
      v-if="hasErrorStatus"
      background-color="bg-red-300"
      class="ml-12"
      color="text-red-900"
      :icon="IconError"
      label="Fehler"
    />
    <template v-if="processStepsEnabled">
      <IconBadge
        v-for="(step, index) in props.processSteps"
        :key="step.id"
        :background-color="
          useProcessStepBadge(step.processStep).value.backgroundColor
        "
        :border-color="useProcessStepBadge(step.processStep).value.borderColor"
        :class="`border px-8 ${index == 0 ? 'ml-12' : 'ml-[-5px]'}`"
        :color="index == props.processSteps.length - 1 ? 'black' : 'gray-900'"
        :label="
          index == props.processSteps.length - 1
            ? step.processStep.name
            : step.processStep.abbreviation
        "
      />
    </template>

    <span class="flex-grow"></span>
    <div
      v-if="hasPendingDuplicateWarning"
      class="flex items-center gap-12 whitespace-nowrap"
    >
      <IconBadge
        background-color="bg-red-300"
        class="ml-12"
        color="text-red-900"
        data-testid="duplicate-icon"
        :icon="IconError"
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

    <Tooltip
      v-if="processStepsEnabled"
      text="Dokumentationseinheit weitergeben"
    >
      <Button
        aria-label="Dokumentationseinheit weitergeben"
        severity="secondary"
        size="small"
        @click="triggerMoveProcessStep"
      >
        <template #icon>
          <IconApprovalDelegation />
        </template>
      </Button>
    </Tooltip>

    <SaveButton
      v-if="isRouteWithSaveButton"
      aria-label="Speichern Button"
      data-testid="document-unit-save-button"
    />

    <Dialog
      class="max-h-[768px] max-w-[1024px]"
      :closable="false"
      header="Dokumentationseinheit weitergeben"
      modal
      :visible="showMoveProcessStepModal"
    >
      <div v-if="nextProcessStep" class="mb-12 flex items-center">
        Nächster Schritt:
        <IconBadge
          :background-color="
            useProcessStepBadge(nextProcessStep).value.backgroundColor
          "
          :border-color="useProcessStepBadge(nextProcessStep).value.borderColor"
          color="black"
          :label="nextProcessStep.name"
        />
      </div>

      <div class="modal-buttons-container flex w-full flex-row gap-[1rem]">
        <Button
          aria-label="Weitergeben"
          label="Weitergeben"
          severity="primary"
          size="small"
          @click="moveProcessStep"
        ></Button>
        <Button
          aria-label="Abbrechen"
          label="Abbrechen"
          severity="secondary"
          size="small"
          @click="showMoveProcessStepModal = false"
        ></Button>
      </div>
    </Dialog>
  </div>
</template>
