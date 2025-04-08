<script setup lang="ts">
defineProps<{
  name: string
}>()
</script>

<template>
  <div class="space-y-24">
    <h3 class="ris-heading3-bold mb-24">{{ name }}</h3>
    <slot />
  </div>
</template>
