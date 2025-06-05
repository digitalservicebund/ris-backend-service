<script lang="ts" setup>
import type { Component } from "vue"
import PendingProceedingShortTexts from "@/components/texts/PendingProceedingShortTexts.vue"

defineProps<{
  registerTextEditorRef: (key: string, el: Component | null) => void
}>()
</script>

<template>
  <div class="flex flex-col gap-24 bg-white p-24">
    <PendingProceedingShortTexts v-bind="{ registerTextEditorRef }" />
  </div>
</template>
