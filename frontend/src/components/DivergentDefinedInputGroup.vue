<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import DivergentCategoryInputGroup from "@/components/DivergentCategoryInputGroup.vue"
import { Metadata, MetadataSectionName } from "@/domain/Norm"
import DateInput from "@/shared/components/input/DateInput.vue"
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

const date = computed({
  get: () => inputValue.value.DATE?.[0],
  set: (data?: string) => (inputValue.value.DATE = data ? [data] : undefined),
})
</script>

<template>
  <div class="flex flex-col gap-8 w-384">
    <InputField :id="id" :aria-label="label" :label="label">
      <DateInput
        :id="id + 'DateInput'"
        v-model="date"
        :aria-label="label + ' Date Input'"
        is-future-date
      />
    </InputField>
    <DivergentCategoryInputGroup
      :model-value="modelValue"
      :section-name="sectionName"
    />
  </div>
</template>
