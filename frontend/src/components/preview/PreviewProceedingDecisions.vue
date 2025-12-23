<script setup lang="ts">
import FlexContainer from "@/components/FlexContainer.vue"
import PreviewCategory from "@/components/preview/PreviewCategory.vue"
import PreviewContent from "@/components/preview/PreviewContent.vue"
import PreviewRow from "@/components/preview/PreviewRow.vue"
import EnsuingDecision from "@/domain/ensuingDecision"
import PreviousDecision from "@/domain/previousDecision"

defineProps<{
  previousDecisions: PreviousDecision[] | undefined
  ensuingDecisions: EnsuingDecision[] | undefined
}>()
</script>

<template>
  <FlexContainer flex-direction="flex-col">
    <PreviewRow v-if="previousDecisions && previousDecisions?.length > 0">
      <PreviewCategory>Vorinstanz</PreviewCategory>
      <PreviewContent>
        <div
          v-for="previousDecision in previousDecisions"
          :key="previousDecision.uuid"
        >
          {{ previousDecision.renderSummary }}
        </div>
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="ensuingDecisions && ensuingDecisions?.length > 0">
      <PreviewCategory>Nachgehende Entscheidungen</PreviewCategory>
      <PreviewContent>
        <div
          v-for="ensuingDecision in ensuingDecisions"
          :key="ensuingDecision.uuid"
        >
          {{ ensuingDecision.renderSummary }}
        </div>
      </PreviewContent>
    </PreviewRow>
  </FlexContainer>
</template>
