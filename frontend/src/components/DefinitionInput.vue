<script lang="ts" setup>
import { storeToRefs } from "pinia"
import Button from "primevue/button"
import InputNumber from "primevue/inputnumber"
import InputText from "primevue/inputtext"
import { computed, onMounted, ref, watch } from "vue"
import InputField from "@/components/input/InputField.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import Definition from "@/domain/definition"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const props = defineProps<{
  modelValue?: Definition
  modelValueList?: Definition[]
}>()

const emit = defineEmits<{
  "update:modelValue": [value: Definition]
  addEntry: [void]
  cancelEdit: [void]
  removeEntry: [value?: boolean]
}>()

const lastSavedModelValue = ref(new Definition({ ...props.modelValue }))
const definition = ref(new Definition({ ...props.modelValue }))

const { documentUnit } = storeToRefs(useDocumentUnitStore())

type DefinitionField = (typeof Definition.fields)[number]
const validationStore = useValidationStore<DefinitionField>()

const borderNumberInvalid = computed(
  () =>
    definition.value.definingBorderNumber &&
    !documentUnit.value?.managementData.borderNumbers.includes(
      `${definition.value.definingBorderNumber}`,
    ),
)

async function addDefinition() {
  validate()
  emit("update:modelValue", definition.value as Definition)
  emit("addEntry")
}

function validate() {
  if (definition.value.isEmpty) {
    validationStore.reset()
  } else {
    if (definition.value.definedTerm) {
      validationStore.remove("definedTerm")
    } else {
      validationStore.add("Pflichtfeld nicht befüllt", "definedTerm")
    }
    if (borderNumberInvalid.value) {
      validationStore.add("Randnummer existiert nicht", "definingBorderNumber")
    } else {
      validationStore.remove("definingBorderNumber")
    }
  }
}

watch(definition, () => validate(), { deep: true })

watch(
  () => props.modelValue,
  () => {
    definition.value = new Definition({ ...props.modelValue })
    lastSavedModelValue.value = new Definition({ ...props.modelValue })
    if (lastSavedModelValue.value.isEmpty) validationStore.reset()
  },
)

onMounted(() => {
  if (!props.modelValue?.isEmpty) {
    validate()
  }
  definition.value = new Definition({ ...props.modelValue })
})
</script>

<template>
  <div class="flex flex-col gap-24">
    <div class="flex gap-24">
      <InputField
        id="definitionDefinedTermInput"
        v-slot="slotProps"
        data-testid="definition-defined-term"
        label="Definierter Begriff *"
        :validation-error="validationStore.getByField('definedTerm')"
      >
        <InputText
          id="definitionDefinedTermInputText"
          v-model="definition.definedTerm"
          aria-label="Definierter Begriff"
          class="flex-1"
          data-testid="definition-defined-term-input"
          :invalid="slotProps.hasError"
          placeholder="Begriff"
          @blur="validate"
          @focus="validationStore.remove('definedTerm')"
        ></InputText>
      </InputField>
      <InputField
        id="definitionDefiningBorderNumberInput"
        v-slot="slotProps"
        data-testid="definition-defining-border-number"
        label="Definition des Begriffs"
        :validation-error="validationStore.getByField('definingBorderNumber')"
      >
        <InputNumber
          id="definitionDefiningBorderNumberInputText"
          v-model="definition.definingBorderNumber"
          aria-label="Definition des Begriffs"
          class="w-full"
          data-testid="definition-defining-border-number-input"
          input-class="w-full"
          :invalid="slotProps.hasError"
          placeholder="Randnummer"
          :use-grouping="false"
          @blur="validate"
          @focus="validationStore.remove('definingBorderNumber')"
        ></InputNumber>
      </InputField>
    </div>
    <div class="flex w-full flex-row justify-between">
      <div>
        <div class="flex gap-16">
          <Button
            aria-label="Definition speichern"
            :disabled="!validationStore.isValid() || definition.isEmpty"
            label="Übernehmen"
            severity="secondary"
            size="small"
            @click.stop="addDefinition"
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
