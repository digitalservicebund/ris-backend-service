<script lang="ts" setup>
import { computed, onMounted, ref } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import NormReference from "@/domain/normReference"
import ComboboxItemService from "@/services/comboboxItemService"
import FeatureToggleService from "@/services/featureToggleService"
import DateInput from "@/shared/components/input/DateInput.vue"
import InputField from "@/shared/components/input/InputField.vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import YearInput from "@/shared/components/input/YearInput.vue"

const props = defineProps<{ modelValue?: NormReference }>()
const emit =
  defineEmits<(e: "update:modelValue", value: NormReference) => void>()

const norm = computed({
  get() {
    return (props.modelValue as NormReference) ?? {}
  },
  set(value) {
    emit("update:modelValue", value)
  },
})

const normAbbreviation = computed({
  get: () =>
    norm?.value?.normAbbreviation
      ? {
          ...norm.value.normAbbreviation,
          label: norm.value.normAbbreviation.abbreviation,
        }
      : undefined,
  set: (newValue) => {
    let normRef = new NormReference()
    if (newValue) {
      normRef = new NormReference({
        ...norm.value,
        normAbbreviation: newValue,
      })
    } else delete normRef.normAbbreviation
    emit("update:modelValue", normRef)
  },
})

const disableRisAbbreviationInput = ref(false)
onMounted(async () => {
  const response = await FeatureToggleService.isEnabled(
    "neuris.disable-ris-abbreviation-input"
  )
  disableRisAbbreviationInput.value = !!response.data
})
</script>

<template>
  <div>
    <span v-if="disableRisAbbreviationInput">feature toggle enabled</span>
    <InputField id="norm-reference-search-field" label="Suchfeld">
      <ComboboxInput
        id="norm-reference-search"
        v-model="normAbbreviation"
        aria-label="Norm Suchfeld"
        clear-on-choosing-item
        :item-service="ComboboxItemService.getRisAbbreviationsAwesome"
        placeholder="Suchfeld"
      ></ComboboxInput>
    </InputField>
    <InputField id="norm-reference-abbreviation-field" label="RIS-Abkürzung">
      <ComboboxInput
        id="norm-reference-abbreviation"
        v-model="normAbbreviation"
        aria-label="Norm RIS-Abkürzung"
        clear-on-choosing-item
        :item-service="ComboboxItemService.getRisAbbreviations"
        placeholder="RIS Abkürzung"
      >
      </ComboboxInput>
    </InputField>
    <div class="flex gap-24 justify-between">
      <InputField id="norm-reference-abbreviation-field" label="Einzelnorm">
        <TextInput
          id="norm-reference-singleNorm"
          v-model="norm.singleNorm"
          aria-label="Norm Einzelnorm"
        ></TextInput>
      </InputField>
      <InputField id="norm-date-of-version" label="Fassungsdatum">
        <DateInput
          id="norm-date-of-version"
          v-model="norm.dateOfVersion"
          aria-label="Norm Fassungsdatum"
        />
      </InputField>
      <InputField id="norm-date-of-relevence" label="Jahr">
        <YearInput
          id="norm-date-of-relevence"
          v-model="norm.dateOfRelevance"
          aria-label="Norm Jahr"
        />
      </InputField>
    </div>
  </div>
</template>
