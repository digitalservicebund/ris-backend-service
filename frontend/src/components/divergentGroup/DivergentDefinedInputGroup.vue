<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import DivergentCategoryInputGroup from "@/components/divergentGroup/DivergentCategoryInputGroup.vue"
import { Metadata, MetadataSectionName } from "@/domain/Norm"
import InputElement from "@/shared/components/input/InputElement.vue"
import InputField from "@/shared/components/input/InputField.vue"
import { InputType } from "@/shared/components/input/types"

interface Props {
  modelValue: Metadata
  id: string
  label: string
  sectionName: MetadataSectionName
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value: Metadata]
}>()

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

const date = computed({
  get: () => inputValue.value.DATE?.[0],
  set: (data?: string) => (inputValue.value.DATE = data ? [data] : undefined),
})
</script>

<template>
  <div class="flex flex-col gap-8 w-384">
    <InputField :id="id" :aria-label="label" :label="label">
      <InputElement
        :id="id + 'DateInput'"
        v-model="date"
        :alt-text="`${label} Datum`"
        :attributes="{ ariaLabel: label + ' Date Input' }"
        is-future-date
        :type="InputType.DATE"
      />
    </InputField>
    <DivergentCategoryInputGroup
      :model-value="modelValue"
      :section-name="sectionName"
    />
  </div>
</template>
