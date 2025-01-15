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
    class="ds-label-02-reg w-[fit-content] rounded-full px-4 py-2"
    :class="backgroundColor"
  >
    <component :is="icon" class="inline-block" :class="color" />
    <span class="mx-2 align-middle"> {{ label }}</span>
  </div>
</template>
