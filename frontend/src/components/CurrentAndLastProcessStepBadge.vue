<script setup lang="ts" generic="TDocument">
import { computed } from "vue"
import IconBadge from "@/components/IconBadge.vue"
import { useProcessStepBadge } from "@/composables/useProcessStepBadge"
import DocumentationUnitProcessStep from "@/domain/documentationUnitProcessStep"

const props = defineProps<{
  processSteps: DocumentationUnitProcessStep[] | undefined
}>()

const currentProcessStep = computed(
  () => props.processSteps?.at(0)?.processStep,
)

const lastProcessStep = computed(() => {
  if (!props.processSteps || props.processSteps.length < 2) {
    return null
  }

  const currentId = currentProcessStep.value?.uuid
  // Find the first item after the current one where the processStep ID is different
  const lastStep = props.processSteps
    .slice(1)
    .find((step) => step.processStep.uuid !== currentId)
  return lastStep ? lastStep.processStep : null
})

const currentProcessBadge = computed(() =>
  currentProcessStep.value
    ? useProcessStepBadge(currentProcessStep.value).value
    : null,
)
const lastProcessBadge = computed(() =>
  lastProcessStep.value
    ? useProcessStepBadge(lastProcessStep.value).value
    : null,
)
</script>

<template>
  <div class="flex flex-row">
    <IconBadge
      v-if="lastProcessStep && lastProcessBadge"
      v-bind="lastProcessBadge"
      :label="lastProcessStep.abbreviation"
      text-color="text-gray-900"
    />
    <IconBadge
      v-if="currentProcessStep && currentProcessBadge"
      v-bind="currentProcessBadge"
      :class="{ 'ml-[-5px]': lastProcessBadge }"
    />
  </div>
</template>
