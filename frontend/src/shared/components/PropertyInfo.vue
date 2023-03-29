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
    <div class="label-03-caps pr-[0.4rem]">{{ label }}</div>
    <div class="label-03-bold">{{ noValue ? " - " : value }}</div>
  </div>
</template>
