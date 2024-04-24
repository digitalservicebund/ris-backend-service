<script lang="ts" setup>
import { computed, onMounted, ref } from "vue"
import { ValidationError } from "./input/types"
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
  "update:validationError": [value?: ValidationError, field?: string]
}>()

const validationStore = useValidationStore<(typeof SingleNorm.fields)[number]>()
const singleNormInput = ref<InstanceType<typeof TextInput> | null>(null)

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
  emit("update:validationError", undefined, "singleNorm")

  //validate singleNorm
  if (singleNorm.value?.singleNorm) {
    const singleNormValidationInfo: SingleNormValidationInfo = {
      singleNorm: singleNorm.value?.singleNorm,
      normAbbreviation: props.normAbbreviation,
    }
    const response = await documentUnitService.validateSingleNorm(
      singleNormValidationInfo,
    )

    if (response.data !== "Ok") {
      validationStore.add("Inhalt nicht valide", "singleNorm")

      emit(
        "update:validationError",
        {
          message: "Inhalt nicht valide",
          instance: "singleNorm",
        },
        "singleNorm",
      )
    }
  }
}

async function removeSingleNormEntry() {
  emit("removeEntry")
}

function updateDateFormatValidation(
  validationError: ValidationError | undefined,
  field: string,
) {
  if (validationError) {
    emit(
      "update:validationError",
      {
        message: validationError.message,
        instance: validationError.instance,
      },
      field,
    )
  } else {
    emit("update:validationError", undefined, field)
  }
}

onMounted(async () => {
  if (props.modelValue.singleNorm) {
    await validateNorm()
  }

  singleNormInput.value?.focusInput()
})
</script>

<template>
  <div class="mb-24 flex justify-between gap-24">
    <InputField
      id="singleNorm"
      v-slot="slotProps"
      label="Einzelnorm"
      :validation-error="validationStore.getByField('singleNorm')"
    >
      <TextInput
        id="singleNorm"
        ref="singleNormInput"
        v-model="singleNorm.singleNorm"
        aria-label="Einzelnorm der Norm"
        :has-error="slotProps.hasError"
        size="medium"
        @blur="validateNorm"
        @input="validationStore.remove('singleNorm')"
      ></TextInput>
    </InputField>
    <InputField
      id="dateOfVersion"
      v-slot="slotProps"
      label="Fassungsdatum"
      :validation-error="validationStore.getByField('dateOfVersion')"
      @update:validation-error="
        (validationError) =>
          updateDateFormatValidation(validationError, 'dateOfVersion')
      "
    >
      <DateInput
        id="dateOfVersion"
        v-model="singleNorm.dateOfVersion"
        aria-label="Fassungsdatum der Norm"
        class="ds-input-medium"
        :has-error="slotProps.hasError"
        @update:validation-error="slotProps.updateValidationError"
      />
    </InputField>
    <InputField
      id="dateOfRelevance"
      v-slot="slotProps"
      label="Jahr"
      :validation-error="validationStore.getByField('dateOfRelevance')"
      @update:validation-error="
        (validationError) =>
          updateDateFormatValidation(validationError, 'dateOfRelevance')
      "
    >
      <YearInput
        id="dateOfRelevance"
        v-model="singleNorm.dateOfRelevance"
        aria-label="Jahr der Norm"
        :has-error="slotProps.hasError"
        size="medium"
        @update:validation-error="slotProps.updateValidationError"
      />
    </InputField>

    <button
      aria-label="Einzelnorm löschen"
      class="mt-[25px] h-[50px] text-blue-800 focus:shadow-[inset_0_0_0_0.25rem] focus:shadow-blue-800 focus:outline-none"
      tabindex="0"
      @click="removeSingleNormEntry"
    >
      <IconClear />
    </button>
  </div>
</template>
