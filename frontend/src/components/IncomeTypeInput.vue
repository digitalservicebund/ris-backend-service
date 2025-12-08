<script lang="ts" setup>
import Button from "primevue/button"
import InputText from "primevue/inputtext"
import InputSelect from "primevue/select"
import { onMounted, ref, watch } from "vue"
import InputField from "@/components/input/InputField.vue"
import IncomeType, { typeOfIncomeItems } from "@/domain/incomeType"

const props = defineProps<{
  modelValue?: IncomeType
  modelValueList?: IncomeType[]
}>()

const emit = defineEmits<{
  "update:modelValue": [value: IncomeType]
  addEntry: [void]
  cancelEdit: [void]
  removeEntry: [value?: boolean]
}>()

const lastSavedModelValue = ref(new IncomeType({ ...props.modelValue }))
const incomeType = ref(new IncomeType({ ...props.modelValue }))

async function addIncomeType() {
  emit("update:modelValue", incomeType.value as IncomeType)
  emit("addEntry")
}

watch(
  () => props.modelValue,
  () => {
    incomeType.value = new IncomeType({ ...props.modelValue })
    lastSavedModelValue.value = new IncomeType({ ...props.modelValue })
  },
)

onMounted(() => {
  incomeType.value = new IncomeType({ ...props.modelValue })
})
</script>

<template>
  <div class="flex flex-col gap-24">
    <div class="flex gap-24">
      <InputField id="typeOfIncomeInput" v-slot="{ id }" label="Einkunftsart">
        <InputSelect
          :id="id"
          v-model="incomeType.typeOfIncome"
          class="flex-1"
          option-label="label"
          option-value="value"
          :options="typeOfIncomeItems"
          placeholder="Bitte auswählen"
        />
      </InputField>
      <InputField
        id="incomeTypeTerminologyInput"
        v-slot="{ id }"
        label="Begrifflichkeit"
      >
        <InputText
          :id="id"
          v-model="incomeType.terminology"
          aria-label="Begrifflichkeit"
          class="flex-1"
        ></InputText>
      </InputField>
    </div>
    <div class="flex w-full flex-row justify-between">
      <div>
        <div class="flex gap-16">
          <Button
            aria-label="Einkunftsart speichern"
            :disabled="incomeType.isEmpty"
            label="Übernehmen"
            severity="secondary"
            size="small"
            @click.stop="addIncomeType"
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
