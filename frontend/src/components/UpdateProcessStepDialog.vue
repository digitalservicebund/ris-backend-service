<script setup lang="ts" generic="TDocument">
import { storeToRefs } from "pinia"
import Button from "primevue/button"
import Dialog from "primevue/dialog"
import { onMounted, Ref, ref } from "vue"
import IconBadge from "@/components/IconBadge.vue"
import { useProcessStepBadge } from "@/composables/useProcessStepBadge"
import { DocumentationUnit } from "@/domain/documentationUnit"
import ProcessStep from "@/domain/processStep"
import processStepService from "@/services/processStepService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import IconApprovalDelegation from "~icons/material-symbols/approval-delegation-outline"

const store = useDocumentUnitStore()
const { documentUnit } = storeToRefs(store) as {
  documentUnit: Ref<DocumentationUnit | undefined>
}
const showDialog = ref(false)

let nextProcessStep: ProcessStep | undefined

async function triggerUpdateProcessStep(): Promise<void> {
  if (documentUnit.value?.uuid)
    nextProcessStep = (
      await processStepService.getNextProcessStep(documentUnit.value?.uuid)
    ).data
  console.log(nextProcessStep)
  showDialog.value = true
}

async function updateProcessStep(): Promise<void> {
  if (nextProcessStep)
    documentUnit.value!.currentProcessStep = { processStep: nextProcessStep }
  await store.updateDocumentUnit()
  showDialog.value = false
}

onMounted(async () => {
  await store.loadDocumentUnit
})
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
