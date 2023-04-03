<script lang="ts" setup>
import { ref } from "vue"
import type { Component } from "vue"
import ExpandableContent from "@/components/ExpandableContent.vue"
import DataSetSummary from "@/shared/components/DataSetSummary.vue"
import TextButton from "@/shared/components/input/TextButton.vue"

interface Props {
  title: string
  dataSet: any // eslint-disable-line @typescript-eslint/no-explicit-any
  summaryComponent?: Component
}

withDefaults(defineProps<Props>(), {
  summaryComponent: DataSetSummary,
})

const isExpanded = ref(false)

function collapse(): void {
  isExpanded.value = false
}
</script>

<template>
  <ExpandableContent
    v-model:is-expanded="isExpanded"
    class="p-16"
    close-icon-name="expand_less"
    open-icon-name="expand_more"
  >
    <template #header>
      <div class="flex w-full">
        <h2 class="label-02-bold min-w-[20rem] text-left">{{ title }}</h2>
        <Component :is="summaryComponent" v-if="!isExpanded" :data="dataSet" />
      </div>
    </template>

    <div class="flex flex-col gap-32 items-start mt-24">
      <slot />
      <TextButton aria-label="Fertig" label="Fertig" @click="collapse" />
    </div>
  </ExpandableContent>
</template>
