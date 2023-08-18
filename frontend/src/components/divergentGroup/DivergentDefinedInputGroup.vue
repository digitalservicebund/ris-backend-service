<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import DivergentCategoryInputGroup from "@/components/divergentGroup/DivergentCategoryInputGroup.vue"
import { Metadata, MetadataSectionName } from "@/domain/norm"
import DateInput from "@/shared/components/input/DateInput.vue"
import InputField from "@/shared/components/input/InputField.vue"

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
  { immediate: true },
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
  <div class="flex w-384 flex-col gap-8">
    <InputField
      :id="`${id}DateInput`"
      v-slot="{ id: dateInputId, hasError, updateValidationError }"
      :aria-label="label"
      :label="label"
    >
      <DateInput
        :id="dateInputId"
        v-model="date"
        :aria-label="`${label} Date Input`"
        :has-error="hasError"
        is-future-date
        @update:validation-error="updateValidationError"
      />
    </InputField>

    <DivergentCategoryInputGroup
      :model-value="modelValue"
      :section-name="sectionName"
    />
  </div>
</template>
