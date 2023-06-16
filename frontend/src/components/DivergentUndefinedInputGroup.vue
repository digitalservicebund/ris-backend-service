<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import DivergentCategoryInputGroup from "@/components/DivergentCategoryInputGroup.vue"
import { Metadata, MetadataSectionName, UndefinedDate } from "@/domain/Norm"
import DropdownInput from "@/shared/components/input/DropdownInput.vue"
import InputField from "@/shared/components/input/InputField.vue"

interface Props {
  modelValue: Metadata
  id: string
  label: string
  sectionName: MetadataSectionName
}

interface Emits {
  (event: "update:modelValue", value: Metadata): void
}

interface DropdownItem {
  label: string
  value: string
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const inputValue = ref(props.modelValue)

watch(
  () => props.modelValue,
  (newValue) => {
    if (newValue !== undefined) {
      inputValue.value = newValue
    }
  },
  { immediate: true }
)

watch(inputValue, () => emit("update:modelValue", inputValue.value), {
  deep: true,
})

const ENTRY_INTO_FORCE_DATE_TRANSLATIONS: { [Value in UndefinedDate]: string } =
  {
    [UndefinedDate.UNDEFINED_UNKNOWN]: "unbestimmt (unbekannt)",
    [UndefinedDate.UNDEFINED_FUTURE]: "unbestimmt (zukünftig)",
    [UndefinedDate.UNDEFINED_NOT_PRESENT]: "nicht vorhanden",
  }

const dropdownItems: DropdownItem[] = Object.entries(
  ENTRY_INTO_FORCE_DATE_TRANSLATIONS
).map(([value, label]) => {
  return { label, value }
})

const undefinedDateState = computed({
  get: () => inputValue.value.UNDEFINED_DATE?.[0],
  set: (data?: UndefinedDate) =>
    data && (inputValue.value.UNDEFINED_DATE = [data]),
})
</script>

<template>
  <div class="flex flex-col gap-8 w-384">
    <InputField :id="id" :aria-label="label" :label="label">
      <DropdownInput
        :id="id + 'Dropdown'"
        v-model="undefinedDateState"
        :aria-label="label + ' Dropdown'"
        has-smaller-height
        :items="dropdownItems"
        placeholder="Bitte auswählen"
      />
    </InputField>
    <DivergentCategoryInputGroup
      :model-value="modelValue"
      :section-name="sectionName"
    />
  </div>
</template>
