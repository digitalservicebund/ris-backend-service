<script setup lang="ts">
import dayjs from "dayjs"
import { provide } from "vue"
import FlexContainer from "@/components/FlexContainer.vue"
import {
  PreviewLayout,
  previewLayoutInjectionKey,
} from "@/components/preview/constants"
import PreviewCaselawReferences from "@/components/preview/PreviewCaselawReferences.vue"
import PreviewCategory from "@/components/preview/PreviewCategory.vue"
import PreviewContent from "@/components/preview/PreviewContent.vue"
import PreviewCoreData from "@/components/preview/PreviewCoreData.vue"
import PreviewLiteratureReferences from "@/components/preview/PreviewLiteratureReferences.vue"
import PreviewRow from "@/components/preview/PreviewRow.vue"
import PreviewShortTexts from "@/components/preview/PreviewShortTexts.vue"
import PendingProceeding from "@/domain/pendingProceeding"
import Reference from "@/domain/reference"
import documentUnitService from "@/services/documentUnitService"

const props = defineProps<{
  layout?: PreviewLayout
  documentNumber: string
}>()

const documentUnit = await loadPendingProceeding(props.documentNumber)

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
    v-if="documentUnit"
    class="max-w-screen-xl"
    data-testid="preview"
    flex-direction="flex-col"
  >
    <h1 class="ds-heading-03-bold mt-16 px-16">
      {{ documentUnit.documentNumber }}
    </h1>
    <p class="ds-label-03-reg mb-16 px-16">
      Vorschau erstellt am {{ dayjs(new Date()).format("DD.MM.YYYY") }} um
      {{ dayjs(new Date()).format("HH:mm:ss") }}
    </p>
    <PreviewCoreData
      :core-data="documentUnit.coreData"
      date-label="Mitteilungsdatum"
    />
    <FlexContainer flex-direction="flex-col">
      <PreviewRow v-if="documentUnit.resolutionNote">
        <PreviewCategory>Erledigungsvermerk</PreviewCategory>
        <PreviewContent>
          {{ documentUnit.resolutionNote }}
        </PreviewContent>
      </PreviewRow>
      <PreviewRow>
        <PreviewCategory>Erledigung</PreviewCategory>
        <PreviewContent>
          {{ documentUnit.isResolved ? "Ja" : "Nein" }}
        </PreviewContent>
      </PreviewRow>
      <PreviewRow v-if="documentUnit.legalIssue">
        <PreviewCategory>Rechtsfrage</PreviewCategory>
        <PreviewContent>
          {{ documentUnit.legalIssue }}
        </PreviewContent>
      </PreviewRow>
      <PreviewRow v-if="documentUnit.admissionOfAppeal">
        <PreviewCategory>Rechtsmittelzulassung</PreviewCategory>
        <PreviewContent>
          {{ documentUnit.admissionOfAppeal }}
        </PreviewContent>
      </PreviewRow>
      <PreviewRow v-if="documentUnit.appellant">
        <PreviewCategory>Rechtsmittelführer</PreviewCategory>
        <PreviewContent>
          {{ documentUnit.appellant }}
        </PreviewContent>
      </PreviewRow>
    </FlexContainer>
    <PreviewCaselawReferences
      :caselaw-references="documentUnit.caselawReferences as Reference[]"
    />
    <PreviewLiteratureReferences
      :literature-references="documentUnit.literatureReferences as Reference[]"
    />
    <PreviewProceedingDecisions />
    <PreviewContentRelatedIndexing />

    <PreviewShortTexts
      :short-texts="documentUnit.shortTexts"
      :valid-border-numbers="[]"
    />
  </FlexContainer>
</template>
