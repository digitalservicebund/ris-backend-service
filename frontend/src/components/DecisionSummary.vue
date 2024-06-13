<script setup lang="ts">
import DocumentNumberPreviewLink from "@/components/DocumentNumberLink.vue"
import FlexContainer from "@/components/FlexContainer.vue"
import RelatedDocumentation from "@/domain/relatedDocumentation"

type Props<T extends RelatedDocumentation> = {
  decision: T
}

const props = defineProps<Props<RelatedDocumentation>>()
</script>

<template>
  <FlexContainer
    :data-testid="'decision-summary-' + props.decision.documentNumber"
  >
    {{ decision.renderDecision }}
    <DocumentNumberPreviewLink :decision="props.decision" />
  </FlexContainer>
</template>
