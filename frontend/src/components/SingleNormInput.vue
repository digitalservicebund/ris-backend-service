<script lang="ts" setup>
import { computed, onMounted } from "vue"
import DateInput from "@/components/input/DateInput.vue"
import InputField from "@/components/input/InputField.vue"
import TextInput from "@/components/input/TextInput.vue"
import YearInput from "@/components/input/YearInput.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import SingleNorm, { SingleNormValidationInfo } from "@/domain/singleNorm"
import documentUnitService from "@/services/documentUnitService"
import IconClear from "~icons/material-symbols/close-small"

const props = defineProps<{
  modelValue: SingleNorm
  normAbbreviation: string
}>()

const emit = defineEmits<{
  "update:modelValue": [value: SingleNorm]
  removeEntry: [void]
}>()

const validationStore = useValidationStore<(typeof SingleNorm.fields)[number]>()

const singleNorm = computed({
  get: () => {
    return props.modelValue
  },
  set: (value) => {
    if (value) emit("update:modelValue", value)
  },
})

async function validateNorm() {
  validationStore.reset()

  //validate singleNorm
  if (singleNorm.value?.singleNorm) {
    const singleNormValidationInfo: SingleNormValidationInfo = {
      singleNorm: singleNorm.value?.singleNorm,
      normAbbreviation: props.normAbbreviation,
    }
    const response = await documentUnitService.validateSingleNorm(
      singleNormValidationInfo,
    )

    if (response.data !== "Ok")
      validationStore.add("Inhalt nicht valide", "singleNorm")
  }

  //validate required fields
  if (singleNorm.value?.missingRequiredFields?.length) {
    singleNorm.value?.missingRequiredFields.forEach((missingField) => {
      validationStore.add("Pflichtfeld nicht befüllt", missingField)
    })
  }
}

async function removeSingleNormEntry() {
  emit("removeEntry")
}

onMounted(async () => {
  if (props.modelValue.singleNorm) {
    await validateNorm()
  }
})
</script>

<template>
  <div
    class="mb-24 flex justify-between gap-24 border-b-1 border-blue-300 pb-24"
  >
    <InputField
      id="norm-reference-singleNorm-field"
      v-slot="slotProps"
      label="Einzelnorm"
      :validation-error="validationStore.getByField('singleNorm')"
    >
      <TextInput
        id="norm-reference-singleNorm"
        v-model="singleNorm.singleNorm"
        aria-label="Einzelnorm der Norm"
        :has-error="slotProps.hasError"
        size="medium"
        @blur="validateNorm"
        @input="validationStore.remove('singleNorm')"
      ></TextInput>
    </InputField>
    <InputField
      id="norm-date-of-version"
      v-slot="slotProps"
      label="Fassungsdatum"
      :validation-error="validationStore.getByField('dateOfVersion')"
    >
      <DateInput
        id="norm-date-of-version"
        v-model="singleNorm.dateOfVersion"
        aria-label="Fassungsdatum der Norm"
        class="ds-input-medium"
        :has-error="slotProps.hasError"
        @update:validation-error="slotProps.updateValidationError"
      />
    </InputField>
    <InputField
      id="norm-date-of-relevance"
      v-slot="slotProps"
      label="Jahr"
      :validation-error="validationStore.getByField('dateOfRelevance')"
    >
      <YearInput
        id="norm-date-of-relevance"
        v-model="singleNorm.dateOfRelevance"
        aria-label="Jahr der Norm"
        :has-error="slotProps.hasError"
        size="medium"
        @update:validation-error="slotProps.updateValidationError"
      />
    </InputField>
    <button
      aria-label="Einzelnorm löschen"
      class="input-close-icon flex items-center text-blue-800 focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
      tabindex="0"
      @click="removeSingleNormEntry"
    >
      <IconClear />
    </button>
  </div>
</template>
