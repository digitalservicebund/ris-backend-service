<script setup lang="ts">
import dayjs from "dayjs"
import FlexContainer from "@/components/FlexContainer.vue"
import PreviewContentRelatedIndexing from "@/components/preview/PreviewContentRelatedIndexing.vue"
import PreviewCoreData from "@/components/preview/PreviewCoreData.vue"
import PreviewProceedingDecisions from "@/components/preview/PreviewProceedingDecisions.vue"
import PreviewTexts from "@/components/preview/PreviewTexts.vue"
import DocumentUnit from "@/domain/documentUnit"

defineProps<{
  documentUnit: DocumentUnit
}>()
</script>

<template>
  <FlexContainer class="max-w-screen-xl flex-col">
    <h1 class="ds-heading-03-bold mt-16 px-16">
      {{ documentUnit.documentNumber }}
    </h1>
    <p class="ds-label-03-reg mb-16 px-16">
      Vorschau erstellt am {{ dayjs(new Date()).format("DD.MM.YYYY") }} um
      {{ dayjs(new Date()).format("HH:mm:ss") }}
    </p>
    <PreviewCoreData :core-data="documentUnit.coreData" />
    <PreviewProceedingDecisions
      :ensuing-decisions="documentUnit.ensuingDecisions"
      :previous-decisions="documentUnit.previousDecisions"
    />
    <PreviewContentRelatedIndexing
      :content-related-indexing="documentUnit.contentRelatedIndexing"
    />
    <PreviewTexts
      :texts="documentUnit.texts"
      :valid-border-numbers="documentUnit.borderNumbers"
    />
  </FlexContainer>
</template>
