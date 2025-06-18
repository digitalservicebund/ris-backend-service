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
import { Kind } from "@/domain/documentationUnitKind"
import PendingProceeding, {
  pendingProceedingLabels,
} from "@/domain/pendingProceeding"
import Reference from "@/domain/reference"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const props = defineProps<{
  layout?: PreviewLayout
  documentNumber: string
}>()

const { documentUnit: pendingProceeding } = storeToRefs(
  useDocumentUnitStore(),
) as {
  documentUnit: Ref<PendingProceeding | undefined>
}

provide(previewLayoutInjectionKey, props.layout || "wide")
</script>

<template>
  <FlexContainer
    v-if="pendingProceeding"
    class="max-w-screen-xl"
    data-testid="preview"
    flex-direction="flex-col"
  >
    <h1 class="ris-heading3-bold mt-16 px-16">
      {{ pendingProceeding.documentNumber }}
    </h1>
    <p class="ris-label3-regular mb-16 px-16">
      Vorschau erstellt am {{ dayjs(new Date()).format("DD.MM.YYYY") }} um
      {{ dayjs(new Date()).format("HH:mm:ss") }}
    </p>
    <PreviewCoreData
      :core-data="pendingProceeding.coreData"
      date-label="Mitteilungsdatum"
      is-pending-proceeding
      :kind="Kind.PENDING_PROCEEDING"
    />
    <PreviewCaselawReferences
      :caselaw-references="pendingProceeding.caselawReferences as Reference[]"
    />
    <PreviewLiteratureReferences
      :literature-references="
        pendingProceeding.literatureReferences as Reference[]
      "
    />
    <PreviewProceedingDecisions
      :ensuing-decisions="pendingProceeding.ensuingDecisions"
      :previous-decisions="pendingProceeding.previousDecisions"
    />
    <PreviewContentRelatedIndexing
      :content-related-indexing="pendingProceeding.contentRelatedIndexing"
    />
    <PreviewShortTexts
      :short-texts="pendingProceeding.shortTexts"
      :valid-border-numbers="[]"
    />
    <PreviewRow v-if="pendingProceeding.shortTexts.legalIssue">
      <PreviewCategory>{{
        pendingProceedingLabels.legalIssue
      }}</PreviewCategory>
      <PreviewContent>
        <TextEditor
          id="previewLegalIssue"
          :aria-label="pendingProceedingLabels.legalIssue"
          field-size="max"
          preview
          :value="pendingProceeding.shortTexts.legalIssue"
        />
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="pendingProceeding.shortTexts.admissionOfAppeal">
      <PreviewCategory>{{
        pendingProceedingLabels.admissionOfAppeal
      }}</PreviewCategory>
      <PreviewContent>
        {{ pendingProceeding.shortTexts.admissionOfAppeal }}
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="pendingProceeding.shortTexts.appellant">
      <PreviewCategory>{{ pendingProceedingLabels.appellant }}</PreviewCategory>
      <PreviewContent>
        {{ pendingProceeding.shortTexts.appellant }}
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="pendingProceeding.shortTexts.resolutionNote">
      <PreviewCategory>Erledigungsvermerk</PreviewCategory>
      <PreviewContent>
        <TextEditor
          id="previewResolutionNote"
          :aria-label="pendingProceedingLabels.resolutionNote"
          field-size="max"
          preview
          :value="pendingProceeding.shortTexts.resolutionNote"
        />
      </PreviewContent>
    </PreviewRow>
  </FlexContainer>
</template>
