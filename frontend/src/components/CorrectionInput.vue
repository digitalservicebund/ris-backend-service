<script lang="ts" setup>
import Button from "primevue/button"
import InputText from "primevue/inputtext"
import InputSelect from "primevue/select"
import { type Component, onMounted, ref, watch } from "vue"
import ChipsBorderNumberInput from "@/components/input/ChipsBorderNumberInput.vue"
import DateInput from "@/components/input/DateInput.vue"
import InputField from "@/components/input/InputField.vue"
import TextEditor from "@/components/input/TextEditor.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import Correction, { CORRECTION_TYPES } from "@/domain/correction"

const props = defineProps<{
  modelValue?: Correction
  modelValueList?: Correction[]
  registerTextEditorRef: (key: string, el: Component | null) => void
}>()

const emit = defineEmits<{
  "update:modelValue": [value: Correction]
  addEntry: [void]
  cancelEdit: [void]
  removeEntry: [value?: boolean]
}>()

const lastSavedModelValue = ref(new Correction({ ...props.modelValue }))
const correction = ref(new Correction({ ...props.modelValue }))

const validationStore = useValidationStore<(typeof Correction.fields)[number]>()

function validateRequiredInput() {
  validationStore.reset()

  for (const missingField of correction.value.missingRequiredFields) {
    validationStore.add("Pflichtfeld nicht befüllt", missingField)
  }
}

async function addCorrection() {
  validateRequiredInput()
  emit("update:modelValue", correction.value as Correction)
  emit("addEntry")
}

watch(
  correction,
  () => {
    if (!correction.value.isEmpty) {
      validateRequiredInput()
    }
  },
  { deep: true },
)

watch(
  () => props.modelValue,
  () => {
    correction.value = new Correction({ ...props.modelValue })
    lastSavedModelValue.value = new Correction({ ...props.modelValue })
    if (lastSavedModelValue.value.isEmpty) validationStore.reset()
  },
)

onMounted(() => {
  correction.value = new Correction({ ...props.modelValue })
})
</script>

<template>
  <div class="flex flex-col gap-24">
    <div class="flex justify-between gap-24">
      <InputField
        id="correctionTypeInput"
        v-slot="slotProps"
        label="Art der Eintragung *"
        :validation-error="validationStore.getByField('type')"
      >
        <InputSelect
          :id="slotProps.id"
          v-model="correction.type"
          aria-label="Art der Eintragung"
          fluid
          :options="[...CORRECTION_TYPES]"
          show-clear
        />
      </InputField>
      <InputField
        id="correctionDescriptionInput"
        label="Art der Änderung"
        :validation-error="validationStore.getByField('description')"
      >
        <InputText
          id="correctionDescriptionTextInput"
          v-model="correction.description"
          aria-label="Art der Änderung"
          fluid
          size="small"
        ></InputText>
      </InputField>
    </div>
    <div class="flex justify-between gap-24">
      <InputField
        id="correctionDateInput"
        v-slot="slotProps"
        label="Datum der Änderung"
        :validation-error="validationStore.getByField('date')"
      >
        <DateInput
          id="correctionDateInputText"
          v-model="correction.date"
          aria-label="Datum der Änderung"
          :has-error="slotProps.hasError"
          @focus="validationStore.remove('date')"
          @update:validation-error="slotProps.updateValidationError"
        ></DateInput>
      </InputField>
      <InputField
        id="correctionBorderNumbersInput"
        v-slot="slotProps"
        label="Randnummern der Änderung"
      >
        <ChipsBorderNumberInput
          :id="slotProps.id"
          v-model="correction.borderNumbers"
          aria-label="Randnummern der Änderung"
          class="w-full"
          :has-error="slotProps.hasError"
          size="small"
          @focus="validationStore.remove('borderNumbers')"
          @update:validation-error="slotProps.updateValidationError"
        />
      </InputField>
    </div>
    <div class="flex flex-col">
      <label class="ris-label2-regular mb-4" for="correctionContent">
        Inhalt der Änderung
      </label>

      <TextEditor
        id="correctionContent"
        :ref="(el) => registerTextEditorRef('correctionContent', el)"
        aria-label="Inhalt der Änderung"
        category="correctionContent"
        class="shadow-blue focus-within:shadow-focus hover:shadow-hover"
        data-testid="correctionContent-editor"
        editable
        field-size="big"
        hide-text-check
        :value="correction.content"
        @update-value="correction.content = $event"
      />
    </div>
    <div class="flex w-full flex-row justify-between">
      <div>
        <div class="flex gap-16">
          <Button
            aria-label="Berichtigung speichern"
            :disabled="correction.hasMissingRequiredFields"
            label="Übernehmen"
            severity="secondary"
            size="small"
            @click.stop="addCorrection"
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
