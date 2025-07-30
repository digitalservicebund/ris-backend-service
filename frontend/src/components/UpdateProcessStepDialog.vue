<script setup lang="ts" generic="TDocument">
import Button from "primevue/button"
import Dialog from "primevue/dialog"
import { ref } from "vue"
import IconBadge from "@/components/IconBadge.vue"
import { useProcessStepBadge } from "@/composables/useProcessStepBadge"
import DocumentationUnitProcessStep from "@/domain/documentationUnitProcessStep"
import ProcessStep from "@/domain/processStep"
import processStepService from "@/services/processStepService"
import IconApprovalDelegation from "~icons/material-symbols/approval-delegation-outline"

const props = defineProps<{
  docUnitId: string
  processSteps: DocumentationUnitProcessStep[]
}>()

const showDialog = ref(false)

let nextProcessStep: ProcessStep | undefined

async function triggerUpdateProcessStep(): Promise<void> {
  nextProcessStep = (
    await processStepService.getNextProcessStep(props.docUnitId)
  ).data
  showDialog.value = true
}

async function updateProcessStep(): Promise<void> {
  if (nextProcessStep) {
    await processStepService.moveToNextProcessStep(
      props.docUnitId,
      nextProcessStep,
    )
    // TODO emit new process step?
    // if (newStep && newStep.data) props.processSteps.push(newStep.data)
  }
  showDialog.value = false
}
</script>

<template>
  <Button
    v-tooltip.bottom="'Dokumentationseinheit weitergeben'"
    aria-label="Dokumentationseinheit weitergeben"
    severity="secondary"
    size="small"
    @click="triggerUpdateProcessStep"
  >
    <template #icon>
      <IconApprovalDelegation />
    </template>
  </Button>

  <Dialog
    class="max-h-[768px] max-w-[1024px]"
    :closable="false"
    header="Dokumentationseinheit weitergeben"
    modal
    :visible="showDialog"
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
        @click="updateProcessStep"
      ></Button>
      <Button
        aria-label="Abbrechen"
        label="Abbrechen"
        severity="secondary"
        size="small"
        @click="showDialog = false"
      ></Button>
    </div>
  </Dialog>
</template>
