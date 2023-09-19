<script lang="ts" setup>
import { produce } from "immer"
import { computed } from "vue"
import { MetadataSectionName, NormCategory } from "@/domain/norm"
import CheckboxInput from "@/shared/components/input/CheckboxInput.vue"
import InputField, {
  LabelPosition,
} from "@/shared/components/input/InputField.vue"

const props = defineProps<{
  modelValue: NormCategory[]
  sectionName: MetadataSectionName
}>()

const emit = defineEmits<{
  "update:modelValue": [value: NormCategory[]]
}>()

const NORM_CATEGORY_NAMES = {
  [NormCategory.AMENDMENT_NORM]: "Änderungsnorm",
  [NormCategory.BASE_NORM]: "Stammnorm",
  [NormCategory.TRANSITIONAL_NORM]: "Übergangsnorm",
}

const localModelValue = computed(() =>
  props.modelValue.reduce<Partial<Record<NormCategory, true>>>(
    (all, current) => {
      all[current] = true
      return all
    },
    {},
  ),
)

function toggleCategory(category: NormCategory, value?: boolean) {
  const next = produce(localModelValue.value, (draft) => {
    if (value) draft[category] = true
    else delete draft[category]
  })

  emit("update:modelValue", Object.keys(next) as NormCategory[])
}
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
          :aria-label="NORM_CATEGORY_NAMES[category]"
          :model-value="localModelValue[category] === true"
          size="small"
          @update:model-value="toggleCategory(category, $event)"
        />
      </InputField>
    </div>
  </InputField>
</template>
