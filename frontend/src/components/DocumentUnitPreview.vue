<script setup lang="ts">
import dayjs from "dayjs"
import DocumentUnitWrapper from "@/components/DocumentUnitWrapper.vue"
import PreviewContentRelatedIndexing from "@/components/preview/PreviewContentRelatedIndexing.vue"
import PreviewCoreData from "@/components/preview/PreviewCoreData.vue"
import PreviewProceedingDecisions from "@/components/preview/PreviewProceedingDecisions.vue"
import PreviewTexts from "@/components/preview/PreviewTexts.vue"
import DocumentUnit from "@/domain/documentUnit"

defineProps<{
  documentUnit: DocumentUnit
  showAttachmentPanel?: boolean
  showNavigationPanel: boolean
}>()
</script>

<template>
  <DocumentUnitWrapper
    :document-unit="documentUnit"
    :show-navigation-panel="showNavigationPanel"
  >
    <div class="bg-white">
      <div class="ds-heading-03-bold mt-16 px-16">
        {{ documentUnit.documentNumber }}
      </div>
      <div
        v-if="documentUnit.coreData.procedure"
        class="ds-label-03-reg px-16 text-gray-900"
      >
        {{ documentUnit.coreData.procedure.label }}
      </div>
      <div class="ds-label-03-reg mb-16 px-16">
        Vorschau erstellt um {{ dayjs(new Date()).format("HH:mm:ss") }}
      </div>
      <PreviewCoreData :core-data="documentUnit.coreData" />
      <PreviewProceedingDecisions
        v-if="
          (documentUnit.ensuingDecisions &&
            documentUnit.ensuingDecisions.length > 0) ||
          (documentUnit.previousDecisions &&
            documentUnit.previousDecisions.length > 0)
        "
        :ensuing-decisions="documentUnit.ensuingDecisions"
        :previous-decisions="documentUnit.previousDecisions"
      />
      <PreviewContentRelatedIndexing
        v-if="
          (documentUnit.contentRelatedIndexing.keywords &&
            documentUnit.contentRelatedIndexing.keywords?.length > 0) ||
          (documentUnit.contentRelatedIndexing.activeCitations &&
            documentUnit.contentRelatedIndexing.activeCitations.length > 0) ||
          (documentUnit.contentRelatedIndexing.fieldsOfLaw &&
            documentUnit.contentRelatedIndexing.fieldsOfLaw.length > 0) ||
          (documentUnit.contentRelatedIndexing.norms &&
            documentUnit.contentRelatedIndexing.norms.length > 0)
        "
        :content-related-indexing="documentUnit.contentRelatedIndexing"
      />
      <PreviewTexts
        v-if="
          documentUnit.texts.caseFacts ||
          documentUnit.texts.decisionName ||
          documentUnit.texts.decisionReasons ||
          documentUnit.texts.guidingPrinciple ||
          documentUnit.texts.headline ||
          documentUnit.texts.headnote ||
          documentUnit.texts.reasons ||
          documentUnit.texts.tenor
        "
        :texts="documentUnit.texts"
        :valid-border-numbers="documentUnit.borderNumbers"
      />
    </div>
  </DocumentUnitWrapper>
</template>
