<script setup lang="ts" generic="TDocument">
import dayjs from "dayjs"
import Button from "primevue/button"
import Column from "primevue/column"
import DataTable from "primevue/datatable"
import Dialog from "primevue/dialog"
import Select from "primevue/select"
import { computed, ref, watch } from "vue"
import { InfoStatus } from "./enumInfoStatus"
import AssigneeBadge from "@/components/AssigneeBadge.vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import IconBadge from "@/components/IconBadge.vue"
import InfoModal from "@/components/InfoModal.vue"
import InputField from "@/components/input/InputField.vue"
import InputErrorMessages from "@/components/InputErrorMessages.vue"
import { useProcessStepBadge } from "@/composables/useProcessStepBadge"
import { DocumentationUnit } from "@/domain/documentationUnit"
import DocumentationUnitProcessStep from "@/domain/documentationUnitProcessStep"
import ProcessStep from "@/domain/processStep"
import { User } from "@/domain/user"
import ComboboxItemService from "@/services/comboboxItemService"
import { ResponseError } from "@/services/httpClient"
import processStepService from "@/services/processStepService"

const props = defineProps<{
  documentationUnit?: DocumentationUnit
  handleAssignProcessStep: (
    documentationUnitProcessStep: DocumentationUnitProcessStep,
  ) => Promise<ResponseError | undefined>
}>()

const visible = defineModel<boolean>("visible", {
  default: false,
  type: Boolean,
})

const processSteps = ref<ProcessStep[]>()
const nextProcessStep = ref<ProcessStep>()
const nextUser = ref<User>()
// erros from fetching the process steps
const fetchProcessStepsErrors = ref<ResponseError[]>([])
// input error
const hasNoProcessStepSelectedError = ref(false)

/**
 * Data restructuring from user props to combobox item.
 */
const selectedUser = computed({
  get: () =>
    nextUser.value
      ? {
          label: nextUser.value.initials || nextUser.value.email || "",
          value: nextUser.value,
        }
      : undefined,
  set: (newValue) => {
    nextUser.value = { ...newValue } as User
  },
})

async function assignProcessStep(): Promise<void> {
  if (!nextProcessStep.value) {
    hasNoProcessStepSelectedError.value = true
  } else {
    hasNoProcessStepSelectedError.value = false

    const error = await props.handleAssignProcessStep({
      processStep: nextProcessStep.value,
      user: nextUser.value,
    })

    if (error) {
      fetchProcessStepsErrors.value?.push({
        title: error.title,
        description: error.description,
      })
    } else {
      nextUser.value = undefined
    }
  }
}

// The logic you want to run every time the dialog is shown
const fetchData = async () => {
  const processStepsResponse = await processStepService.getProcessSteps()
  if (processStepsResponse.error) {
    fetchProcessStepsErrors.value?.push(processStepsResponse.error)
  } else {
    processSteps.value = processStepsResponse.data
  }

  // documentationUnit not given when multi edit, and therefor no next processstep
  if (props.documentationUnit) {
    const nextProcessStepResponse = await processStepService.getNextProcessStep(
      props.documentationUnit.uuid,
    )
    if (nextProcessStepResponse.error) {
      fetchProcessStepsErrors.value?.push(nextProcessStepResponse.error)
    } else {
      nextProcessStep.value = nextProcessStepResponse.data
    }
  }
}

function rowClass(data: DocumentationUnitProcessStep): string {
  return props.documentationUnit &&
    data.id !== props.documentationUnit.processSteps?.at(0)?.id
    ? "bg-gray-100"
    : ""
}

watch(
  visible,
  async (newValue) => {
    console.log(newValue)
    if (newValue === true) {
      fetchProcessStepsErrors.value = []
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
      <div
        v-if="fetchProcessStepsErrors.length > 0"
        class="mb-48 flex flex-col"
      >
        <InfoModal
          v-for="(error, index) in fetchProcessStepsErrors"
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
              :has-error="hasNoProcessStepSelectedError"
              option-label="name"
              :options="processSteps"
              @focus="() => (hasNoProcessStepSelectedError = false)"
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

      <InputErrorMessages
        v-if="hasNoProcessStepSelectedError"
        class="self-start"
        error-message="Wählen Sie einen Vorgang aus"
      />

      <div
        class="modal-buttons-container flex w-full flex-row gap-[1rem] pt-32"
      >
        <Button
          aria-label="Weitergeben"
          label="Weitergeben"
          severity="primary"
          size="small"
          @click="assignProcessStep"
        ></Button>
        <Button
          aria-label="Abbrechen"
          label="Abbrechen"
          severity="secondary"
          size="small"
          @click="visible = false"
        ></Button>
      </div>

      <!-- history not visible for multi edit -->
      <DataTable
        v-if="props.documentationUnit"
        class="overflow-x-scroll pt-32"
        :row-class="rowClass"
        :value="props.documentationUnit.processSteps"
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

        <Column field="user.name" header="Person">
          <template #body="{ data: item }">
            <AssigneeBadge :name="item?.user?.initials" />
          </template>
        </Column>
        <template #empty>Keine Prozessschritte</template>
      </DataTable>
    </div>
  </Dialog>
</template>
