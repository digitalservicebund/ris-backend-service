<script setup lang="ts">
import { inject } from "vue"
import FlexItem from "@/components/FlexItem.vue"
import { previewLayoutInjectionKey } from "@/components/preview/constants"

const layout = inject(previewLayoutInjectionKey) || "wide"
</script>
<template>
  <FlexItem
    class="ds-body-01-reg px-16 text-black"
    :class="{
      'py-4': layout === 'wide',
      'mt-[-2px] pb-4': layout === 'narrow',
    }"
  >
    <slot></slot>
  </FlexItem>
</template>
