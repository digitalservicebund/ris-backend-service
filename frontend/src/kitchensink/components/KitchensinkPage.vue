<script setup lang="ts">
defineProps<{
  name: string
}>()
</script>

<template>
  <h2 class="ris-heading2-regular capitalize">{{ name }}</h2>
  <hr class="my-24 border-blue-400" />
  <div class="space-y-48">
    <slot />
  </div>
</template>
