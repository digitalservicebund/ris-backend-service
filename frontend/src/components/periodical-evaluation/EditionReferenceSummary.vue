<script lang="ts" setup>
import DecisionSummary from "@/components/DecisionSummary.vue"
import { DisplayMode } from "@/components/enumDisplayMode"
import FlexContainer from "@/components/FlexContainer.vue"
import Reference from "@/domain/reference"

const props = defineProps<{
  data: Reference
  hideCitation?: boolean
}>()
</script>

<template>
  <div class="flex w-full justify-between">
    <div class="flex flex-col">
      <RouterLink
        v-if="props.data.documentationUnit?.documentNumber"
        tabindex="-1"
        target="_blank"
        :to="{
          name: 'caselaw-documentUnit-documentNumber-preview',
          params: {
            documentNumber: props.data.documentationUnit?.documentNumber,
          },
        }"
      >
        <FlexContainer flex-direction="flex-row">
          <DecisionSummary
            :decision="props.data.documentationUnit"
            :display-mode="DisplayMode.SIDEPANEL"
          />
        </FlexContainer>
      </RouterLink>

      <div
        v-if="!hideCitation"
        class="ds-label-01-reg mr-8"
        data-testid="citation-summary"
      >
        {{ props.data.renderDecision }}
      </div>
    </div>
  </div>
</template>
