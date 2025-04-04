<script setup lang="ts">
import { type Component } from "vue"

defineProps<{
  label: string
  icon?: Component
  color: string
  backgroundColor: string
  marginX?: number
}>()
</script>

<template>
  <div
    class="ris-label2-regular w-[fit-content] rounded-full px-4 py-2"
    :class="backgroundColor"
  >
    <component :is="icon" class="inline-block" :class="color" />
    <span class="align-middle" :class="marginX ? `mx-${marginX}` : 'mx-2'">
      {{ label }}
    </span>
  </div>
</template>
