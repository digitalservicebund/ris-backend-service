<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { Metadata, RangeUnit } from "@/domain/Norm"
import DropdownInput from "@/shared/components/input/DropdownInput.vue"
import InputField from "@/shared/components/input/InputField.vue"
import TextInput from "@/shared/components/input/TextInput.vue"

interface Props {
  modelValue: Metadata
}

interface Emits {
  (event: "update:modelValue", value: Metadata): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

interface DropdownItem {
  label: string
  value: string
}

const RANGE_UNIT_TRANSLATIONS: { [Value in RangeUnit]: string } = {
  [RangeUnit.YEARS]: "Jahr",
  [RangeUnit.MONTHS]: "Monat",
  [RangeUnit.WEEKS]: "Woche",
  [RangeUnit.DAYS]: "Tag",
  [RangeUnit.HOURS]: "Stunde",
  [RangeUnit.MINUTES]: "Minute",
  [RangeUnit.SECONDS]: "Sekunde",
  [RangeUnit.YEARS_OF_LIFE]: "Lebensjahre",
  [RangeUnit.MONTHS_OF_LIFE]: "Lebensmonate",
}

const dropdownItems: DropdownItem[] = Object.entries(
  RANGE_UNIT_TRANSLATIONS
).map(([value, label]) => {
  return { label, value }
})

const inputValue = ref(props.modelValue)

const rangeStart = computed({
  get: () => inputValue.value.RANGE_START?.[0],
  set: (data?: string) => data && (inputValue.value.RANGE_START = [data]),
})

const rangeStartUnit = computed({
  get: () => inputValue.value.RANGE_START_UNIT?.[0],
  set: (data?: RangeUnit) =>
    data && (inputValue.value.RANGE_START_UNIT = [data]),
})

const rangeEnd = computed({
  get: () => inputValue.value.RANGE_END?.[0],
  set: (data?: string) => data && (inputValue.value.RANGE_END = [data]),
})

const rangeEndUnit = computed({
  get: () => inputValue.value.RANGE_END_UNIT?.[0],
  set: (data?: RangeUnit) => data && (inputValue.value.RANGE_END_UNIT = [data]),
})

watch(props, () => (inputValue.value = props.modelValue), {
  immediate: true,
  deep: true,
})

watch(inputValue, () => emit("update:modelValue", inputValue.value), {
  deep: true,
})
</script>

<template>
  <div class="pb-32">
    <div>
      <div class="flex">
        <InputField
          id="ageIndicationStart"
          aria-label="Anfang"
          class="w-1/2"
          label="Anfang"
        >
          <TextInput
            id="ageIndicationStart"
            v-model="rangeStart"
            aria-label="Anfang"
          />
        </InputField>
        <InputField
          id="ageIndicationStartUnit"
          aria-label="Einheit"
          class="w-1/2"
          label="Einheit"
        >
          <DropdownInput
            id="ageIndicationStartUnit"
            v-model="rangeStartUnit"
            aria-label="Altersangabe Starteinheit"
            class="[&:not(:hover,:focus)]:border-l-0"
            has-smaller-height
            :items="dropdownItems"
            placeholder="Bitte auswählen"
          />
        </InputField>
      </div>
      <div class="flex mt-24">
        <InputField
          id="ageIndicationEnd"
          aria-label="Ende"
          class="w-1/2"
          label="Ende"
        >
          <TextInput
            id="ageIndicationEnd"
            v-model="rangeEnd"
            aria-label="Ende"
          />
        </InputField>
        <InputField
          id="ageIndicationEndUnit"
          aria-label="Einheit"
          class="w-1/2"
          label="Einheit"
        >
          <DropdownInput
            id="ageIndicationEndUnit"
            v-model="rangeEndUnit"
            aria-label="Altersangabe Endgerät"
            has-smaller-height
            :items="dropdownItems"
            placeholder="Bitte auswählen"
          />
        </InputField>
      </div>
    </div>
  </div>
</template>
