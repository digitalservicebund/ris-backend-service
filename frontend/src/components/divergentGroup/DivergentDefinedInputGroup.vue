<script lang="ts" setup>
import { produce } from "immer"
import { computed } from "vue"
import DivergentCategoryInputGroup from "@/components/divergentGroup/DivergentCategoryInputGroup.vue"
import { Metadata, MetadataSectionName } from "@/domain/norm"
import DateInput from "@/shared/components/input/DateInput.vue"
import InputField from "@/shared/components/input/InputField.vue"

const props = defineProps<{
  modelValue: Metadata
  id: string
  label: string
  sectionName: MetadataSectionName
}>()

const emit = defineEmits<{
  "update:modelValue": [value: Metadata]
}>()

const date = computed({
  get: () => props.modelValue.DATE?.[0],
  set: (data) => {
    const next = produce(props.modelValue, (draft) => {
      draft.DATE = data ? [data] : undefined
    })
    emit("update:modelValue", next)
  },
})

const normCategory = computed({
  get: () => props.modelValue.NORM_CATEGORY ?? [],
  set: (data) => {
    const next = produce(props.modelValue, (draft) => {
      draft.NORM_CATEGORY = data
    })
    emit("update:modelValue", next)
  },
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
      v-model="normCategory"
      :section-name="sectionName"
    />
  </div>
</template>
