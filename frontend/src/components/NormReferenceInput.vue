<script lang="ts" setup>
import { computed } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import { NormReference } from "@/domain/normReference"
import ComboboxItemService from "@/services/comboboxItemService"
import CustomDateInput from "@/shared/components/input/CustomDateInput.vue"
import InputField from "@/shared/components/input/InputField.vue"
import TextInput from "@/shared/components/input/TextInput.vue"

const props = defineProps<{ modelValue: NormReference }>()
const emit = defineEmits<{
  (e: "update:modelValue", value: NormReference): void
}>()

const norm = computed({
  get() {
    return props.modelValue ?? {}
  },
  set(value) {
    emit("update:modelValue", value)
  },
})
</script>

<template>
  <div class="flex flex-row gap-24">
    <InputField id="norm-reference-abbreviation-field" label="RIS-Abkürzung">
      <ComboboxInput
        id="norm-reference-abbreviation"
        v-model="norm.risAbbreviation"
        aria-label="RIS-Abkürzung"
        clear-on-choosing-item
        :item-service="ComboboxItemService.getRisAbbreviations"
        placeholder="RIS Abkürzung"
      >
      </ComboboxInput>
    </InputField>

    <InputField id="norm-reference-abbreviation-field" label="Einzelnorm">
      <TextInput
        id="norm-reference-singleNorm"
        v-model="norm.singleNorm"
        aria-label="Einzelnorm"
      ></TextInput>
    </InputField>
    <InputField id="norm-date-of-version" label="Fassungsdatum">
      <CustomDateInput
        id="norm-date-of-version"
        v-model="norm.dateOfVersion"
        aria-label="Fassungsdatum"
      />
    </InputField>
    <InputField id="norm-date-of-relevence" label="Jahr">
      <TextInput
        id="norm-date-of-relevence"
        v-model="norm.dateOfRelevance"
        aria-label="Jahr"
      ></TextInput>
    </InputField>
  </div>
</template>
