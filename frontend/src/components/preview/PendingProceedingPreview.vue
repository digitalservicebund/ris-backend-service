<script setup lang="ts">
import dayjs from "dayjs"
import { storeToRefs } from "pinia"
import { provide, Ref } from "vue"
import FlexContainer from "@/components/FlexContainer.vue"
import TextEditor from "@/components/input/TextEditor.vue"
import {
  PreviewLayout,
  previewLayoutInjectionKey,
} from "@/components/preview/constants"
import PreviewCaselawReferences from "@/components/preview/PreviewCaselawReferences.vue"
import PreviewCategory from "@/components/preview/PreviewCategory.vue"
import PreviewContent from "@/components/preview/PreviewContent.vue"
import PreviewContentRelatedIndexing from "@/components/preview/PreviewContentRelatedIndexing.vue"
import PreviewCoreData from "@/components/preview/PreviewCoreData.vue"
import PreviewLiteratureReferences from "@/components/preview/PreviewLiteratureReferences.vue"
import PreviewProceedingDecisions from "@/components/preview/PreviewProceedingDecisions.vue"
import PreviewRow from "@/components/preview/PreviewRow.vue"
import PreviewShortTexts from "@/components/preview/PreviewShortTexts.vue"
import { Kind } from "@/domain/documentUnit"
import PendingProceeding, {
  pendingProceedingShortTextLabels,
} from "@/domain/pendingProceeding"
import Reference from "@/domain/reference"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const props = defineProps<{
  layout?: PreviewLayout
  documentNumber: string
}>()

const { documentUnit } = storeToRefs(useDocumentUnitStore()) as {
  documentUnit: Ref<PendingProceeding | undefined>
}

provide(previewLayoutInjectionKey, props.layout || "wide")
</script>

<template>
  <FlexContainer
    v-if="documentUnit"
    class="max-w-screen-xl"
    data-testid="preview"
    flex-direction="flex-col"
  >
    <h1 class="ris-heading3-bold mt-16 px-16">
      {{ documentUnit.documentNumber }}
    </h1>
    <p class="ris-label3-regular mb-16 px-16">
      Vorschau erstellt am {{ dayjs(new Date()).format("DD.MM.YYYY") }} um
      {{ dayjs(new Date()).format("HH:mm:ss") }}
    </p>
    <PreviewCoreData
      :core-data="documentUnit.coreData"
      date-label="Mitteilungsdatum"
      is-pending-proceeding
      :kind="Kind.PENDING_PROCEEDING"
    />
    <PreviewCaselawReferences
      :caselaw-references="documentUnit.caselawReferences as Reference[]"
    />
    <PreviewLiteratureReferences
      :literature-references="documentUnit.literatureReferences as Reference[]"
    />
    <PreviewProceedingDecisions
      :ensuing-decisions="documentUnit.ensuingDecisions"
      :previous-decisions="documentUnit.previousDecisions"
    />
    <PreviewContentRelatedIndexing
      :content-related-indexing="documentUnit.contentRelatedIndexing"
    />
    <PreviewShortTexts
      :short-texts="documentUnit.shortTexts"
      :valid-border-numbers="[]"
    />
    <PreviewRow v-if="documentUnit.legalIssue">
      <PreviewCategory>{{
        pendingProceedingShortTextLabels.legalIssue
      }}</PreviewCategory>
      <PreviewContent>
        <TextEditor
          id="previewLegalIssue"
          :aria-label="pendingProceedingShortTextLabels.legalIssue"
          field-size="max"
          preview
          :value="documentUnit.legalIssue"
        />
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="documentUnit.admissionOfAppeal">
      <PreviewCategory>{{
        pendingProceedingShortTextLabels.admissionOfAppeal
      }}</PreviewCategory>
      <PreviewContent>
        {{ documentUnit.admissionOfAppeal }}
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="documentUnit.appellant">
      <PreviewCategory>{{
        pendingProceedingShortTextLabels.appellant
      }}</PreviewCategory>
      <PreviewContent>
        {{ documentUnit.appellant }}
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="documentUnit.resolutionNote">
      <PreviewCategory>Erledigungsvermerk</PreviewCategory>
      <PreviewContent>
        <TextEditor
          id="previewResolutionNote"
          :aria-label="pendingProceedingShortTextLabels.resolutionNote"
          field-size="max"
          preview
          :value="documentUnit.resolutionNote"
        />
      </PreviewContent>
    </PreviewRow>
  </FlexContainer>
</template>
