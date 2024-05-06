<script setup lang="ts">
import dayjs from "dayjs"
import PreviewLeftCell from "@/components/preview/PreviewLeftCell.vue"
import PreviewRightCell from "@/components/preview/PreviewRightCell.vue"
import TableView from "@/components/TableView.vue"
import { CoreData } from "@/domain/documentUnit"

defineProps<{
  coreData: CoreData
}>()
</script>

<template>
  <TableView class="table w-full table-fixed">
    <tr v-if="coreData.court">
      <PreviewLeftCell>Gericht</PreviewLeftCell>
      <PreviewRightCell>
        {{ coreData.court.type }}
        {{ coreData.court.location }}
      </PreviewRightCell>
    </tr>
    <tr v-if="coreData.deviatingCourts && coreData.deviatingCourts.length > 0">
      <PreviewLeftCell>Fehlerhaftes Gericht</PreviewLeftCell>
      <PreviewRightCell>
        {{ coreData.deviatingCourts.toString() }}
      </PreviewRightCell>
    </tr>
    <tr v-if="coreData.fileNumbers && coreData.fileNumbers.length > 0">
      <PreviewLeftCell> Aktenzeichen</PreviewLeftCell>
      <PreviewRightCell>
        {{ coreData.fileNumbers.toString() }}
      </PreviewRightCell>
    </tr>
    <tr
      v-if="
        coreData.deviatingFileNumbers &&
        coreData.deviatingFileNumbers.length > 0
      "
    >
      <PreviewLeftCell> Abweichendes Aktenzeichen</PreviewLeftCell>
      <PreviewRightCell>
        {{ coreData.deviatingFileNumbers.toString() }}
      </PreviewRightCell>
    </tr>
    <tr v-if="coreData.decisionDate">
      <PreviewLeftCell>Entscheidungsdatum</PreviewLeftCell>
      <PreviewRightCell>{{
        dayjs(coreData.decisionDate).format("DD.MM.YYYY")
      }}</PreviewRightCell>
    </tr>
    <tr
      v-if="
        coreData.deviatingDecisionDates &&
        coreData.deviatingDecisionDates.length > 0
      "
    >
      <PreviewLeftCell>Abweichendes Entscheidungsdatum</PreviewLeftCell>
      <PreviewRightCell>
        <div
          v-for="(item, index) in coreData.deviatingDecisionDates"
          :key="index"
        >
          {{ dayjs(item).format("DD.MM.YYYY") }}
        </div>
      </PreviewRightCell>
    </tr>
    <tr v-if="coreData.appraisalBody">
      <PreviewLeftCell>Spruchkörper</PreviewLeftCell>
      <PreviewRightCell>{{ coreData.appraisalBody }}</PreviewRightCell>
    </tr>
    <tr v-if="coreData.documentType">
      <PreviewLeftCell>Dokumenttyp</PreviewLeftCell>
      <PreviewRightCell>{{ coreData.documentType.label }}</PreviewRightCell>
    </tr>
    <tr v-if="coreData.ecli">
      <PreviewLeftCell>ECLI</PreviewLeftCell>
      <PreviewRightCell>{{ coreData.ecli }}</PreviewRightCell>
    </tr>
    <tr v-if="coreData.deviatingEclis && coreData.deviatingEclis.length > 0">
      <PreviewLeftCell>Abweichender ECLI</PreviewLeftCell>
      <PreviewRightCell
        >{{ coreData.deviatingEclis.toString() }}
      </PreviewRightCell>
    </tr>
    <tr v-if="coreData.procedure">
      <PreviewLeftCell>Vorgang</PreviewLeftCell>
      <PreviewRightCell>{{ coreData.procedure.label }} </PreviewRightCell>
    </tr>
    <tr
      v-if="
        coreData.previousProcedures && coreData.previousProcedures.length > 0
      "
    >
      <PreviewLeftCell>Vorgangshistorie</PreviewLeftCell>
      <PreviewRightCell
        >{{ coreData.previousProcedures?.toReversed().toString() }}
      </PreviewRightCell>
    </tr>
    <tr v-if="coreData.legalEffect">
      <PreviewLeftCell>Rechtskraft</PreviewLeftCell>
      <PreviewRightCell>{{ coreData.legalEffect }}</PreviewRightCell>
    </tr>
    <tr v-if="coreData.region">
      <PreviewLeftCell>Region</PreviewLeftCell>
      <PreviewRightCell>{{ coreData.region }}</PreviewRightCell>
    </tr>
  </TableView>
</template>
