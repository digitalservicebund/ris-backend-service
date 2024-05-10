<script setup lang="ts">
import dayjs from "dayjs"
import FlexContainer from "@/components/FlexContainer.vue"
import PreviewCategory from "@/components/preview/PreviewCategory.vue"
import PreviewContent from "@/components/preview/PreviewContent.vue"
import { CoreData } from "@/domain/documentUnit"

defineProps<{
  coreData: CoreData
}>()
</script>

<template>
  <FlexContainer class="flex-col">
    <FlexContainer v-if="coreData.court" class="flex-row">
      <PreviewCategory>Gericht</PreviewCategory>
      <PreviewContent>
        {{ coreData.court.type }}
        {{ coreData.court.location }}
      </PreviewContent>
    </FlexContainer>
    <FlexContainer
      v-if="coreData.deviatingCourts && coreData.deviatingCourts.length > 0"
      class="flex-row"
    >
      <PreviewCategory>Fehlerhaftes Gericht</PreviewCategory>
      <PreviewContent>
        {{ coreData.deviatingCourts.toString() }}
      </PreviewContent>
    </FlexContainer>
    <FlexContainer
      v-if="coreData.fileNumbers && coreData.fileNumbers.length > 0"
      class="flex-row"
    >
      <PreviewCategory> Aktenzeichen</PreviewCategory>
      <PreviewContent>
        {{ coreData.fileNumbers.toString() }}
      </PreviewContent>
    </FlexContainer>
    <FlexContainer
      v-if="
        coreData.deviatingFileNumbers &&
        coreData.deviatingFileNumbers.length > 0
      "
      class="flex-row"
    >
      <PreviewCategory> Abweichendes Aktenzeichen</PreviewCategory>
      <PreviewContent>
        {{ coreData.deviatingFileNumbers.toString() }}
      </PreviewContent>
    </FlexContainer>
    <FlexContainer v-if="coreData.decisionDate" class="flex-row">
      <PreviewCategory>Entscheidungsdatum</PreviewCategory>
      <PreviewContent
        >{{ dayjs(coreData.decisionDate).format("DD.MM.YYYY") }}
      </PreviewContent>
    </FlexContainer>
    <FlexContainer
      v-if="
        coreData.deviatingDecisionDates &&
        coreData.deviatingDecisionDates.length > 0
      "
      class="flex-row"
    >
      <PreviewCategory>Abweichendes Entscheidungsdatum</PreviewCategory>
      <PreviewContent>
        <div
          v-for="(item, index) in coreData.deviatingDecisionDates"
          :key="index"
        >
          {{ dayjs(item).format("DD.MM.YYYY") }}
        </div>
      </PreviewContent>
    </FlexContainer>
    <FlexContainer v-if="coreData.appraisalBody" class="flex-row">
      <PreviewCategory>Spruchkörper</PreviewCategory>
      <PreviewContent>{{ coreData.appraisalBody }}</PreviewContent>
    </FlexContainer>
    <FlexContainer v-if="coreData.documentType" class="flex-row">
      <PreviewCategory>Dokumenttyp</PreviewCategory>
      <PreviewContent>{{ coreData.documentType.label }}</PreviewContent>
    </FlexContainer>
    <FlexContainer v-if="coreData.ecli" class="flex-row">
      <PreviewCategory>ECLI</PreviewCategory>
      <PreviewContent>{{ coreData.ecli }}</PreviewContent>
    </FlexContainer>
    <FlexContainer
      v-if="coreData.deviatingEclis && coreData.deviatingEclis.length > 0"
      class="flex-row"
    >
      <PreviewCategory>Abweichender ECLI</PreviewCategory>
      <PreviewContent>{{ coreData.deviatingEclis.toString() }}</PreviewContent>
    </FlexContainer>
    <FlexContainer v-if="coreData.procedure" class="flex-row">
      <PreviewCategory>Vorgang</PreviewCategory>
      <PreviewContent>{{ coreData.procedure.label }}</PreviewContent>
    </FlexContainer>
    <FlexContainer
      v-if="
        coreData.previousProcedures && coreData.previousProcedures.length > 0
      "
      class="flex-row"
    >
      <PreviewCategory>Vorgangshistorie</PreviewCategory>
      <PreviewContent
        >{{ coreData.previousProcedures?.toReversed().toString() }}
      </PreviewContent>
    </FlexContainer>
    <FlexContainer v-if="coreData.legalEffect" class="flex-row">
      <PreviewCategory>Rechtskraft</PreviewCategory>
      <PreviewContent>{{ coreData.legalEffect }}</PreviewContent>
    </FlexContainer>
    <FlexContainer v-if="coreData.region" class="flex-row">
      <PreviewCategory>Region</PreviewCategory>
      <PreviewContent>{{ coreData.region }}</PreviewContent>
    </FlexContainer>
  </FlexContainer>
</template>
