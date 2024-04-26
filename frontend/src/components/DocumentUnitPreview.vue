<script setup lang="ts">
import CellItem from "@/components/CellItem.vue"
import DocumentUnitWrapper from "@/components/DocumentUnitWrapper.vue"
import TableRow from "@/components/TableRow.vue"
import TableView from "@/components/TableView.vue"
import TitleElement from "@/components/TitleElement.vue"
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
        <CellItem>
          {{ documentUnit.coreData.court?.location }}
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
        <CellItem></CellItem>
      </TableRow>
    </TableView>

    <TableView>
      <TableRow>
        <CellItem v-if="documentUnit.previousDecisions?.length > 0"
          >Vorinstanz
        </CellItem>
        <CellItem
          v-for="(previousDecision, index) in documentUnit.previousDecisions"
          :key="index"
        >
          {{ previousDecision.court?.type }}
        </CellItem>
      </TableRow>
    </TableView>
    <TitleElement>{{ documentUnit }}</TitleElement>
  </DocumentUnitWrapper>
</template>
