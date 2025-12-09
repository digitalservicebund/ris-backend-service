<script lang="ts" setup>
import InputSelect from "primevue/select"
import { computed } from "vue"
import InputField from "@/components/input/InputField.vue"
import { coreDataLabels } from "@/domain/coreData"
import { CourtBranchLocation } from "@/domain/courtBranchLocation"

const props = defineProps<{
  courtBranchLocations?: CourtBranchLocation[]
  modelValue?: CourtBranchLocation
}>()

const emit = defineEmits<{
  "update:modelValue": [value?: CourtBranchLocation]
}>()

const hasOptions = computed(
  () => options.value !== undefined && options.value.length > 0,
)

const options = computed(() => {
  if (hasInvalidOptionSelected.value) {
    return props.courtBranchLocations
      ? [...props.courtBranchLocations, props.modelValue]
      : [props.modelValue]
  } else return props.courtBranchLocations
})

const hasInvalidOptionSelected = computed(
  () =>
    props.modelValue &&
    (!props.courtBranchLocations ||
      !props.courtBranchLocations.some(
        (option) => option.id === props.modelValue?.id,
      )),
)

const branchLocation = computed({
  get: () => {
    return props.modelValue
  },

  set: (newValue: CourtBranchLocation) => {
    emit("update:modelValue", newValue)
  },
})
</script>

<template>
  <InputField
    id="branchLocation"
    v-slot="{ id }"
    :label="coreDataLabels.courtBranchLocation"
    :validation-error="
      hasInvalidOptionSelected
        ? {
            message: 'Gehört nicht zum ausgewählten Gericht',
            instance: 'branchLocation',
          }
        : undefined
    "
  >
    <InputSelect
      :id="id"
      v-model="branchLocation"
      :aria-label="coreDataLabels.courtBranchLocation"
      :disabled="!hasOptions"
      fluid
      option-label="value"
      :options="options"
      placeholder="Bitte auswählen"
      :show-clear="branchLocation !== undefined"
    />
  </InputField>
</template>
