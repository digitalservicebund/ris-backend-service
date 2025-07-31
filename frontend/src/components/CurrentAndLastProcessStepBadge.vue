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
const lastProcessStep = computed(() => props.processSteps?.at(1)?.processStep)
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
  <div class="ml-12 flex flex-row">
    <IconBadge
      v-if="lastProcessStep && lastProcessBadge"
      :background-color="lastProcessBadge.backgroundColor"
      :border-color="lastProcessBadge?.borderColor"
      class="border px-8"
      color="gray-900"
      :label="lastProcessStep.abbreviation"
    />
    <IconBadge
      v-if="currentProcessStep && currentProcessBadge"
      :background-color="currentProcessBadge.backgroundColor"
      :border-color="currentProcessBadge?.borderColor"
      :class="`border px-8 ${lastProcessBadge ? 'ml-[-5px]' : 'ml-12'}`"
      color="black"
      :label="currentProcessStep.name"
    />
  </div>
</template>
