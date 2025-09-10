<script setup lang="ts" generic="TDocument">
import Button from "primevue/button"
import Dialog from "primevue/dialog"
import Select from "primevue/select"
import { computed, ref, watch } from "vue"
import { InfoStatus } from "./enumInfoStatus"
import ComboboxInput from "@/components/ComboboxInput.vue"
import InfoModal from "@/components/InfoModal.vue"
import InputField from "@/components/input/InputField.vue"
import { DocumentationUnit } from "@/domain/documentationUnit"
import ProcessStep from "@/domain/processStep"
import { User } from "@/domain/user"
import ComboboxItemService from "@/services/comboboxItemService"
import { ResponseError } from "@/services/httpClient"
import processStepService from "@/services/processStepService"

const emit = defineEmits<{
  onProcessStepUpdated: []
  onCancelled: []
}>()

const visible = defineModel<boolean>("visible", {
  default: false,
  type: Boolean,
})

const processSteps = ref<ProcessStep[]>()
const errors = ref<ResponseError[]>([])
const nextProcessStep = ref<ProcessStep | undefined>()
const nextProcessStepUser = ref<User>()

/**
 * Data restructuring from user props to combobox item.
 */
const selectedUser = computed({
  get: () =>
    nextProcessStepUser.value
      ? {
          label:
            nextProcessStepUser.value.initials ||
            nextProcessStepUser.value.email ||
            "",
          value: nextProcessStepUser.value,
        }
      : undefined,
  set: (newValue) => {
    nextProcessStepUser.value = { ...newValue } as User
  },
})

async function updateProcessStep(): Promise<void> {
  if (nextProcessStep.value) {
    emit("onProcessStepUpdated")
    nextProcessStepUser.value = undefined
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
    header="Dokumentationseinheit(en) weitergeben"
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
              class="z-10"
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
    </div>
  </Dialog>
</template>
