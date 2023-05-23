<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { Metadata, NormCategory, UndefinedDate } from "@/domain/Norm"
import CheckboxInput from "@/shared/components/input/CheckboxInput.vue"
import DropdownInput from "@/shared/components/input/DropdownInput.vue"
import InputField, {
  LabelPosition,
} from "@/shared/components/input/InputField.vue"

interface Props {
  modelValue: Metadata
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

const NORM_CATEGORY_NAMES = {
  [NormCategory.AMENDMENT_NORM]: "Änderungsnorm",
  [NormCategory.BASE_NORM]: "Stammnorm",
  [NormCategory.TRANSITIONAL_NORM]: "Übergangsnorm",
}

const selectedNormCategories = ref<Record<NormCategory, boolean>>(
  {} as Record<NormCategory, boolean>
)

watch(
  selectedNormCategories,
  () => {
    inputValue.value.NORM_CATEGORY = (
      Object.keys(selectedNormCategories.value) as NormCategory[]
    ).filter((category) => selectedNormCategories.value[category])
  },
  { deep: true }
)

watch(
  () => inputValue.value.NORM_CATEGORY,
  (categories) => {
    for (const category of Object.values(NormCategory)) {
      selectedNormCategories.value[category] =
        categories?.includes(category) ?? false
    }
  },
  { immediate: true, deep: true }
)

const checkboxStyle = ref({
  width: "24px",
  height: "24px",
})
</script>

<template>
  <div class="w-384">
    <InputField
      id="divergentExpirationUndefinedDate"
      label="Unbestimmtes abweichendes Außerkrafttretedatum"
    >
      <DropdownInput
        id="divergentExpirationUndefinedDate"
        v-model="undefinedDateState"
        aria-label="Unbestimmtes abweichendes Außerkrafttretedatum Dropdown"
        has-smaller-height
        :items="dropdownItems"
        placeholder="Bitte auswählen"
      />
    </InputField>
    <div class="mb-24 mt-24">
      <InputField
        id="documentNormCategory"
        aria-label="Art der Norm"
        label="Art der Norm"
      >
        <div class="flex gap-24">
          <InputField
            v-for="category in NormCategory"
            :id="category"
            :key="category"
            :aria-label="NORM_CATEGORY_NAMES[category]"
            :label="NORM_CATEGORY_NAMES[category]"
            :label-position="LabelPosition.RIGHT"
          >
            <CheckboxInput
              :id="category"
              v-model="selectedNormCategories[category]"
              :aria-label="NORM_CATEGORY_NAMES[category]"
              :style="checkboxStyle"
            />
          </InputField>
        </div>
      </InputField>
    </div>
  </div>
</template>
