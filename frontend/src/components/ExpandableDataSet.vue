<script lang="ts" setup>
import type { Component } from "vue"
import { ref } from "vue"
import DataSetSummary from "@/components/DataSetSummary.vue"
import ExpandableContent from "@/components/ExpandableContent.vue"
import IconExpandLess from "~icons/ic/baseline-expand-less"
import IconExpandMore from "~icons/ic/baseline-expand-more"

interface Props {
  title: string
  dataSet: any // eslint-disable-line @typescript-eslint/no-explicit-any
  summaryComponent?: Component
}

withDefaults(defineProps<Props>(), {
  summaryComponent: DataSetSummary,
  borderBottom: false,
})

const isExpanded = ref(false)
</script>

<template>
  <ExpandableContent v-model:is-expanded="isExpanded" class="bg-white">
    <template #open-icon>
      <IconExpandMore />
    </template>

    <template #close-icon>
      <IconExpandLess />
    </template>

    <template #header>
      <div id="expandableHeader" class="flex w-full flex-col">
        <h2 class="ds-label-01-bold">
          {{ title }}
        </h2>
        <Component :is="summaryComponent" v-if="!isExpanded" :data="dataSet" />
      </div>
    </template>

    <div class="mt-16 flex flex-col items-start gap-24">
      <slot />
    </div>
  </ExpandableContent>
</template>
