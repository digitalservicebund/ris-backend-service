<script lang="ts" setup>
import { watch } from "vue"
import DateOrYearInputGroup from "@/components/DateOrYearInputGroup.vue"
import { Metadata } from "@/domain/Norm"

interface Props {
  modelValue: Metadata
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value: Metadata]
}>()

watch(props.modelValue, () => emit("update:modelValue", props.modelValue), {
  deep: true,
})
</script>

<template>
  <DateOrYearInputGroup
    id-prefix="publication"
    label="Veröffentlichungsdatum"
    :model-value="modelValue"
  />
</template>
