<script setup lang="ts">
import dayjs from "dayjs"
import { provide } from "vue"
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
import PendingProceeding from "@/domain/pendingProceeding"
import Reference from "@/domain/reference"
import documentUnitService from "@/services/documentUnitService"

const props = defineProps<{
  layout?: PreviewLayout
  documentNumber: string
}>()

const pendingProceeding = await loadPendingProceeding(props.documentNumber)

async function loadPendingProceeding(
  documentNumber: string,
): Promise<PendingProceeding> {
  const response =
    await documentUnitService.getPendingProceedingByDocumentNumber(
      documentNumber,
    )
  return response.data!
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
    <h1 class="ds-heading-03-bold mt-16 px-16">
      {{ pendingProceeding.documentNumber }}
    </h1>
    <p class="ds-label-03-reg mb-16 px-16">
      Vorschau erstellt am {{ dayjs(new Date()).format("DD.MM.YYYY") }} um
      {{ dayjs(new Date()).format("HH:mm:ss") }}
    </p>
    <PreviewCoreData
      :core-data="pendingProceeding.coreData"
      date-label="Mitteilungsdatum"
    />
    <FlexContainer flex-direction="flex-col">
      <PreviewRow v-if="pendingProceeding.resolutionNote">
        <PreviewCategory>Erledigungsvermerk</PreviewCategory>
        <PreviewContent>
          <TextEditor
            id="previewResolutionNote"
            aria-label="Erledigungsvermerk"
            field-size="max"
            preview
            :value="pendingProceeding.resolutionNote"
          />
        </PreviewContent>
      </PreviewRow>
      <PreviewRow>
        <PreviewCategory>Erledigung</PreviewCategory>
        <PreviewContent>
          {{ pendingProceeding.isResolved ? "Ja" : "Nein" }}
        </PreviewContent>
      </PreviewRow>
      <PreviewRow v-if="pendingProceeding.legalIssue">
        <PreviewCategory>Rechtsfrage</PreviewCategory>
        <PreviewContent>
          <TextEditor
            id="previewLegalIssue"
            aria-label="Rechtsfrage"
            field-size="max"
            preview
            :value="pendingProceeding.legalIssue"
          />
        </PreviewContent>
      </PreviewRow>
      <PreviewRow v-if="pendingProceeding.admissionOfAppeal">
        <PreviewCategory>Rechtsmittelzulassung</PreviewCategory>
        <PreviewContent>
          {{ pendingProceeding.admissionOfAppeal }}
        </PreviewContent>
      </PreviewRow>
      <PreviewRow v-if="pendingProceeding.appellant">
        <PreviewCategory>Rechtsmittelführer</PreviewCategory>
        <PreviewContent>
          {{ pendingProceeding.appellant }}
        </PreviewContent>
      </PreviewRow>
    </FlexContainer>
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
  </FlexContainer>
</template>
