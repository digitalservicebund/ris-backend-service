<script setup lang="ts">
import { inject } from "vue"
import { previewLayoutInjectionKey } from "@/components/preview/constants"

const layout = inject(previewLayoutInjectionKey) || "wide"
</script>

<template>
  <div class="flex pb-8" :class="{ 'flex-col': layout === 'narrow' }">
    <slot></slot>
  </div>
</template>
