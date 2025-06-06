<script setup lang="ts">
import dayjs from "dayjs"
import { provide } from "vue"
import FlexContainer from "@/components/FlexContainer.vue"
import TextEditor from "@/components/input/TextEditor.vue"
import ErrorPage from "@/components/PageError.vue"
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
import PendingProceeding, {
  pendingProceedingShortTextLabels,
} from "@/domain/pendingProceeding"
import Reference from "@/domain/reference"
import documentUnitService from "@/services/documentUnitService"
import { ServiceResponse } from "@/services/httpClient"

const props = defineProps<{
  layout?: PreviewLayout
  documentNumber: string
}>()

const pendingProceedingResponse = await loadPendingProceeding(
  props.documentNumber,
)

async function loadPendingProceeding(
  documentNumber: string,
): Promise<ServiceResponse<PendingProceeding>> {
  return await documentUnitService.getPendingProceedingByDocumentNumber(
    documentNumber,
  )
}

provide(previewLayoutInjectionKey, props.layout || "wide")
</script>

<template>
  <FlexContainer
    v-if="pendingProceedingResponse.data"
    class="max-w-screen-xl"
    data-testid="preview"
    flex-direction="flex-col"
  >
    <h1 class="ris-heading3-bold mt-16 px-16">
      {{ pendingProceedingResponse.data.documentNumber }}
    </h1>
    <p class="ris-label3-regular mb-16 px-16">
      Vorschau erstellt am {{ dayjs(new Date()).format("DD.MM.YYYY") }} um
      {{ dayjs(new Date()).format("HH:mm:ss") }}
    </p>
    <PreviewCoreData
      :core-data="pendingProceedingResponse.data.coreData"
      date-label="Mitteilungsdatum"
    />
    <FlexContainer flex-direction="flex-col">
      <PreviewRow v-if="pendingProceedingResponse.data.resolutionNote">
        <PreviewCategory>Erledigungsvermerk</PreviewCategory>
        <PreviewContent>
          <TextEditor
            id="previewResolutionNote"
            :aria-label="pendingProceedingShortTextLabels.resolutionNote"
            field-size="max"
            preview
            :value="pendingProceedingResponse.data.resolutionNote"
          />
        </PreviewContent>
      </PreviewRow>
      <PreviewRow v-if="pendingProceedingResponse.data.legalIssue">
        <PreviewCategory>{{
          pendingProceedingShortTextLabels.legalIssue
        }}</PreviewCategory>
        <PreviewContent>
          <TextEditor
            id="previewLegalIssue"
            :aria-label="pendingProceedingShortTextLabels.legalIssue"
            field-size="max"
            preview
            :value="pendingProceedingResponse.data.legalIssue"
          />
        </PreviewContent>
      </PreviewRow>
      <PreviewRow v-if="pendingProceedingResponse.data.admissionOfAppeal">
        <PreviewCategory>{{
          pendingProceedingShortTextLabels.admissionOfAppeal
        }}</PreviewCategory>
        <PreviewContent>
          {{ pendingProceedingResponse.data.admissionOfAppeal }}
        </PreviewContent>
      </PreviewRow>
      <PreviewRow v-if="pendingProceedingResponse.data.appellant">
        <PreviewCategory>{{
          pendingProceedingShortTextLabels.appellant
        }}</PreviewCategory>
        <PreviewContent>
          {{ pendingProceedingResponse.data.appellant }}
        </PreviewContent>
      </PreviewRow>
    </FlexContainer>
    <PreviewCaselawReferences
      :caselaw-references="
        pendingProceedingResponse.data.caselawReferences as Reference[]
      "
    />
    <PreviewLiteratureReferences
      :literature-references="
        pendingProceedingResponse.data.literatureReferences as Reference[]
      "
    />
    <PreviewProceedingDecisions
      :ensuing-decisions="pendingProceedingResponse.data.ensuingDecisions"
      :previous-decisions="pendingProceedingResponse.data.previousDecisions"
    />
    <PreviewContentRelatedIndexing
      :content-related-indexing="
        pendingProceedingResponse.data.contentRelatedIndexing
      "
    />

    <PreviewShortTexts
      :short-texts="pendingProceedingResponse.data.shortTexts"
      :valid-border-numbers="[]"
    />
  </FlexContainer>
  <ErrorPage
    v-if="pendingProceedingResponse.error"
    :error="pendingProceedingResponse.error"
    :title="pendingProceedingResponse.error?.title"
  />
</template>
