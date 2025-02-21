<script setup lang="ts">
import type { Component } from "vue"
import DocumentUnitCategories from "@/components/DocumentUnitCategories.vue"

const props = defineProps<{
  registerTextEditorRef: (key: string, el: Component | null) => void
}>()
</script>

<template>
  <DocumentUnitCategories v-bind="{ registerTextEditorRef }" />
</template>
