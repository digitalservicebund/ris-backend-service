<script setup lang="ts" generic="TDocument">
import dayjs from "dayjs"
import { storeToRefs } from "pinia"
import Button from "primevue/button"
import Column from "primevue/column"
import DataTable from "primevue/datatable"
import Dialog from "primevue/dialog"
import Select from "primevue/select"
import { Ref, ref, watch } from "vue"
import { InfoStatus } from "./enumInfoStatus"
import ComboboxInput from "@/components/ComboboxInput.vue"
import IconBadge from "@/components/IconBadge.vue"
import InfoModal from "@/components/InfoModal.vue"
import InputField from "@/components/input/InputField.vue"
import { ComboboxItem } from "@/components/input/types"
import { useProcessStepBadge } from "@/composables/useProcessStepBadge"
import { DocumentationUnit } from "@/domain/documentationUnit"
import ProcessStep from "@/domain/processStep"
import ComboboxItemService from "@/services/comboboxItemService"
import { ResponseError } from "@/services/httpClient"
import processStepService from "@/services/processStepService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const props = defineProps<{
  showDialog: boolean
}>()

const emit = defineEmits<{
  onProcessStepUpdated: []
  onCancelled: []
}>()

const store = useDocumentUnitStore()
const { documentUnit } = storeToRefs(store) as {
  documentUnit: Ref<DocumentationUnit>
}
const selectedUser = ref<ComboboxItem>()
const processSteps = ref<ProcessStep[]>()
const nextProcessStep = ref<ProcessStep>()
const serviceError1 = ref<ResponseError>()
const serviceError2 = ref<ResponseError>()

async function updateProcessStep(): Promise<void> {
  if (nextProcessStep.value)
    documentUnit.value!.currentProcessStep = {
      processStep: nextProcessStep.value,
    }
  const response = await store.updateDocumentUnit()
  if (response.error) {
    serviceError1.value = {
      title: "Die Dokumentationseinheit konnte nicht weitergegeben werden.",
      description: "Versuchen Sie es erneut.",
    }
  } else {
    emit("onProcessStepUpdated")
  }
}

// The logic you want to run every time the dialog is shown
const fetchData = async () => {
  const processStepsResponse = await processStepService.getProcessSteps()
  if (processStepsResponse.error) {
    serviceError1.value = processStepsResponse.error
  } else {
    processSteps.value = processStepsResponse.data
  }

  const nextProcessStepResponse = await processStepService.getNextProcessStep(
    documentUnit.value.uuid,
  )
  if (nextProcessStepResponse.error) {
    serviceError2.value = nextProcessStepResponse.error
  } else {
    nextProcessStep.value = nextProcessStepResponse.data
  }
}

watch(
  () => props.showDialog,
  async (newValue) => {
    if (newValue) {
      await fetchData()
    }
  },
  { immediate: true },
)
</script>

<template>
  <Dialog
    class="max-h-[768px] max-w-[1024px]"
    :closable="false"
    header="Dokumentationseinheit weitergeben"
    modal
    :visible="props.showDialog"
  >
    <div class="flex w-full flex-col pt-32">
      <div v-if="serviceError1 || serviceError2" class="mb-48 flex flex-col">
        <InfoModal
          v-if="serviceError1"
          data-testid="service-error"
          :description="serviceError1.description"
          :status="InfoStatus.ERROR"
          :title="serviceError1.title"
        />
        <InfoModal
          v-if="serviceError2"
          :class="serviceError1 ? 'mt-16' : ''"
          data-testid="service-error"
          :description="serviceError2.description"
          :status="InfoStatus.ERROR"
          :title="serviceError2.title"
        />
      </div>

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
          @click="$emit('onCancelled')"
        ></Button>
      </div>

      <DataTable class="pt-32" :value="documentUnit.processSteps">
        <Column field="createdAt" header="Datum">
          <template #body="{ data: item }">
            {{ dayjs(item.decisionDate).format("DD.MM.YYYY") }}
          </template>
        </Column>
        <Column field="processStep.name" header="Schritt">
          <template #body="{ data: item }">
            <IconBadge
              class="px-8"
              v-bind="useProcessStepBadge(item.processStep).value"
              :label="item.processStep.name"
            />
          </template>
        </Column>

        <Column field="user.name" header="Person" />
        <template #empty>Keine Prozessschritte</template>
      </DataTable>
    </div>
  </Dialog>
</template>
