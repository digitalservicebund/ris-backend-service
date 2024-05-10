<script setup lang="ts">
import FlexContainer from "@/components/FlexContainer.vue"
import PreviewCategory from "@/components/preview/PreviewCategory.vue"
import PreviewContent from "@/components/preview/PreviewContent.vue"
import EnsuingDecision from "@/domain/ensuingDecision"
import PreviousDecision from "@/domain/previousDecision"

defineProps<{
  previousDecisions?: PreviousDecision[]
  ensuingDecisions?: EnsuingDecision[]
}>()
</script>

<template>
  <FlexContainer class="flex-col">
    <FlexContainer
      v-if="previousDecisions && previousDecisions?.length > 0"
      class="flex-row"
    >
      <PreviewCategory>Vorinstanz</PreviewCategory>
      <PreviewContent>
        <div
          v-for="(previousDecision, index) in previousDecisions"
          :key="index"
        >
          {{ previousDecision.renderDecision }}
        </div>
      </PreviewContent>
    </FlexContainer>
    <FlexContainer
      v-if="ensuingDecisions && ensuingDecisions?.length > 0"
      class="flex-row"
    >
      <PreviewCategory>Nachgehende Entscheidungen</PreviewCategory>
      <PreviewContent>
        <div v-for="(ensuingDecision, index) in ensuingDecisions" :key="index">
          {{ ensuingDecision.renderDecision }}
        </div>
      </PreviewContent>
    </FlexContainer>
  </FlexContainer>
</template>
