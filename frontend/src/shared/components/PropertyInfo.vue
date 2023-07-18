<script lang="ts" setup>
import { computed } from "vue"

interface Props {
  label: string
  value?: string
  direction?: "row" | "column"
}

const props = defineProps<Props>()

const noValue = computed(() => {
  return props.value === undefined || props.value === ""
})

const classes = computed(() => ({
  "flex-row": props.direction == "row",
  "flex-col": props.direction == "column",
}))
</script>

<template>
  <div class="flex items-baseline" :class="classes">
    <div class="ds-label-03-reg pr-[0.4rem] uppercase">{{ label }}</div>
    <div class="ds-label-01-bold">{{ noValue ? " - " : value }}</div>
  </div>
</template>
