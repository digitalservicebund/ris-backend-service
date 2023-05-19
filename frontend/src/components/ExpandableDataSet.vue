<script lang="ts" setup>
import { computed, ref } from "vue"
import type { Component } from "vue"
import ExpandableContent from "@/components/ExpandableContent.vue"
import DataSetSummary from "@/shared/components/DataSetSummary.vue"
import TextButton from "@/shared/components/input/TextButton.vue"

interface Props {
  title: string
  dataSet: any // eslint-disable-line @typescript-eslint/no-explicit-any
  summaryComponent?: Component
  asColumn?: boolean
  fallbackText?: string
  borderBottom?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  summaryComponent: DataSetSummary,
  asColumn: false,
  fallbackText: undefined,
  borderBottom: false,
})

const isExpanded = ref(false)

function collapse(): void {
  isExpanded.value = false
}

const column = computed(() => ({
  "flex-col": props.asColumn,
}))

const padding = computed(() => ({
  "mb-[1.65rem]": props.asColumn,
}))
</script>

<template>
  <ExpandableContent
    v-model:is-expanded="isExpanded"
    class="bg-white p-16 pt-20"
    :class="[
      borderBottom && 'border-b border-gray-400',
      { 'hover:bg-blue-300': !isExpanded },
    ]"
    close-icon-name="expand_less"
    open-icon-name="expand_more"
  >
    <template #header>
      <div class="flex w-full" :class="column">
        <div class="min-w-[15rem]">
          <h2 class="label-02-bold mb-24 text-left" :class="padding">
            {{ title }}
          </h2>
        </div>
        <span
          v-if="dataSet?.length === 0 && fallbackText !== undefined"
          class="label-02-reg text-start"
          >{{ fallbackText }}
        </span>
        <Component :is="summaryComponent" v-if="!isExpanded" :data="dataSet" />
      </div>
    </template>

    <div class="flex flex-col gap-32 items-start">
      <slot />
      <TextButton aria-label="Fertig" label="Fertig" @click="collapse" />
    </div>
  </ExpandableContent>
</template>
