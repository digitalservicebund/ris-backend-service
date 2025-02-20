<script setup lang="ts">
import dayjs from "dayjs"
import { provide } from "vue"
import FlexContainer from "@/components/FlexContainer.vue"
import {
  PreviewLayout,
  previewLayoutInjectionKey,
} from "@/components/preview/constants"
import PreviewCaselawReferences from "@/components/preview/PreviewCaselawReferences.vue"
import PreviewContentRelatedIndexing from "@/components/preview/PreviewContentRelatedIndexing.vue"
import PreviewCoreData from "@/components/preview/PreviewCoreData.vue"
import PreviewLiteratureReferences from "@/components/preview/PreviewLiteratureReferences.vue"
import PreviewProceedingDecisions from "@/components/preview/PreviewProceedingDecisions.vue"
import PreviewShortTexts from "@/components/preview/PreviewShortTexts.vue"
import Reference from "@/domain/reference"
import documentUnitService from "@/services/documentUnitService"
import PendingProceeding from "@/domain/pendingProceeding"

const props = defineProps<{
  layout?: PreviewLayout
  documentNumber: string
}>()

const documentUnit = await loadPendingProceeding(props.documentNumber)

async function loadPendingProceeding(
  documentNumber: string,
): PendingProceeding | undefined {
  const response =
    await documentUnitService.getPendingProceedingByDocumentNumber(
      documentNumber,
    )
  return response.data
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
    <PreviewCoreData :core-data="documentUnit.coreData" />
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
      valid-border-numbers="[]"
    />
  </FlexContainer>
</template>
