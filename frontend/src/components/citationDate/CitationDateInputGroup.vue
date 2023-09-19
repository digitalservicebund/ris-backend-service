<script lang="ts" setup>
import { computed } from "vue"
import DateOrYearInputGroup from "@/components/DateOrYearInputGroup.vue"
import { Metadata, MetadatumType } from "@/domain/norm"

const props = defineProps<{
  modelValue: Metadata
}>()

const emit = defineEmits<{
  "update:modelValue": [value: Metadata]
}>()

/* -------------------------------------------------- *
 * Section type                                       *
 * -------------------------------------------------- */

const initialValue: Metadata = {
  YEAR: props.modelValue.YEAR,
  DATE: props.modelValue.DATE,
}

const selectedInputType = computed<MetadatumType.YEAR | MetadatumType.DATE>({
  get() {
    if (props.modelValue.DATE) {
      return MetadatumType.DATE
    } else if (props.modelValue.YEAR) {
      return MetadatumType.YEAR
    } else {
      return MetadatumType.DATE
    }
  },
  set(value) {
    emit("update:modelValue", { [value]: initialValue[value] ?? [] })
  },
})

/* -------------------------------------------------- *
 * Section data                                       *
 * -------------------------------------------------- */

const dateSection = computed({
  get: () => props.modelValue.DATE?.[0],
  set: (data) => {
    const effectiveData = data ? [data] : undefined
    initialValue.DATE = effectiveData

    const next: Metadata = { DATE: effectiveData }
    emit("update:modelValue", next)
  },
})

const yearSection = computed({
  get: () => props.modelValue.YEAR?.[0],
  set: (data) => {
    const effectiveData = data ? [data] : []
    initialValue.YEAR = effectiveData

    const next: Metadata = { YEAR: effectiveData }
    emit("update:modelValue", next)
  },
})
</script>

<template>
  <DateOrYearInputGroup
    v-model:date-value="dateSection"
    v-model:selected-input-type="selectedInputType"
    v-model:year-value="yearSection"
    id-prefix="citation"
    label="Zitierdatum"
  />
</template>
