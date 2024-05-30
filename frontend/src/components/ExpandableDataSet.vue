<script lang="ts" setup>
import type { Component } from "vue"
import { ref } from "vue"
import DataSetSummary from "@/components/DataSetSummary.vue"
import ExpandableContent from "@/components/ExpandableContent.vue"
import TextButton from "@/components/input/TextButton.vue"
import values from "@/data/values.json"
import IconExpandLess from "~icons/ic/baseline-expand-less"
import IconExpandMore from "~icons/ic/baseline-expand-more"

interface Props {
  title: string
  dataSet: any // eslint-disable-line @typescript-eslint/no-explicit-any
  summaryComponent?: Component
  fallbackText?: string
  borderBottom?: boolean
}

withDefaults(defineProps<Props>(), {
  summaryComponent: DataSetSummary,
  fallbackText: undefined,
  borderBottom: false,
})

const isExpanded = ref(false)

function collapse(): void {
  isExpanded.value = false
  scrollToTop()
}

function scrollToTop() {
  const element = document.getElementById("expandableHeader")
  if (element) {
    const headerOffset = values.headerOffset
    const elementPosition = element?.getBoundingClientRect().top
    const offsetPosition = elementPosition + window.scrollY - headerOffset

    window.scrollTo({
      top: offsetPosition,
      behavior: "smooth",
    })
  }
}
</script>

<template>
  <ExpandableContent
    v-model:is-expanded="isExpanded"
    class="border-gray-400 bg-white p-32"
    :class="{
      'hover:border-blue-500 hover:bg-blue-200': !isExpanded,
      'border-b': borderBottom,
    }"
  >
    <template #open-icon>
      <IconExpandMore />
    </template>

    <template #close-icon>
      <IconExpandLess />
    </template>

    <template #header>
      <div id="expandableHeader" class="flex w-full flex-col gap-24">
        <h2 class="ds-heading-03-reg">
          {{ title }}
        </h2>
        <span
          v-if="dataSet?.length === 0 && fallbackText !== undefined"
          class="ds-label-02-reg text-start"
          >{{ fallbackText }}</span
        >
        <Component :is="summaryComponent" v-if="!isExpanded" :data="dataSet" />
      </div>
    </template>

    <div
      class="flex flex-col items-start gap-32"
      :class="{ 'mt-24': isExpanded }"
    >
      <slot />
      <TextButton aria-label="Fertig" label="Fertig" @click="collapse" />
    </div>
  </ExpandableContent>
</template>
