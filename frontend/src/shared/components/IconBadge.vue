<script setup lang="ts">
defineProps<{
  value?: string
  icon: string
  color: string
}>()
</script>

<script lang="ts">
export interface IconBadgeProps {
  label: string
  value: string
  icon: string
  color: string
}
</script>

<template>
  <div
    class="bg-white border-1 flex items-center label-03-reg px-8 py-2 rounded-md w-[fit-content]"
    :style="{ color }"
  >
    <span class="material-icons mr-8">{{ icon }}</span
    >{{ value }}
  </div>
</template>
