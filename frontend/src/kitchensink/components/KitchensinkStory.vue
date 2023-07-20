<script setup lang="ts">
defineProps<{
  name: string
}>()
</script>

<template>
  <div>
    <h3 class="ds-heading-03-bold mb-24">{{ name }}</h3>
    <slot />
  </div>
</template>
