<script setup lang="ts">
import type { Component } from "vue"
import PendingProceedingCategories from "@/components/PendingProceedingCategories.vue"

defineProps<{
  registerTextEditorRef: (key: string, el: Component | null) => void
}>()
</script>

<template>
  <PendingProceedingCategories v-bind="{ registerTextEditorRef }" />
</template>
