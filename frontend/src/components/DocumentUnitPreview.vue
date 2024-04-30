<script setup lang="ts">
import dayjs from "dayjs"
import { computed } from "vue"
import DocumentUnitWrapper from "@/components/DocumentUnitWrapper.vue"
import PreviewContentRelatedIndexing from "@/components/preview/PreviewContentRelatedIndexing.vue"
import PreviewCoreData from "@/components/preview/PreviewCoreData.vue"
import PreviewProceedingDecisions from "@/components/preview/PreviewProceedingDecisions.vue"
import PreviewTexts from "@/components/preview/PreviewTexts.vue"
import DocumentUnit from "@/domain/documentUnit"

const props = defineProps<{
  documentUnit: DocumentUnit
  showAttachmentPanel?: boolean
  showNavigationPanel: boolean
}>()

const hasProceedingDecisions = computed(() => {
  return (
    (props.documentUnit.ensuingDecisions &&
      props.documentUnit.ensuingDecisions.length > 0) ||
    (props.documentUnit.previousDecisions &&
      props.documentUnit.previousDecisions.length > 0)
  )
})

const hasContentRelatedIndexing = computed(() => {
  return (
    (props.documentUnit.contentRelatedIndexing.keywords &&
      props.documentUnit.contentRelatedIndexing.keywords?.length > 0) ||
    (props.documentUnit.contentRelatedIndexing.activeCitations &&
      props.documentUnit.contentRelatedIndexing.activeCitations.length > 0) ||
    (props.documentUnit.contentRelatedIndexing.fieldsOfLaw &&
      props.documentUnit.contentRelatedIndexing.fieldsOfLaw.length > 0) ||
    (props.documentUnit.contentRelatedIndexing.norms &&
      props.documentUnit.contentRelatedIndexing.norms.length > 0)
  )
})
const hasTexts = computed(() => {
  return (
    props.documentUnit.texts.caseFacts ||
    props.documentUnit.texts.decisionName ||
    props.documentUnit.texts.decisionReasons ||
    props.documentUnit.texts.guidingPrinciple ||
    props.documentUnit.texts.headline ||
    props.documentUnit.texts.headnote ||
    props.documentUnit.texts.reasons ||
    props.documentUnit.texts.tenor
  )
})
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
        v-if="hasProceedingDecisions"
        :ensuing-decisions="documentUnit.ensuingDecisions"
        :previous-decisions="documentUnit.previousDecisions"
      />
      <PreviewContentRelatedIndexing
        v-if="hasContentRelatedIndexing"
        :content-related-indexing="documentUnit.contentRelatedIndexing"
      />
      <PreviewTexts
        v-if="hasTexts"
        :texts="documentUnit.texts"
        :valid-border-numbers="documentUnit.borderNumbers"
      />
    </div>
  </DocumentUnitWrapper>
</template>
