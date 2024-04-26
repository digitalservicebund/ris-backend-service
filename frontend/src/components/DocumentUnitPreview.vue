<script setup lang="ts">
import CellItem from "@/components/CellItem.vue"
import DocumentUnitWrapper from "@/components/DocumentUnitWrapper.vue"
import TableRow from "@/components/TableRow.vue"
import TableView from "@/components/TableView.vue"
import TitleElement from "@/components/TitleElement.vue"
import legalEffectTypes from "@/data/legalEffectTypes.json"
import DocumentUnit from "@/domain/documentUnit"

const props = defineProps<{
  documentUnit: DocumentUnit
  showAttachmentPanel?: boolean
  showNavigationPanel: boolean
}>()
</script>

<template>
  <DocumentUnitWrapper
    :document-unit="documentUnit"
    :show-navigation-panel="showNavigationPanel"
  >
    <TitleElement>Preview</TitleElement>

    <TableView>
      <TableRow>
        <CellItem> Gericht</CellItem>
        <CellItem>
          {{ documentUnit.coreData.court?.type }}
        </CellItem>
      </TableRow>
      <TableRow>
        <CellItem> Fehlerhaftes Gericht</CellItem>
        <CellItem>
          {{ documentUnit.coreData.deviatingCourts?.toString() }}
        </CellItem>
      </TableRow>
      <TableRow>
        <CellItem> Aktenzeichen</CellItem>
        <CellItem>
          {{ documentUnit.coreData.fileNumbers?.toString() }}
        </CellItem>
      </TableRow>
      <TableRow>
        <CellItem> Abweichendes Aktenzeichen</CellItem>
        <CellItem>
          {{ documentUnit.coreData.deviatingFileNumbers?.toString() }}
        </CellItem>
      </TableRow>
      <TableRow>
        <CellItem>Entscheidungsdatum</CellItem>
        <CellItem>{{ documentUnit.coreData.decisionDate }}</CellItem>
      </TableRow>
      <TableRow>
        <CellItem>Spruchkörper</CellItem>
        <CellItem>{{ documentUnit.coreData.appraisalBody }}</CellItem>
      </TableRow>
      <TableRow>
        <CellItem>Dokumenttyp</CellItem>
        <CellItem>{{ documentUnit.coreData.documentType }}</CellItem>
      </TableRow>
      <TableRow>
        <CellItem>ECLI</CellItem>
        <CellItem>{{ documentUnit.coreData.ecli }}</CellItem>
      </TableRow>
      <TableRow>
        <CellItem>Abweichender ECLI</CellItem>
        <CellItem
          >{{ documentUnit.coreData.deviatingEclis?.toString() }}
        </CellItem>
      </TableRow>
      <TableRow>
        <CellItem>Vorgang</CellItem>
        <CellItem>{{ documentUnit.coreData.procedure }}</CellItem>
      </TableRow>
      <TableRow>
        <CellItem>Vorgangshistorie</CellItem>
        <CellItem
          >{{
            documentUnit.coreData.previousProcedures?.toReversed().toString()
          }}
        </CellItem>
      </TableRow>
      <TableRow>
        <CellItem>Rechtskraft</CellItem>
        <CellItem>{{ documentUnit.coreData.legalEffect }}</CellItem>
      </TableRow>
      <TableRow>
        <CellItem>Region</CellItem>
        <CellItem>{{ documentUnit.coreData.region }}</CellItem>
      </TableRow>
    </TableView>

    <TableView>
      <TableRow>
        <CellItem
          v-if="
            documentUnit.previousDecisions &&
            documentUnit.previousDecisions?.length > 0
          "
          >Vorinstanz
        </CellItem>
        <CellItem
          v-for="(previousDecision, index) in documentUnit.previousDecisions"
          :key="index"
        >
          {{ previousDecision.renderDecision }}
        </CellItem>
      </TableRow>
      <TableRow>
        <CellItem
          v-if="
            documentUnit.ensuingDecisions &&
            documentUnit.ensuingDecisions?.length > 0
          "
          >Nachgehende Entscheidungen
        </CellItem>
        <CellItem
          v-for="(ensuingDecision, index) in documentUnit.ensuingDecisions"
          :key="index"
        >
          {{ ensuingDecision.renderDecision }}
        </CellItem>
      </TableRow>
    </TableView>

    <TableView></TableView>

    <TableView>
      <TableRow>
        <CellItem>Entscheidungsname</CellItem>
        <CellItem>{{ documentUnit.texts.decisionName }}</CellItem>
      </TableRow>
      <TableRow>
        <CellItem>Titelzeile</CellItem>
        <CellItem>{{ documentUnit.texts.headline }}</CellItem>
      </TableRow>
      <TableRow>
        <CellItem>Leitsatz</CellItem>
        <CellItem>{{ documentUnit.texts.guidingPrinciple }}</CellItem>
      </TableRow>
      <TableRow>
        <CellItem>Orientierungssatz</CellItem>
        <CellItem>{{ documentUnit.texts.headnote }}</CellItem>
      </TableRow>
      <TableRow>
        <CellItem>Tenor</CellItem>
        <CellItem>{{ documentUnit.texts.tenor }}</CellItem>
      </TableRow>
      <TableRow>
        <CellItem>Gründe</CellItem>
        <CellItem>{{ documentUnit.texts.reasons }}</CellItem>
      </TableRow>
      <TableRow>
        <CellItem>Tatbestand</CellItem>
        <CellItem>{{ documentUnit.texts.reasons }}</CellItem>
      </TableRow>
      <TableRow>
        <CellItem>Entscheidungsgründe</CellItem>
        <CellItem>{{ documentUnit.texts.caseFacts }}</CellItem>
      </TableRow>
    </TableView>
  </DocumentUnitWrapper>
</template>
