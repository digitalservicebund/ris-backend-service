<script lang="ts" setup>
import { computed } from "vue"
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
        <!-- Use DecisionSummary here? right now, not possible because the renderDecision method of the relatedDocumentation is missing the citationType -->
        <div class="ds-label-01-reg">{{ data?.renderDecision }}</div>
        <span class="ds-label-01-reg ml-8">|</span>
        <RouterLink
          class="ds-link-03-bold ml-8 mr-8 flex flex-row border-b-2 border-blue-800 leading-24 focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
          tabindex="0"
          target="_blank"
          :to="{
            name: 'caselaw-documentUnit-documentNumber-preview',
            params: { documentNumber: data?.documentNumber },
          }"
        >
          <div class="flex flex-row items-center" @click.stop>
            {{ data?.documentNumber }}
            <BaselineArrowOutward />
          </div>
        </RouterLink>
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
