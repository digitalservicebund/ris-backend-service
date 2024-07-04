<script setup lang="ts">
import { inject } from "vue"
import FlexContainer from "@/components/FlexContainer.vue"
import { previewLayoutInjectionKey } from "@/components/preview/constants"

const layout = inject(previewLayoutInjectionKey) || "wide"
</script>

<template>
  <FlexContainer :class="{ 'flex-col': layout === 'narrow' }">
    <slot></slot>
  </FlexContainer>
</template>
