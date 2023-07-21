<script lang="ts" setup>
import { computed, onMounted, ref, watch } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import { NormAbbreviation } from "@/domain/normAbbreviation"
import NormReference, { SingleNormValidationInfo } from "@/domain/normReference"
import ComboboxItemService from "@/services/comboboxItemService"
import documentUnitService from "@/services/documentUnitService"
import DateInput from "@/shared/components/input/DateInput.vue"
import InputField from "@/shared/components/input/InputField.vue"
import TextButton from "@/shared/components/input/TextButton.vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import { ValidationError } from "@/shared/components/input/types"
import YearInput from "@/shared/components/input/YearInput.vue"

const props = defineProps<{ modelValue?: NormReference }>()
const emit = defineEmits<{
  "update:modelValue": [value: NormReference]
  closeEntry: [void]
}>()

const hasValidationError = ref()

const validationErrors = ref<ValidationError[]>()

const norm = ref(new NormReference({ ...props.modelValue }))

const normAbbreviation = computed({
  get: () =>
    props.modelValue?.normAbbreviation
      ? {
          label: props.modelValue.normAbbreviation.abbreviation,
          value: norm.value.normAbbreviation,
          additionalInformation:
            props.modelValue.normAbbreviation.officialLongTitle,
        }
      : undefined,
  set: (newValue) => {
    const newNormAbbreviation = { ...newValue } as NormAbbreviation
    const normRef = new NormReference({
      ...norm,
      normAbbreviation: newNormAbbreviation,
    })
    emit("update:modelValue", normRef)
  },
})

async function validateNorm() {
  validationErrors.value = []

  //validate singleNorm
  if (norm.value?.singleNorm) {
    const singleNormValidationInfo: SingleNormValidationInfo = {
      singleNorm: norm.value.singleNorm,
      normAbbreviation: norm.value?.normAbbreviation?.abbreviation,
    }
    const response = await documentUnitService.validateSingleNorm(
      singleNormValidationInfo,
    )

    if (response.data !== "Ok") {
      validationErrors.value?.push({
        defaultMessage: "Inhalt nicht valide",
        field: "singleNorm",
      })
      hasValidationError.value = true
    } else {
      hasValidationError.value = false
    }
  } else {
    hasValidationError.value = false
  }

  //validate required fields
  if (norm.value?.missingRequiredFields?.length) {
    norm.value?.missingRequiredFields.forEach((missingField) => {
      validationErrors.value?.push({
        defaultMessage: "Pflichtfeld nicht befüllt",
        field: missingField,
      })
    })
  }
}

async function addNormReference() {
  const validation = validateNorm()
  validation.then(() => {
    if (!hasValidationError.value) {
      emit("update:modelValue", norm.value as NormReference)
      emit("closeEntry")
    }
  })
}

function resetValidationError(field: string) {
  validationErrors.value = validationErrors.value?.filter(
    (error) => error.field !== field,
  )
}

onMounted(() => {
  validateNorm()
  norm.value = new NormReference({ ...props.modelValue })
})

watch(props, () => {
  if (
    props.modelValue?.normAbbreviation?.abbreviation !==
    norm.value?.normAbbreviation?.abbreviation
  )
    norm.value = new NormReference({ ...props.modelValue })
})
</script>

<template>
  <div>
    <InputField id="norm-reference-search-field" label="Suchfeld">
      <ComboboxInput
        id="norm-reference-search"
        v-model="normAbbreviation"
        aria-label="Norm Suchfeld"
        clear-on-choosing-item
        :item-service="ComboboxItemService.getRisAbbreviationsAwesome"
        placeholder="Suchfeld"
        throttle-item-service-throughput
      ></ComboboxInput>
    </InputField>
    <InputField
      id="norm-reference-abbreviation-field"
      v-slot="slotProps"
      label="RIS-Abkürzung *"
      :validation-error="
        validationErrors?.find((err) => err.field === 'normAbbreviation')
      "
    >
      <ComboboxInput
        id="norm-reference-abbreviation"
        v-model="normAbbreviation"
        aria-label="RIS-Abkürzung der Norm"
        clear-on-choosing-item
        :has-error="slotProps.hasError"
        :item-service="ComboboxItemService.getRisAbbreviations"
        placeholder="RIS Abkürzung"
        @click="resetValidationError('normAbbreviation')"
      >
      </ComboboxInput>
    </InputField>
    <div class="flex justify-between gap-24">
      <InputField
        id="norm-reference-singleNorm-field"
        v-slot="slotProps"
        label="Einzelnorm"
        :validation-error="
          validationErrors?.find((err) => err.field === 'singleNorm')
        "
      >
        <TextInput
          id="norm-reference-singleNorm"
          v-model="norm.singleNorm"
          aria-label="Einzelnorm der Norm"
          :has-error="slotProps.hasError"
          @input="resetValidationError('singleNorm')"
        ></TextInput>
      </InputField>
      <InputField
        id="norm-date-of-version"
        v-slot="slotProps"
        label="Fassungsdatum"
        :validation-error="
          validationErrors?.find((err) => err.field === 'dateOfVersion')
        "
      >
        <DateInput
          id="norm-date-of-version"
          v-model="norm.dateOfVersion"
          aria-label="Fassungsdatum der Norm"
          :has-error="slotProps.hasError"
          @update:validation-error="slotProps.updateValidationError"
        />
      </InputField>
      <InputField id="norm-date-of-relevence" label="Jahr">
        <YearInput
          id="norm-date-of-relevence"
          v-model="norm.dateOfRelevance"
          aria-label="Jahr der Norm"
        />
      </InputField>
    </div>
    <TextButton
      aria-label="Norm speichern"
      class="mr-28"
      :disabled="norm.isEmpty"
      label="Übernehmen"
      @click="addNormReference"
    />
  </div>
</template>
