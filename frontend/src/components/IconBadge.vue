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
    class="ds-label-02-reg flex w-[fit-content] rounded-full py-2 pe-8 ps-4"
    :class="backgroundColor"
  >
    <component :is="icon" :class="color" />
    <span class="mx-2"> {{ label }}</span>
  </div>
</template>
