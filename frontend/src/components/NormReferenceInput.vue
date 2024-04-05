<script lang="ts" setup>
import { computed, onMounted, onBeforeUnmount, ref, watch } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import DateInput from "@/components/input/DateInput.vue"
import InputField from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"
import TextInput from "@/components/input/TextInput.vue"
import YearInput from "@/components/input/YearInput.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import { NormAbbreviation } from "@/domain/normAbbreviation"
import NormReference, { SingleNormValidationInfo } from "@/domain/normReference"
import ComboboxItemService from "@/services/comboboxItemService"
import documentUnitService from "@/services/documentUnitService"
import IconAdd from "~icons/ic/baseline-add"

const props = defineProps<{ modelValue?: NormReference }>()
const emit = defineEmits<{
  "update:modelValue": [value: NormReference]
  addEntry: [void]
  cancelEdit: [void]
  removeListEntry: [void]
}>()

const validationStore =
  useValidationStore<(typeof NormReference.fields)[number]>()

const norm = ref(new NormReference({ ...props.modelValue }))
const lastSavedModelValue = ref(new NormReference({ ...props.modelValue }))

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

watch(
  () => props.modelValue,
  () => {
    norm.value = new NormReference({ ...props.modelValue })
    lastSavedModelValue.value = new NormReference({ ...props.modelValue })
    if (lastSavedModelValue.value.isEmpty) validationStore.reset()
  },
)

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

onBeforeUnmount(() => {
  if (norm.value.isEmpty) emit("removeListEntry")
})
</script>

<template>
  <div class="flex flex-col gap-24">
    <InputField
      id="norm-reference-abbreviation-field"
      v-slot="slotProps"
      label="RIS-Abkürzung *"
      :validation-error="validationStore.getByField('normAbbreviation')"
    >
      <ComboboxInput
        id="norm-reference-abbreviation"
        v-model="normAbbreviation"
        aria-label="RIS-Abkürzung"
        :has-error="slotProps.hasError"
        :item-service="ComboboxItemService.getRisAbbreviations"
        placeholder="Abkürzung, Kurz-oder Langtitel oder Region eingeben ..."
        @click="validationStore.remove('normAbbreviation')"
      ></ComboboxInput>
    </InputField>
    <div
      v-if="normAbbreviation"
      class="flex justify-between gap-24 border-t-1 border-blue-300 pt-24"
    >
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
          size="medium"
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
          v-model="norm.dateOfRelevance"
          aria-label="Jahr der Norm"
          :has-error="slotProps.hasError"
          size="medium"
          @update:validation-error="slotProps.updateValidationError"
        />
      </InputField>
    </div>
    <div class="flex w-full flex-row justify-between">
      <div>
        <div class="flex">
          <TextButton
            aria-label="Weitere Einzelnorm"
            button-type="tertiary"
            class="mr-16"
            :disabled="!norm.singleNorm"
            :icon="IconAdd"
            label="Weitere Einzelnorm"
            size="small"
          />
          <TextButton
            aria-label="Norm speichern"
            button-type="primary"
            class="mr-16"
            :disabled="norm.isEmpty"
            label="Übernehmen"
            size="small"
            @click.stop="addNormReference"
          />
          <TextButton
            v-if="!lastSavedModelValue.isEmpty"
            aria-label="Abbrechen"
            button-type="ghost"
            label="Abbrechen"
            size="small"
            @click.stop="emit('cancelEdit')"
          />
        </div>
      </div>
      <TextButton
        v-if="!lastSavedModelValue.isEmpty"
        aria-label="Eintrag löschen"
        button-type="destructive"
        label="Eintrag löschen"
        size="small"
        @click.stop="emit('removeListEntry')"
      />
    </div>
  </div>
</template>
