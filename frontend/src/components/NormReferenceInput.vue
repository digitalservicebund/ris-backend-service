<script lang="ts" setup>
import { computed, onMounted, ref } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import { NormAbbreviation } from "@/domain/normAbbreviation"
import NormReference from "@/domain/normReference"
import ComboboxItemService from "@/services/comboboxItemService"
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
const validationErrors = ref<ValidationError[]>()

const norm = computed({
  get() {
    return (props.modelValue as NormReference) ?? {}
  },
  set(value) {
    emit("update:modelValue", value)
  },
})

const normAbbreviation = computed({
  get: () =>
    norm?.value?.normAbbreviation
      ? {
          label: norm.value.normAbbreviation.abbreviation,
          value: norm.value.normAbbreviation,
          additionalInformation: norm.value.normAbbreviation.officialLongTitle,
        }
      : undefined,
  set: (newValue) => {
    const newNormAbbreviation = { ...newValue } as NormAbbreviation
    const normRef = new NormReference({
      ...norm.value,
      normAbbreviation: newNormAbbreviation,
    })
    emit("update:modelValue", normRef)
  },
})

async function validateNorm() {
  validationErrors.value = []

  if (norm.value?.updateValidationErrors) {
    if (await norm.value.updateValidationErrors()) {
      validationErrors.value?.push({
        defaultMessage: "Inhalt nicht valide",
        field: "singleNorm",
      })
    }

    //validate required fields
    if (norm.value.missingRequiredFields?.length) {
      norm.value.missingRequiredFields.forEach((missingField) => {
        validationErrors.value?.push({
          defaultMessage: "Pflichtfeld nicht befüllt",
          field: missingField,
        })
      })
    }
  }
}

async function addNormReference() {
  validateNorm()
  emit("update:modelValue", norm.value)
  emit("closeEntry")
}

onMounted(() => {
  validateNorm()
  norm.value = (props.modelValue as NormReference) ?? {}
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
      ></ComboboxInput>
    </InputField>
    <InputField
      id="norm-reference-abbreviation-field"
      v-slot="slotProps"
      label="RIS-Abkürzung *"
      :validation-error="
        validationErrors?.find((err) => err.field === 'normAbbreviation')
          ?.defaultMessage
      "
    >
      <ComboboxInput
        id="norm-reference-abbreviation"
        v-model="normAbbreviation"
        aria-label="Norm RIS-Abkürzung"
        clear-on-choosing-item
        :has-error="slotProps.hasError"
        :item-service="ComboboxItemService.getRisAbbreviations"
        placeholder="RIS Abkürzung"
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
            ?.defaultMessage
        "
      >
        <TextInput
          id="norm-reference-singleNorm"
          v-model="norm.singleNorm"
          aria-label="Norm Einzelnorm"
          :has-error="slotProps.hasError"
        ></TextInput>
      </InputField>
      <InputField id="norm-date-of-version" label="Fassungsdatum">
        <DateInput
          id="norm-date-of-version"
          v-model="norm.dateOfVersion"
          aria-label="Norm Fassungsdatum"
        />
      </InputField>
      <InputField id="norm-date-of-relevence" label="Jahr">
        <YearInput
          id="norm-date-of-relevence"
          v-model="norm.dateOfRelevance"
          aria-label="Norm Jahr"
        />
      </InputField>
    </div>
    <TextButton
      aria-label="Norm speichern"
      class="mr-28"
      label="Übernehmen"
      @click="addNormReference"
    />
  </div>
</template>
