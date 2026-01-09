<script lang="ts" setup>
import Button from "primevue/button"
import InputText from "primevue/inputtext"
import { computed, ref, watch } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import InputField from "@/components/input/InputField.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import LegalPeriodical from "@/domain/legalPeriodical"
import Reference from "@/domain/reference"
import ComboboxItemServices from "@/services/comboboxItemService"

const props = defineProps<{
  modelValue?: Reference
  modelValueList?: Reference[]
}>()

const emit = defineEmits<{
  "update:modelValue": [value: Reference]
  addEntry: [void]
  cancelEdit: [void]
  removeEntry: [value?: boolean]
}>()

const reference = ref(new Reference({ ...props.modelValue }))
const lastSavedModelValue = ref(new Reference({ ...props.modelValue }))

const validationStore = useValidationStore<(typeof Reference.fields)[number]>()

const legalPeriodical = computed({
  get: () => reference?.value?.legalPeriodical,
  set: (newValue) => {
    const legalPeriodical = { ...newValue } as LegalPeriodical
    if (newValue) {
      reference.value.legalPeriodical = legalPeriodical
      reference.value.legalPeriodicalRawValue = legalPeriodical.abbreviation
      reference.value.primaryReference = legalPeriodical.primaryReference
    } else {
      reference.value.legalPeriodical = undefined
      reference.value.legalPeriodicalRawValue = undefined
      reference.value.primaryReference = undefined
    }
  },
})

async function validateRequiredInput() {
  validationStore.reset()
  reference.value.missingRequiredLiteratureFields.forEach((missingField) =>
    validationStore.add("Pflichtfeld nicht befüllt", missingField),
  )
}

async function addReference() {
  await validateRequiredInput()

  if (!reference.value.hasMissingRequiredLiteratureFields) {
    const literatureReference = new Reference({
      ...reference.value,
      referenceType: "literature",
    })
    emit("update:modelValue", literatureReference)
    emit("addEntry")
  }
}

/**
 * This updates the local reference with the updated model value from the props. It also stores a copy of the last saved
 * model value, because the local reference might change in between.
 */
watch(
  () => props.modelValue,
  () => {
    reference.value = new Reference({ ...props.modelValue })
    lastSavedModelValue.value = new Reference({ ...props.modelValue })
    if (lastSavedModelValue.value.isEmpty) {
      validationStore.reset()
    } else if (
      !lastSavedModelValue.value.legalPeriodical &&
      !!lastSavedModelValue.value.legalPeriodicalRawValue
    ) {
      validationStore.add("Mehrdeutiger Verweis", "legalPeriodical")
    }
  },
  { immediate: true },
)
</script>

<template>
  <div
    id="literature-reference-input"
    class="flex flex-col gap-24"
    data-testid="literature-reference-input"
  >
    <InputField
      id="legalPeriodical"
      v-slot="slotProps"
      label="Periodikum *"
      :validation-error="validationStore.getByField('legalPeriodical')"
    >
      <ComboboxInput
        id="legalPeriodical"
        v-model="legalPeriodical"
        aria-label="Periodikum Literaturfundstelle"
        :has-error="slotProps.hasError"
        :item-service="ComboboxItemServices.getLegalPeriodicals"
        @show="validationStore.remove('legalPeriodical')"
      ></ComboboxInput>
    </InputField>

    <div class="flex flex-col gap-24">
      <div class="flex justify-between gap-24">
        <div class="flex w-full flex-col">
          <InputField
            id="citation"
            v-slot="slotProps"
            label="Zitatstelle *"
            :validation-error="validationStore.getByField('citation')"
          >
            <InputText
              id="citation"
              v-model="reference.citation"
              aria-label="Zitatstelle Literaturfundstelle"
              fluid
              :invalid="slotProps.hasError"
              size="small"
              @focus="validationStore.remove('citation')"
            />
          </InputField>
          <span v-if="legalPeriodical" class="ris-label3-regular pt-4"
            >Zitierbeispiel: {{ legalPeriodical.citationStyle }}</span
          >
        </div>

        <InputField
          id="literatureReferenceDocumentType"
          v-slot="slotProps"
          label="Dokumenttyp *"
          :validation-error="validationStore.getByField('documentType')"
        >
          <ComboboxInput
            id="literatureReferenceDocumentType"
            v-model="reference.documentType"
            aria-label="Dokumenttyp Literaturfundstelle"
            :has-error="slotProps.hasError"
            :item-service="
              ComboboxItemServices.getDependentLiteratureDocumentTypes
            "
            @show="validationStore.remove('documentType')"
          ></ComboboxInput>
        </InputField>
      </div>
      <div class="w-[calc(50%-10px)]">
        <InputField
          id="literatureReferenceDocumentType"
          v-slot="slotProps"
          label="Autor *"
          :validation-error="validationStore.getByField('author')"
        >
          <InputText
            id="literatureReferenceDocumentType"
            v-model="reference.author"
            aria-label="Autor Literaturfundstelle"
            fluid
            :invalid="slotProps.hasError"
            size="small"
            @focus="validationStore.remove('author')"
          />
        </InputField>
      </div>
    </div>
    <div class="flex w-full flex-row justify-between">
      <div>
        <div class="flex gap-16">
          <Button
            aria-label="Literaturfundstelle speichern"
            :disabled="reference.isEmpty"
            label="Übernehmen"
            severity="secondary"
            size="small"
            @click.stop="addReference"
          ></Button>
          <Button
            v-if="!lastSavedModelValue.isEmpty"
            aria-label="Abbrechen"
            label="Abbrechen"
            size="small"
            text
            @click.stop="emit('cancelEdit')"
          ></Button>
        </div>
      </div>
      <Button
        v-if="!lastSavedModelValue.isEmpty"
        aria-label="Eintrag löschen"
        label="Eintrag löschen"
        severity="danger"
        size="small"
        @click.stop="emit('removeEntry', true)"
      ></Button>
    </div>
  </div>
</template>
