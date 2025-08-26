<script setup lang="ts" generic="TDocument">
import { computed } from "vue"
import IconBadge from "@/components/IconBadge.vue"
import { useProcessStepBadge } from "@/composables/useProcessStepBadge"
import ProcessStep from "@/domain/processStep"

const props = defineProps<{
  currentProcessStep?: ProcessStep
  previousProcessStep?: ProcessStep
}>()

const currentProcessBadge = computed(() =>
  props.currentProcessStep
    ? useProcessStepBadge(props.currentProcessStep).value
    : null,
)
const previousProcessBadge = computed(() =>
  props.previousProcessStep
    ? useProcessStepBadge(props.previousProcessStep).value
    : null,
)
</script>

<template>
  <div class="flex flex-row">
    <IconBadge
      v-if="props.previousProcessStep"
      v-bind="previousProcessBadge"
      :label="props.previousProcessStep.abbreviation"
      text-color="text-gray-900"
    />
    <IconBadge
      v-if="props.currentProcessStep"
      v-bind="currentProcessBadge"
      :class="{ 'ml-[-5px]': previousProcessBadge }"
      :label="props.currentProcessStep.name"
    />
  </div>
</template>
