<script setup lang="ts">
import { type Component } from "vue"

defineProps<{
  value?: string
  icon?: Component
  color: string
}>()
</script>

<script lang="ts">
export interface IconBadgeProps {
  label: string
  value: string
  icon: Component
  color: string
}
</script>

<template>
  <div
    class="ds-label-03-reg flex w-[fit-content] items-center rounded-md border-1 border-black bg-white px-8 py-2"
    :style="{ color }"
  >
    <component :is="icon" class="mr-8" />
    {{ value }}
  </div>
</template>
