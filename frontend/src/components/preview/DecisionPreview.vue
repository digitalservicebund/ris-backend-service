<script setup lang="ts">
import dayjs from "dayjs"
import { storeToRefs } from "pinia"
import { provide, Ref } from "vue"
import FlexContainer from "@/components/FlexContainer.vue"
import {
  PreviewLayout,
  previewLayoutInjectionKey,
} from "@/components/preview/constants"
import PreviewCaselawReferences from "@/components/preview/PreviewCaselawReferences.vue"
import PreviewContentRelatedIndexing from "@/components/preview/PreviewContentRelatedIndexing.vue"
import PreviewCoreData from "@/components/preview/PreviewCoreData.vue"
import PreviewLiteratureReferences from "@/components/preview/PreviewLiteratureReferences.vue"
import PreviewLongTexts from "@/components/preview/PreviewLongTexts.vue"
import PreviewNote from "@/components/preview/PreviewNote.vue"
import PreviewProceedingDecisions from "@/components/preview/PreviewProceedingDecisions.vue"
import PreviewShortTexts from "@/components/preview/PreviewShortTexts.vue"
import { ContentRelatedIndexing } from "@/domain/contentRelatedIndexing"
import { Decision, LongTexts } from "@/domain/decision"
import { Kind } from "@/domain/documentationUnitKind"
import EnsuingDecision from "@/domain/ensuingDecision"
import PreviousDecision from "@/domain/previousDecision"
import Reference from "@/domain/reference"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const props = defineProps<{
  layout?: PreviewLayout
}>()

const { documentUnit: decision } = storeToRefs(useDocumentUnitStore()) as {
  documentUnit: Ref<Decision | undefined>
}

provide(previewLayoutInjectionKey, props.layout || "wide")
</script>

<template>
  <FlexContainer
    v-if="decision"
    class="max-w-screen-xl"
    data-testid="preview"
    flex-direction="flex-col"
  >
    <h1 class="ris-heading3-bold mt-16 px-16">
      {{ decision.documentNumber }}
    </h1>
    <p class="ris-label3-regular mb-16 px-16">
      Vorschau erstellt am {{ dayjs(new Date()).format("DD.MM.YYYY") }} um
      {{ dayjs(new Date()).format("HH:mm:ss") }}
    </p>
    <PreviewCoreData
      :core-data="decision.coreData"
      :date-label="
        decision.coreData.hasDeliveryDate
          ? 'Datum der Zustellung an Verkündungs statt'
          : 'Entscheidungsdatum'
      "
      :kind="Kind.DECISION"
    />
    <PreviewNote :note="decision.note" />
    <PreviewCaselawReferences
      :caselaw-references="decision.caselawReferences as Reference[]"
    />
    <PreviewLiteratureReferences
      :literature-references="decision.literatureReferences as Reference[]"
    />
    <PreviewProceedingDecisions
      :ensuing-decisions="decision.ensuingDecisions as EnsuingDecision[]"
      :previous-decisions="decision.previousDecisions as PreviousDecision[]"
    />
    <PreviewContentRelatedIndexing
      :content-related-indexing="
        decision.contentRelatedIndexing as ContentRelatedIndexing
      "
    />
    <PreviewShortTexts
      :short-texts="decision.shortTexts"
      :valid-border-numbers="decision.managementData.borderNumbers"
    />
    <PreviewLongTexts :long-texts="decision.longTexts as LongTexts" />
  </FlexContainer>
</template>
