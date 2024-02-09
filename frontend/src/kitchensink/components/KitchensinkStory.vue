<script setup lang="ts">
defineProps<{
  name: string
}>()
</script>

<template>
  <div class="space-y-24">
    <h3 class="ds-heading-03-bold mb-24">{{ name }}</h3>
    <slot />
  </div>
</template>
