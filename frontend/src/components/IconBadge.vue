<script setup lang="ts">
import { type Component, computed } from "vue"

const props = defineProps<{
  label: string
  icon?: Component
  iconColor?: string
  textColor?: string
  backgroundColor?: string
  borderColor?: string
  marginX?: number
}>()

const borderClass = computed(() => ({
  border: props.borderColor,
}))
</script>

<template>
  <div
    class="ris-label2-regular flex w-[fit-content] rounded-full px-4 py-2"
    :class="[backgroundColor, borderColor, borderClass]"
  >
    <component :is="icon" class="inline-block" :class="[iconColor]" />
    <span class="mx-2 align-middle" :class="[textColor]">
      {{ label }}
    </span>
  </div>
</template>
