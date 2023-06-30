<script lang="ts" setup>
import { watch } from "vue"
import DateOrYearInputGroup from "@/components/DateOrYearInputGroup.vue"
import { Metadata } from "@/domain/Norm"

interface Props {
  modelValue: Metadata
}

interface Emits {
  (event: "update:modelValue", value: Metadata): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

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
