<script lang="ts" setup>
import DecisionSummary from "@/components/DecisionSummary.vue"
import { DisplayMode } from "@/components/enumDisplayMode"
import FlexContainer from "@/components/FlexContainer.vue"
import Reference from "@/domain/reference"

const props = defineProps<{
  data: Reference
}>()
</script>

<template>
  <div class="flex w-full justify-between" data-testid="reference-list-summary">
    <div class="flex flex-col">
      <FlexContainer flex-direction="flex-row">
        <DecisionSummary
          :decision="props.data.documentationUnit"
          :display-mode="DisplayMode.SIDEPANEL"
        />
      </FlexContainer>

      <div class="ds-label-01-reg mr-8" data-testid="citation-summary">
        {{ props.data.renderDecision }}
      </div>
    </div>
  </div>
</template>
