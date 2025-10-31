<script setup lang="ts">
import dayjs from "dayjs"
import { computed } from "vue"
import FlexContainer from "@/components/FlexContainer.vue"
import FlexItem from "@/components/FlexItem.vue"
import PreviewCategory from "@/components/preview/PreviewCategory.vue"
import PreviewContent from "@/components/preview/PreviewContent.vue"
import PreviewRow from "@/components/preview/PreviewRow.vue"
import { CoreData, coreDataLabels } from "@/domain/coreData"

import { Kind } from "@/domain/documentationUnitKind"

const props = defineProps<{
  coreData: CoreData
  kind: Kind
}>()

const isPendingProceeding = props.kind === Kind.PENDING_PROCEEDING
// TODO: (Malte Laukötter, 2025-10-31) change to array
const sourceValue = computed(() =>
  props.coreData.sources
    ? (props.coreData.sources?.[0].value ??
      props.coreData.sources?.[0].sourceRawValue)
    : undefined,
)
const dateLabel = computed(() => {
  const decisionDateLabel = props.coreData.hasDeliveryDate
    ? "Datum der Zustellung an Verkündungs statt"
    : "Entscheidungsdatum"
  return isPendingProceeding ? "Mitteilungsdatum" : decisionDateLabel
})
</script>

