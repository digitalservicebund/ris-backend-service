<script lang="ts" setup>
import { computed } from "vue"
import DecisionSummary from "@/components/DecisionSummary.vue"
import IconBadge from "@/components/IconBadge.vue"
import ActiveCitation from "@/domain/activeCitation"
import IconBaselineContentCopy from "~icons/ic/baseline-content-copy"
import IconBaselineDescription from "~icons/ic/baseline-description"
import IconError from "~icons/ic/baseline-error"
import IconOutlineDescription from "~icons/ic/outline-description"

const props = defineProps<{
  data: ActiveCitation
}>()

const iconComponent = computed(() => {
  return props.data?.hasForeignSource
    ? IconBaselineDescription
    : IconOutlineDescription
})

const showErrorBadge = computed(() => {
  return props.data?.hasForeignSource
    ? !props.data.citationTypeIsSet
    : props.data?.hasMissingRequiredFields
})

function copyActiveCitationSummary() {
  if (props.data) navigator.clipboard.writeText(props.data.renderDecision)
}
</script>

<template>
  <div class="flex w-full justify-between">
    <div class="flex flex-row items-center">
      <component :is="iconComponent" class="mr-8" />
      <div v-if="data?.hasForeignSource" class="flex flex-row items-baseline">
        <div v-if="data?.hasForeignSource" class="flex flex-row items-baseline">
          <DecisionSummary class="mr-8" :decision="data"></DecisionSummary>
        </div>
      </div>
      <div v-else class="ds-label-01-reg mr-8">{{ data?.renderDecision }}</div>
      <IconBadge
        v-if="showErrorBadge"
        background-color="bg-red-300"
        color="text-red-900"
        :icon="IconError"
        label="Fehlende Daten"
      />
    </div>
    <button
      class="flex h-32 w-32 items-center justify-center text-blue-800 hover:bg-blue-100 focus:shadow-[inset_0_0_0_0.125rem] focus:shadow-blue-800 focus:outline-none"
      data-testid="copySummary"
      @click="copyActiveCitationSummary"
      @keypress.enter="copyActiveCitationSummary"
    >
      <IconBaselineContentCopy />
    </button>
  </div>
</template>
