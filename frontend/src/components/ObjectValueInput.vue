<script lang="ts" setup>
import Button from "primevue/button"
import InputSelect from "primevue/select"
import { onMounted, ref, watch } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import InputField from "@/components/input/InputField.vue"
import MonetaryInput from "@/components/input/MonetaryInput.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import { proceedingType } from "@/data/proceedingType"
import ObjectValue from "@/domain/objectValue"
import ComboboxItemService from "@/services/comboboxItemService"

const props = defineProps<{
  modelValue?: ObjectValue
  modelValueList?: ObjectValue[]
}>()

const emit = defineEmits<{
  "update:modelValue": [value: ObjectValue]
  addEntry: [void]
  cancelEdit: [void]
  removeEntry: [value?: boolean]
}>()

const validationStore = useValidationStore<["amount"][number]>()

const lastSavedModelValue = ref(new ObjectValue({ ...props.modelValue }))
const objectValue = ref(new ObjectValue({ ...props.modelValue }))

async function addObjectValue() {
  emit("update:modelValue", objectValue.value as ObjectValue)
  emit("addEntry")
}

watch(
  () => props.modelValue,
  () => {
    objectValue.value = new ObjectValue({
      ...props.modelValue,
    })
    lastSavedModelValue.value = new ObjectValue({
      ...props.modelValue,
    })
  },
)

onMounted(() => {
  objectValue.value = new ObjectValue({
    ...props.modelValue,
  })
})
</script>

<template>
  <div class="flex flex-col gap-24">
    <div class="flex flex-row gap-24">
      <div>
        <InputField
          id="objectValueAmount"
          v-slot="slotProps"
          data-testid="object-value-amount"
          label="Betrag *"
        >
          <MonetaryInput
            :id="slotProps.id"
            v-model="objectValue.amount"
            aria-label="Betrag"
            :has-error="slotProps.hasError"
            @focus="validationStore.remove('amount')"
            @update:validation-error="slotProps.updateValidationError"
          ></MonetaryInput>
        </InputField>
      </div>
      <div class="basis-1/3">
        <InputField
          id="objectValueCurrencyInput"
          v-slot="slotProps"
          data-testid="object-value-currency"
          label="Währung *"
        >
          <ComboboxInput
            id="objectValueCurrencyInputText"
            v-model="objectValue.currencyCode"
            aria-label="Währung"
            class="w-full"
            data-testid="object-value-currency-input"
            :invalid="slotProps.hasError"
            :item-service="ComboboxItemService.getCurrencyCodes"
          ></ComboboxInput>
        </InputField>
      </div>
      <div class="basis-1/3">
        <InputField
          id="objectValueProceedingType"
          v-slot="{ id }"
          data-testid="object-value-proceeding-type"
          label="Verfahren"
        >
          <InputSelect
            :id="id"
            v-model="objectValue.proceedingType"
            aria-label="Verfahren"
            fluid
            option-label="label"
            option-value="value"
            :options="proceedingType"
          />
        </InputField>
      </div>
    </div>
    <div class="flex w-full flex-row justify-between">
      <div>
        <div class="flex gap-16">
          <Button
            aria-label="Gegenstandswert speichern"
            :disabled="objectValue.isEmpty"
            label="Übernehmen"
            severity="secondary"
            size="small"
            @click.stop="addObjectValue"
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
