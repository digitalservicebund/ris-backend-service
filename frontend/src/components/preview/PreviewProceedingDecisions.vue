<script setup lang="ts">
import { computed } from "vue"
import FlexContainer from "@/components/FlexContainer.vue"
import PreviewCategory from "@/components/preview/PreviewCategory.vue"
import PreviewContent from "@/components/preview/PreviewContent.vue"
import PreviewRow from "@/components/preview/PreviewRow.vue"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const store = useDocumentUnitStore()

const previousDecisions = computed({
  get: () => store.documentUnit!.previousDecisions,
  set: (newValues) => {
    store.documentUnit!.previousDecisions = newValues
  },
})

const ensuingDecisions = computed({
  get: () => store.documentUnit!.ensuingDecisions,
  set: (newValues) => {
    store.documentUnit!.ensuingDecisions = newValues
  },
})
</script>

<template>
  <FlexContainer flex-direction="flex-col">
    <PreviewRow v-if="previousDecisions && previousDecisions?.length > 0">
      <PreviewCategory>Vorinstanz</PreviewCategory>
      <PreviewContent>
        <div
          v-for="previousDecision in previousDecisions"
          :key="previousDecision.id"
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
          :key="ensuingDecision.id"
        >
          {{ ensuingDecision.renderSummary }}
        </div>
      </PreviewContent>
    </PreviewRow>
  </FlexContainer>
</template>
