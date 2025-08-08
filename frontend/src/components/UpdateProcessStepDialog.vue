<script setup lang="ts" generic="TDocument">
import dayjs from "dayjs"
import { storeToRefs } from "pinia"
import Button from "primevue/button"
import Column from "primevue/column"
import DataTable from "primevue/datatable"
import Dialog from "primevue/dialog"
import Select from "primevue/select"
import { computed, Ref, ref, watch } from "vue"
import { InfoStatus } from "./enumInfoStatus"
import ComboboxInput from "@/components/ComboboxInput.vue"
import IconBadge from "@/components/IconBadge.vue"
import InfoModal from "@/components/InfoModal.vue"
import InputField from "@/components/input/InputField.vue"
import { useProcessStepBadge } from "@/composables/useProcessStepBadge"
import { DocumentationUnit } from "@/domain/documentationUnit"
import DocumentationUnitProcessStep from "@/domain/documentationUnitProcessStep"
import ProcessStep from "@/domain/processStep"
import { User } from "@/domain/user"
import ComboboxItemService from "@/services/comboboxItemService"
import { ResponseError } from "@/services/httpClient"
import processStepService from "@/services/processStepService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const emit = defineEmits<{
  onProcessStepUpdated: []
  onCancelled: []
}>()

const visible = defineModel<boolean>("visible", {
  default: false,
  type: Boolean,
})

const store = useDocumentUnitStore()
const { documentUnit } = storeToRefs(store) as {
  documentUnit: Ref<DocumentationUnit>
}

const processSteps = ref<ProcessStep[]>()
const nextProcessStep = ref<ProcessStep>()
const errors = ref<ResponseError[]>([])
const nextProcessStepUser = ref<User>()

/**
 * Data restructuring from user props to combobox item.
 */
const selectedUser = computed({
  get: () =>
    nextProcessStepUser.value
      ? {
          label:
            nextProcessStepUser.value.name ||
            nextProcessStepUser.value.email ||
            "Keine Information",
          value: nextProcessStepUser.value,
        }
      : undefined,
  set: (newValue) => {
    nextProcessStepUser.value = { ...newValue } as User
  },
})

async function updateProcessStep(): Promise<void> {
  if (nextProcessStep.value) {
    documentUnit.value!.currentProcessStep = {
      processStep: nextProcessStep.value,
      user: nextProcessStepUser.value,
    }
    const response = await store.updateDocumentUnit()
    if (response.error) {
      errors.value?.push({
        title: "Die Dokumentationseinheit konnte nicht weitergegeben werden.",
        description: "Versuchen Sie es erneut.",
      })
    } else {
      emit("onProcessStepUpdated")
    }
  }
}

// The logic you want to run every time the dialog is shown
const fetchData = async () => {
  const processStepsResponse = await processStepService.getProcessSteps()
  if (processStepsResponse.error) {
    errors.value?.push(processStepsResponse.error)
  } else {
    processSteps.value = processStepsResponse.data
  }

  const nextProcessStepResponse = await processStepService.getNextProcessStep(
    documentUnit.value.uuid,
  )
  if (nextProcessStepResponse.error) {
    errors.value?.push(nextProcessStepResponse.error)
  } else {
    nextProcessStep.value = nextProcessStepResponse.data
  }
}

function rowClass(data: DocumentationUnitProcessStep): string {
  return data.id !== documentUnit.value.processSteps?.at(0)?.id
    ? "bg-gray-100"
    : ""
}

watch(
  visible,
  async (newValue) => {
    if (newValue === true) {
      errors.value = []
      await fetchData()
    }
  },
  { immediate: true },
)
</script>

<template>
  <Dialog
    v-model:visible="visible"
    class="max-h-[768px] max-w-[1024px]"
    :closable="false"
    dismissable-mask
    header="Dokumentationseinheit weitergeben"
    modal
  >
    <div class="flex w-full flex-col pt-32">
      <div v-if="errors.length > 0" class="mb-48 flex flex-col">
        <InfoModal
          v-for="(error, index) in errors"
          :key="index"
          :class="index !== 0 ? 'mt-16' : ''"
          data-testid="service-error"
          :description="error.description"
          :status="InfoStatus.ERROR"
          :title="error.title"
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
          :disabled="!nextProcessStep"
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

      <DataTable
        class="overflow-x-scroll pt-32"
        :row-class="rowClass"
        :value="documentUnit.processSteps"
      >
        <Column field="createdAt" header="Datum">
          <template #body="{ data: item }">
            {{ dayjs(item.decisionDate).format("DD.MM.YYYY") }}
          </template>
        </Column>
        <Column field="processStep.name" header="Schritt">
          <template #body="{ data: item }">
            <IconBadge v-bind="useProcessStepBadge(item.processStep).value" />
          </template>
        </Column>

        <Column field="user.name" header="Person" />
        <template #empty>Keine Prozessschritte</template>
      </DataTable>
    </div>
  </Dialog>
</template>
