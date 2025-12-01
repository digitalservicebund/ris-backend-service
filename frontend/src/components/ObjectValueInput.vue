<script lang="ts" setup>
import Button from "primevue/button"
import InputNumber, { InputNumberInputEvent } from "primevue/inputnumber"
import InputSelect from "primevue/select"
import { computed, onMounted, ref, watch } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import InputField from "@/components/input/InputField.vue"
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

type ObjectValueField = (typeof ObjectValue.fields)[number]
const validationStore = useValidationStore<ObjectValueField>()

const lastSavedModelValue = ref(new ObjectValue({ ...props.modelValue }))
const objectValue = ref(new ObjectValue({ ...props.modelValue }))
const isSixDigitNumber = computed(() => {
  return !!objectValue.value.amount && objectValue.value.amount <= 999999
})
async function addObjectValue() {
  validate()
  emit("update:modelValue", objectValue.value as ObjectValue)
  emit("addEntry")
}

function validate() {
  if (objectValue.value.isEmpty) {
    validationStore.reset()
  } else {
    if (objectValue.value.amount) {
      if (isSixDigitNumber.value) {
        validationStore.remove("amount")
      } else {
        validationStore.add("Max. 6 Zeichen", "amount")
      }
    } else {
      validationStore.add("Pflichtfeld nicht befüllt", "amount")
    }
    if (objectValue.value.currencyCode) {
      validationStore.remove("currencyCode")
    } else {
      validationStore.add("Pflichtfeld nicht befüllt", "currencyCode")
    }
  }
}

function onInput(event: InputNumberInputEvent) {
  objectValue.value.amount = event.value as number
}

watch(objectValue, () => validate(), { deep: true })

watch(
  () => props.modelValue,
  () => {
    objectValue.value = new ObjectValue({
      ...props.modelValue,
    })
    lastSavedModelValue.value = new ObjectValue({
      ...props.modelValue,
    })
    if (lastSavedModelValue.value.isEmpty) validationStore.reset()
  },
)

onMounted(() => {
  if (!props.modelValue?.isEmpty) {
    validate()
  }
  objectValue.value = new ObjectValue({
    ...props.modelValue,
  })
})
</script>

<template>
  <div class="flex flex-col gap-24">
    <div class="flex flex-row gap-24">
      <div class="basis-1/3">
        <InputField
          id="objectValueAmount"
          v-slot="slotProps"
          data-testid="object-value-amount"
          label="Betrag *"
          :validation-error="validationStore.getByField('amount')"
        >
          <InputNumber
            :id="slotProps.id"
            v-model="objectValue.amount"
            aria-label="Betrag"
            class="w-full"
            data-testid="object-value-amount-input"
            fluid
            input-class="w-full"
            :invalid="slotProps.hasError"
            locale="de"
            :min="1"
            @blur="validate"
            @focus="validationStore.remove('amount')"
            @input="onInput"
          />
        </InputField>
      </div>
      <div class="basis-1/3">
        <InputField
          id="objectValueCurrencyInput"
          v-slot="slotProps"
          data-testid="object-value-currency"
          label="Währung *"
          :validation-error="validationStore.getByField('currencyCode')"
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
            data-testid="object-value-proceeding-type-input"
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
            :disabled="!validationStore.isValid() || objectValue.isEmpty"
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
