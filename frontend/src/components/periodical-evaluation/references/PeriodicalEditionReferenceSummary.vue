<script lang="ts" setup>
import DecisionSummary from "@/components/DecisionSummary.vue"
import { DisplayMode } from "@/components/enumDisplayMode"
import Reference from "@/domain/reference"

const props = defineProps<{
  data: Reference
}>()
</script>

<template>
  <div class="w-full" data-testid="reference-list-summary">
    <div v-if="props.data.documentationUnit?.documentNumber">
      <div class="ds-label-01-bold" data-testid="citation-summary">
        {{ props.data.renderSummary }}
      </div>
      <DecisionSummary
        :display-mode="DisplayMode.SIDEPANEL"
        :document-number="props.data.documentationUnit.documentNumber"
        :link-clickable="props.data.documentationUnit.hasPreviewAccess"
        :status="props.data.documentationUnit.status"
        :summary="props.data.documentationUnit.renderSummary"
      ></DecisionSummary>
    </div>
  </div>
</template>
