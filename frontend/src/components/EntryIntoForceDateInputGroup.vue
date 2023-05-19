<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { Metadata, NormCategory, EntryIntoForceDropDown } from "@/domain/Norm"
import CheckboxInput from "@/shared/components/input/CheckboxInput.vue"
import DateInput from "@/shared/components/input/DateInput.vue"
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

enum InputType {
  FIRST = "date",
  SECOND = "year",
}

const inputValue = ref(props.modelValue)
const selectedInputType = ref<InputType>(InputType.FIRST)

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

const ENTRY_INTO_FORCE_DATE_TRANSLATIONS = {
  [EntryIntoForceDropDown.UNKNOWN]: "unbekannt",
  [EntryIntoForceDropDown.FUTURE]: "zukünftig",
  [EntryIntoForceDropDown.NOT_AVAILABLE]: "nicht vorhanden",
}

const dropdownItems: DropdownItem[] = Object.entries(
  ENTRY_INTO_FORCE_DATE_TRANSLATIONS
).map(([value, label]) => {
  return { label, value }
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
    <div class="radio-group w-320">
      <label class="form-control">
        <input
          v-model="selectedInputType"
          name="inputType"
          type="radio"
          :value="InputType.FIRST"
        />
        bestimmt
      </label>
      <label class="form-control">
        <input
          v-model="selectedInputType"
          name="inputType"
          type="radio"
          :value="InputType.SECOND"
        />
        unbestimmt
      </label>
    </div>
    <div v-if="selectedInputType === InputType.FIRST">
      <InputField
        aria-label="Zitierdatum Datum"
        label="Bestimmtes grundsätzliches Außerkrafttretedatum"
      >
        <DateInput is-future-date />
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
    <div v-if="selectedInputType === InputType.SECOND">
      <InputField
        id="ageIndicationEndUnit"
        aria-label="Einheit"
        label="Unbestimmtes abweichendes Außerkrafttretedatum"
      >
        <DropdownInput
          id="ageIndicationEndUnit"
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
  </div>
  <div></div>
</template>

<style lang="scss" scoped>
.radio-group {
  display: flex;
  justify-content: space-between;
  margin-bottom: 24px;
}

.form-control {
  display: flex;
  flex-direction: row;
  align-items: center;
}

input[type="radio"] {
  display: grid;
  width: 1.5em;
  height: 1.5em;
  border: 0.15em solid currentcolor;
  border-radius: 50%;
  margin-right: 10px;
  appearance: none;
  background-color: white;
  color: #004b76;
  place-content: center;
}

input[type="radio"]::before {
  width: 0.75em;
  height: 0.75em;
  border-radius: 50%;
  background-color: #004b76;
  content: "";
  transform: scale(0);
}

input[type="radio"]:checked::before {
  transform: scale(1);
}
</style>
