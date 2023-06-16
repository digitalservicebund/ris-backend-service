<script lang="ts" setup>
import { ref, watch } from "vue"
import { Metadata, MetadataSectionName, NormCategory } from "@/domain/Norm"
import CheckboxInput from "@/shared/components/input/CheckboxInput.vue"
import InputField, {
  LabelPosition,
} from "@/shared/components/input/InputField.vue"

interface Props {
  modelValue: Metadata
  sectionName: MetadataSectionName
}

interface Emits {
  (event: "update:modelValue", value: Metadata): void
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
  <InputField
    id="documentNormCategory"
    aria-label="Art der Norm"
    label="Art der Norm"
  >
    <div class="flex gap-24">
      <InputField
        v-for="category in NormCategory"
        :id="[sectionName, category].join('-')"
        :key="category"
        :aria-label="NORM_CATEGORY_NAMES[category]"
        :label="NORM_CATEGORY_NAMES[category]"
        :label-position="LabelPosition.RIGHT"
      >
        <CheckboxInput
          :id="[sectionName, category].join('-')"
          v-model="selectedNormCategories[category]"
          :aria-label="NORM_CATEGORY_NAMES[category]"
          :style="checkboxStyle"
        />
      </InputField>
    </div>
  </InputField>
</template>
