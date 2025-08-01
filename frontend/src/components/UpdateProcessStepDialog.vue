<script setup lang="ts" generic="TDocument">
import { storeToRefs } from "pinia"
import Button from "primevue/button"
import Dialog from "primevue/dialog"
import Select from "primevue/select"
import { onMounted, Ref, ref } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import InputField from "@/components/input/InputField.vue"
import { ComboboxItem } from "@/components/input/types"
import { DocumentationUnit } from "@/domain/documentationUnit"
import ProcessStep from "@/domain/processStep"
import ComboboxItemService from "@/services/comboboxItemService"
import processStepService from "@/services/processStepService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import IconApprovalDelegation from "~icons/material-symbols/approval-delegation-outline"

const store = useDocumentUnitStore()
const { documentUnit } = storeToRefs(store) as {
  documentUnit: Ref<DocumentationUnit | undefined>
}
const showDialog = ref(false)
const selectedUser = ref<ComboboxItem>()
const processSteps = ref<ProcessStep[]>()
let nextProcessStep: ProcessStep | undefined

onMounted(async () => {
  processSteps.value = (await processStepService.getProcessSteps()).data
})

async function triggerUpdateProcessStep(): Promise<void> {
  if (documentUnit.value?.uuid)
    nextProcessStep = (
      await processStepService.getNextProcessStep(documentUnit.value?.uuid)
    ).data
  showDialog.value = true
}

async function updateProcessStep(): Promise<void> {
  if (nextProcessStep)
    documentUnit.value!.currentProcessStep = {
      processStep: nextProcessStep,
    }
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
    <div class="flex w-full flex-col pt-32">
      <div class="flex gap-32">
        <div class="flex-1">
          <InputField id="nextProcessStep" label="Neuer Schritt">
            <Select
              v-model="nextProcessStep"
              aria-label="Neuer Schritt"
              class="w-full"
              option-label="name"
              :options="processSteps"
            ></Select>
          </InputField>
        </div>
        <div class="flex-1">
          <InputField id="processStepPerson" label="Neue Person">
            <ComboboxInput
              id="processStepPerson"
              v-model="selectedUser"
              aria-label="Neue Person"
              :item-service="ComboboxItemService.getUsersForDocOffice"
            ></ComboboxInput>
          </InputField>
        </div>
      </div>

      <div
        class="modal-buttons-container flex w-full flex-row gap-[1rem] pt-32"
      >
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
    </div>
  </Dialog>
</template>
