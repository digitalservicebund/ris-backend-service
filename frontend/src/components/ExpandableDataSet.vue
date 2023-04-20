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
}

const props = withDefaults(defineProps<Props>(), {
  summaryComponent: DataSetSummary,
  asColumn: false,
})

const isExpanded = ref(false)

function collapse(): void {
  isExpanded.value = false
}

const column = computed(() => ({
  "flex-col": props.asColumn,
}))

const padding = computed(() => ({
  "mb-[1.65rem]": props.asColumn && !isExpanded.value,
}))
</script>

<template>
  <ExpandableContent
    v-model:is-expanded="isExpanded"
    class="bg-white p-16 pt-20"
    :class="{ 'hover:bg-blue-300': !isExpanded }"
    close-icon-name="expand_less"
    open-icon-name="expand_more"
  >
    <template #header>
      <div class="flex w-full" :class="column">
        <h2 class="label-02-bold min-w-[14rem] text-left" :class="padding">
          {{ title }}
        </h2>
        <Component :is="summaryComponent" v-if="!isExpanded" :data="dataSet" />
      </div>
    </template>

    <div class="flex flex-col gap-32 items-start mt-24">
      <slot />
      <TextButton aria-label="Fertig" label="Fertig" @click="collapse" />
    </div>
  </ExpandableContent>
</template>
