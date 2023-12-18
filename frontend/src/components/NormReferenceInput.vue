<script lang="ts" setup>
import { computed, onMounted, ref, watch } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import { NormAbbreviation } from "@/domain/normAbbreviation"
import NormReference, { SingleNormValidationInfo } from "@/domain/normReference"
import ComboboxItemService from "@/services/comboboxItemService"
import documentUnitService from "@/services/documentUnitService"
import DateInput from "@/shared/components/input/DateInput.vue"
import InputField from "@/shared/components/input/InputField.vue"
import TextButton from "@/shared/components/input/TextButton.vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import YearInput from "@/shared/components/input/YearInput.vue"

const props = defineProps<{ modelValue?: NormReference }>()
const emit = defineEmits<{
  "update:modelValue": [value: NormReference]
  addEntry: [void]
}>()

const validationStore =
  useValidationStore<(typeof NormReference.fields)[number]>()

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
      ...norm.value,
      normAbbreviation: newNormAbbreviation,
    })
    emit("update:modelValue", normRef)
  },
})

async function validateNorm() {
  validationStore.reset()

  //validate singleNorm
  if (norm.value?.singleNorm) {
    const singleNormValidationInfo: SingleNormValidationInfo = {
      singleNorm: norm.value.singleNorm,
      normAbbreviation: norm.value?.normAbbreviation?.abbreviation,
    }
    const response = await documentUnitService.validateSingleNorm(
      singleNormValidationInfo,
    )

    if (response.data !== "Ok")
      validationStore.add("Inhalt nicht valide", "singleNorm")
  }

  //validate required fields
  if (norm.value?.missingRequiredFields?.length) {
    norm.value?.missingRequiredFields.forEach((missingField) => {
      validationStore.add("Pflichtfeld nicht befüllt", missingField)
    })
  }
}

async function addNormReference() {
  await validateNorm()

  if (!validationStore.getByMessage("Inhalt nicht valide").length) {
    emit("update:modelValue", norm.value as NormReference)
    emit("addEntry")
  }
}

onMounted(async () => {
  // On first mount, we don't need to validate. When the props.modelValue do not
  // have the isEmpty getter, we can be sure that it has not been initialized as
  // NormReference and is therefore the inital load. As soons as we are using
  // uuid for norms, the check should be 'props.modelValue?.uuid !== undefined'

  if (props.modelValue?.isEmpty !== undefined) {
    await validateNorm()
  }
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
        placeholder="Nach Norm suchen"
        throttle-item-service-throughput
      ></ComboboxInput>
    </InputField>
    <InputField
      id="norm-reference-abbreviation-field"
      v-slot="slotProps"
      label="RIS-Abkürzung *"
      :validation-error="validationStore.getByField('normAbbreviation')"
    >
      <ComboboxInput
        id="norm-reference-abbreviation"
        v-model="normAbbreviation"
        aria-label="RIS-Abkürzung der Norm"
        clear-on-choosing-item
        :has-error="slotProps.hasError"
        :item-service="ComboboxItemService.getRisAbbreviations"
        @click="validationStore.remove('normAbbreviation')"
      >
      </ComboboxInput>
    </InputField>
    <div class="flex justify-between gap-24">
      <InputField
        id="norm-reference-singleNorm-field"
        v-slot="slotProps"
        label="Einzelnorm"
        :validation-error="validationStore.getByField('singleNorm')"
      >
        <TextInput
          id="norm-reference-singleNorm"
          v-model="norm.singleNorm"
          aria-label="Einzelnorm der Norm"
          :has-error="slotProps.hasError"
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
          v-model="norm.dateOfVersion"
          aria-label="Fassungsdatum der Norm"
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
          v-model="norm.dateOfRelevance"
          aria-label="Jahr der Norm"
          :has-error="slotProps.hasError"
          @update:validation-error="slotProps.updateValidationError"
        />
      </InputField>
    </div>
    <TextButton
      aria-label="Norm speichern"
      class="mr-28"
      :disabled="norm.isEmpty"
      label="Übernehmen"
      size="small"
      @click="addNormReference"
    />
  </div>
</template>
