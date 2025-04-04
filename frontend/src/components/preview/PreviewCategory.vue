<script setup lang="ts">
import { inject } from "vue"
import FlexItem from "@/components/FlexItem.vue"
import { previewLayoutInjectionKey } from "@/components/preview/constants"

const layout = inject(previewLayoutInjectionKey) || "wide"
</script>
<template>
  <FlexItem
    class="ris-body1-regular min-w-[213px] px-16 text-gray-900"
    :class="{
      'w-min py-4': layout === 'wide',
    }"
  >
    <slot></slot>
  </FlexItem>
</template>