<template>
  <FlexContainer flex-direction="flex-col">
    <PreviewRow v-if="coreData.court">
      <PreviewCategory>Gericht</PreviewCategory>
      <PreviewContent>
        {{ coreData.court.type }}
        {{ coreData.court.location }}
      </PreviewContent>
    </PreviewRow>
    <PreviewRow
      v-if="coreData.deviatingCourts && coreData.deviatingCourts.length > 0"
    >
      <PreviewCategory>Fehlerhaftes Gericht</PreviewCategory>
      <PreviewContent>
        {{ coreData.deviatingCourts.join(", ") }}
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="coreData.fileNumbers && coreData.fileNumbers.length > 0">
      <PreviewCategory> Aktenzeichen</PreviewCategory>
      <PreviewContent>
        {{ coreData.fileNumbers.join(", ") }}
      </PreviewContent>
    </PreviewRow>
    <PreviewRow
      v-if="
        coreData.deviatingFileNumbers &&
        coreData.deviatingFileNumbers.length > 0
      "
    >
      <PreviewCategory> Abweichendes Aktenzeichen</PreviewCategory>
      <PreviewContent>
        {{ coreData.deviatingFileNumbers.join(", ") }}
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="coreData.decisionDate">
      <PreviewCategory>{{ dateLabel }}</PreviewCategory>
      <PreviewContent
        >{{ dayjs(coreData.decisionDate).format("DD.MM.YYYY") }}
      </PreviewContent>
    </PreviewRow>
    <PreviewRow
      v-if="
        coreData.deviatingDecisionDates &&
        coreData.deviatingDecisionDates.length > 0
      "
    >
      <PreviewCategory>Abweichendes {{ dateLabel }}</PreviewCategory>
      <PreviewContent>
        <div v-for="item in coreData.deviatingDecisionDates" :key="item">
          {{ dayjs(item).format("DD.MM.YYYY") }}
        </div>
      </PreviewContent>
    </PreviewRow>
    <PreviewRow
      v-if="coreData.oralHearingDates && coreData.oralHearingDates.length > 0"
    >
      <PreviewCategory>{{ coreDataLabels.oralHearingDates }}</PreviewCategory>
      <PreviewContent>
        <div v-for="item in coreData.oralHearingDates" :key="item">
          {{ dayjs(item).format("DD.MM.YYYY") }}
        </div>
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="coreData.appraisalBody">
      <PreviewCategory>Spruchkörper</PreviewCategory>
      <PreviewContent>{{ coreData.appraisalBody }}</PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="coreData.documentType">
      <PreviewCategory>Dokumenttyp</PreviewCategory>
      <PreviewContent>{{ coreData.documentType.label }}</PreviewContent>
    </PreviewRow>
    <PreviewRow
      v-if="
        coreData.deviatingDocumentNumbers &&
        coreData.deviatingDocumentNumbers.length > 0
      "
    >
      <PreviewCategory>Abweichende Dokumentnummer</PreviewCategory>
      <PreviewContent>
        {{ coreData.deviatingDocumentNumbers.join(", ") }}
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="coreData.celexNumber">
      <PreviewCategory>CELEX-Nummer</PreviewCategory>
      <PreviewContent>{{ coreData.celexNumber }}</PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="coreData.ecli">
      <PreviewCategory>ECLI</PreviewCategory>
      <PreviewContent>{{ coreData.ecli }}</PreviewContent>
    </PreviewRow>
    <PreviewRow
      v-if="coreData.deviatingEclis && coreData.deviatingEclis.length > 0"
    >
      <PreviewCategory>Abweichender ECLI</PreviewCategory>
      <PreviewContent>{{ coreData.deviatingEclis.join(", ") }}</PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="coreData.procedure">
      <PreviewCategory>Vorgang</PreviewCategory>
      <PreviewContent>{{ coreData.procedure.label }}</PreviewContent>
    </PreviewRow>
    <PreviewRow
      v-if="
        coreData.previousProcedures && coreData.previousProcedures.length > 0
      "
    >
      <PreviewCategory>Vorgangshistorie</PreviewCategory>
      <PreviewContent
        >{{ coreData.previousProcedures?.toReversed().join(", ") }}
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="coreData.legalEffect">
      <PreviewCategory>Rechtskraft</PreviewCategory>
      <PreviewContent>{{ coreData.legalEffect }}</PreviewContent>
    </PreviewRow>
    <PreviewRow
      v-if="coreData.yearsOfDispute && coreData.yearsOfDispute.length > 0"
    >
      <PreviewCategory>Streitjahr</PreviewCategory>
      <PreviewContent>
        <FlexContainer
          v-for="yearOfDispute in coreData.yearsOfDispute"
          :key="yearOfDispute"
          flex-direction="flex-col"
        >
          <FlexItem> {{ yearOfDispute }}</FlexItem>
        </FlexContainer>
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="sourceValue">
      <PreviewCategory>Quelle</PreviewCategory>
      <PreviewContent>
        {{ sourceValue }}
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="coreData.inputTypes && coreData.inputTypes.length > 0">
      <PreviewCategory>Eingangsart</PreviewCategory>
      <PreviewContent>
        {{ coreData.inputTypes?.join(", ") }}
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="isPendingProceeding">
      <PreviewCategory>Erledigung</PreviewCategory>
      <PreviewContent>
        {{ coreData.isResolved ? "Ja" : "Nein" }}
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="coreData.resolutionDate">
      <PreviewCategory>Erledigungsmitteilung</PreviewCategory>
      <PreviewContent>
        {{ dayjs(coreData.resolutionDate).format("DD.MM.YYYY") }}
      </PreviewContent>
    </PreviewRow>
    <PreviewRow v-if="coreData.court?.jurisdictionType">
      <PreviewCategory>Gerichtsbarkeit</PreviewCategory>
      <PreviewContent>
        {{ coreData.court.jurisdictionType }}
      </PreviewContent>
    </PreviewRow>
    <PreviewRow
      v-if="coreData.court?.regions && coreData.court?.regions.length > 0"
    >
      <PreviewCategory>Region</PreviewCategory>
      <PreviewContent>{{ coreData.court.regions.join(", ") }}</PreviewContent>
    </PreviewRow>
    <PreviewRow
      v-if="
        coreData.leadingDecisionNormReferences &&
        coreData.leadingDecisionNormReferences.length > 0
      "
    >
      <PreviewCategory>BGH Nachschlagewerk</PreviewCategory>
      <PreviewContent>
        <FlexContainer
          v-for="leadingDecisionNormReference in coreData.leadingDecisionNormReferences"
          :key="leadingDecisionNormReference"
          flex-direction="flex-col"
        >
          <FlexItem> {{ leadingDecisionNormReference }}</FlexItem>
        </FlexContainer>
      </PreviewContent>
    </PreviewRow>
  </FlexContainer>
</template>
