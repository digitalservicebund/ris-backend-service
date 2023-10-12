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
    class="ds-label-03-reg flex w-[fit-content] items-center rounded-md border-1 border-black bg-white px-8 py-2"
    :style="{ color }"
  >
    <!-- TODO: render dynamic vue icon component based on props -->
    <span class="material-icons mr-8">{{ icon }}</span
    >{{ value }}
  </div>
</template>
