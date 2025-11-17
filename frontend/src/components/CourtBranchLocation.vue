<script lang="ts" setup>
import InputSelect from "primevue/select"
import { computed, onMounted, ref, watch } from "vue"
import InputField from "@/components/input/InputField.vue"
import { coreDataLabels } from "@/domain/coreData"
import { Court } from "@/domain/court"
import courtService from "@/services/courtService"

const props = defineProps<{
  court?: Court
  modelValue?: string
}>()

const emit = defineEmits<{
  "update:modelValue": [value?: string]
}>()

const options = ref<string[] | undefined>()

const hasOptions = computed(
  () => options.value !== undefined && options.value.length > 0,
)

const branchLocation = computed({
  get: () => {
    return props.modelValue
  },

  set: (newValue: string) => {
    emit("update:modelValue", newValue)
  },
})

async function loadOptions() {
  if (props.court?.type) {
    const branchLocationsResponse = await courtService.getBranchLocations(
      props.court.type,
      props.court.location,
    )
    if (branchLocationsResponse.data) {
      options.value = branchLocationsResponse.data
    }
  } else {
    options.value = undefined
  }
}

watch(
  () => props.court,
  async () => {
    await loadOptions()
    if (!props.court) {
      console.log("clear sitz der aussenstelle field: ", props.court, " -")
      emit("update:modelValue", undefined)
    }
  },
)

onMounted(async () => {
  await loadOptions()
})
</script>

<template>
  <InputField
    id="branchLocation"
    v-slot="{ id }"
    :label="coreDataLabels.courtBranchLocation"
  >
    <InputSelect
      :id="id"
      v-model="branchLocation"
      :aria-label="coreDataLabels.courtBranchLocation"
      :disabled="!hasOptions"
      fluid
      :options="options"
      placeholder="Bitte auswählen"
      :show-clear="branchLocation !== undefined"
    />
  </InputField>
</template>
