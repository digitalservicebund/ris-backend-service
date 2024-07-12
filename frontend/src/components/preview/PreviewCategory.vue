<script setup lang="ts">
import { inject } from "vue"
import FlexItem from "@/components/FlexItem.vue"
import { previewLayoutInjectionKey } from "@/components/preview/constants"

const layout = inject(previewLayoutInjectionKey) || "wide"
</script>
<template>
  <FlexItem
    class="ds-body-01-reg min-w-[213px] px-16 text-gray-900"
    :class="{
      'w-min py-4': layout === 'wide',
      'mb-[-2px] pt-4': layout === 'narrow',
    }"
  >
    <slot></slot>
  </FlexItem>
</template>
