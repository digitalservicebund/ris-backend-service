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
  reference.value.missingRequiredFieldsForDocunit.forEach((missingField) =>
    validationStore.add("Pflichtfeld nicht befüllt", missingField),
  )
}

async function addReference() {
  await validateRequiredInput()

  if (!reference.value.hasMissingRequiredFieldsForDocunit) {
    emit("update:modelValue", reference.value as Reference)
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
    id="caselaw-reference-input"
    class="flex flex-col gap-24"
    data-testid="caselaw-reference-input"
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
        aria-label="Periodikum"
        :has-error="slotProps.hasError"
        :item-service="ComboboxItemServices.getLegalPeriodicals"
        @focus="validationStore.remove('legalPeriodical')"
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
              aria-label="Zitatstelle"
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
          id="referenceSupplement"
          v-slot="slotProps"
          label="Klammernzusatz"
          :validation-error="validationStore.getByField('referenceSupplement')"
        >
          <InputText
            id="referenceSupplement"
            v-model="reference.referenceSupplement"
            aria-label="Klammernzusatz"
            fluid
            :invalid="slotProps.hasError"
            size="small"
            @focus="validationStore.remove('referenceSupplement')"
          />
        </InputField>
      </div>
    </div>
    <div class="flex w-full flex-row justify-between">
      <div>
        <div class="flex gap-16">
          <Button
            aria-label="Fundstelle speichern"
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
