<script lang="ts" setup>
import Button from "primevue/button"
import InputSelect from "primevue/select"
import { computed, onMounted, ref, watch } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import InputField from "@/components/input/InputField.vue"
import MonetaryInput from "@/components/input/MonetaryInput.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import { addressee } from "@/data/addressee"
import AbuseFee from "@/domain/abuseFee"
import ComboboxItemService from "@/services/comboboxItemService"

const props = defineProps<{
  modelValue?: AbuseFee
  modelValueList?: AbuseFee[]
}>()

const emit = defineEmits<{
  "update:modelValue": [value: AbuseFee]
  addEntry: [void]
  cancelEdit: [void]
  removeEntry: [value?: boolean]
}>()

type AbuseFeeField = (typeof AbuseFee.fields)[number]
const validationStore = useValidationStore<AbuseFeeField>()

const lastSavedModelValue = ref(new AbuseFee({ ...props.modelValue }))
const abuseFee = ref(new AbuseFee({ ...props.modelValue }))
const isSixDigitNumber = computed(() => {
  return abuseFee.value.amount != null && abuseFee.value.amount <= 999999
})
async function addAbuseFee() {
  validate()
  emit("update:modelValue", abuseFee.value as AbuseFee)
  emit("addEntry")
}

function validate() {
  if (abuseFee.value.isEmpty) {
    validationStore.reset()
  } else {
    if (null == abuseFee.value.amount) {
      validationStore.add("Pflichtfeld nicht befüllt", "amount")
    } else if (isSixDigitNumber.value) {
      validationStore.remove("amount")
    } else {
      validationStore.add("Max. 6 Zeichen", "amount")
    }
    if (abuseFee.value.currencyCode) {
      validationStore.remove("currencyCode")
    } else {
      validationStore.add("Pflichtfeld nicht befüllt", "currencyCode")
    }
  }
}

watch(abuseFee, () => validate(), { deep: true })

watch(
  () => props.modelValue,
  () => {
    abuseFee.value = new AbuseFee({
      ...props.modelValue,
    })
    lastSavedModelValue.value = new AbuseFee({
      ...props.modelValue,
    })
    if (lastSavedModelValue.value.isEmpty) validationStore.reset()
  },
)

onMounted(() => {
  if (!props.modelValue?.isEmpty) {
    validate()
  }
  abuseFee.value = new AbuseFee({
    ...props.modelValue,
  })
})
</script>

<template>
  <div class="flex flex-col gap-24">
    <div class="flex flex-row gap-24">
      <div class="basis-1/3">
        <InputField
          id="abuseFeeAmount"
          v-slot="slotProps"
          label="Betrag *"
          :validation-error="validationStore.getByField('amount')"
        >
          <MonetaryInput
            :id="slotProps.id"
            v-model="abuseFee.amount"
            :has-error="slotProps.hasError"
          />
        </InputField>
      </div>
      <div class="basis-1/3">
        <InputField
          id="abuseFeeCurrencyInput"
          v-slot="slotProps"
          label="Währung *"
          :validation-error="validationStore.getByField('currencyCode')"
        >
          <ComboboxInput
            id="abuseFeeCurrencyInputText"
            v-model="abuseFee.currencyCode"
            aria-label="Währung"
            class="w-full"
            :invalid="slotProps.hasError"
            :item-service="ComboboxItemService.getCurrencyCodes"
          ></ComboboxInput>
        </InputField>
      </div>
      <div class="basis-1/3">
        <InputField id="abuseFeeAddressee" v-slot="{ id }" label="Adressat">
          <InputSelect
            :id="id"
            v-model="abuseFee.addressee"
            aria-label="Adressat"
            fluid
            option-label="label"
            option-value="value"
            :options="addressee"
          />
        </InputField>
      </div>
    </div>
    <div class="flex w-full flex-row justify-between">
      <div>
        <div class="flex gap-16">
          <Button
            aria-label="Gebühren speichern"
            :disabled="!validationStore.isValid() || abuseFee.isEmpty"
            label="Übernehmen"
            severity="secondary"
            size="small"
            @click.stop="addAbuseFee"
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
