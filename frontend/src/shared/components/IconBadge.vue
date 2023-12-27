<script setup lang="ts">
import { type Component } from "vue"

defineProps<{
  label: string
  icon?: Component
  color: string
  backgroundColor: string
}>()
</script>

<script lang="ts">
export interface IconBadgeProps {
  label: string
  icon: Component
  backgroundColor: string
  color: string
}
</script>

<template>
  <div
    class="ds-label-03-reg flex w-[fit-content] items-center rounded-full px-6 py-4"
    :class="backgroundColor"
  >
    <component :is="icon" :class="color" />
    <span class="mx-2"> {{ label }}</span>
  </div>
</template>
