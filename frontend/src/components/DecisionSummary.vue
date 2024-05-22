<script setup lang="ts">
import DocumentNumberPreviewLink from "@/components/DocumentNumberLink.vue"
import FlexContainer from "@/components/FlexContainer.vue"
import RelatedDocumentation from "@/domain/relatedDocumentation"

interface Props {
  decision: RelatedDocumentation
}

const props = defineProps<Props>()
</script>

<template>
  <FlexContainer flex-direction="flex-row">
    {{ decision.renderDecisionWithoutDocumentNumber }}
    <DocumentNumberPreviewLink :decision="props.decision" />
  </FlexContainer>
</template>
