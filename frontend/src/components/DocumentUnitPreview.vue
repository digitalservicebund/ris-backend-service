<script setup lang="ts">
import TextEditor from "../components/input/TextEditor.vue"
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

    <TableView
      v-if="documentUnit.previousDecisions || documentUnit.ensuingDecisions"
    >
      <TableRow
        v-if="
          documentUnit.previousDecisions &&
          documentUnit.previousDecisions?.length > 0
        "
      >
        <CellItem>Vorinstanz</CellItem>
        <CellItem>
          <ul>
            <li
              v-for="(
                previousDecision, index
              ) in documentUnit.previousDecisions"
              :key="index"
            >
              {{ previousDecision.renderDecision }}
            </li>
          </ul>
        </CellItem>
      </TableRow>
      <TableRow
        v-if="
          documentUnit.ensuingDecisions &&
          documentUnit.ensuingDecisions?.length > 0
        "
      >
        <CellItem>Nachgehende Entscheidungen</CellItem>
        <CellItem>
          <ul>
            <li
              v-for="(ensuingDecision, index) in documentUnit.ensuingDecisions"
              :key="index"
            >
              {{ ensuingDecision.renderDecision }}
            </li>
          </ul>
        </CellItem>
      </TableRow>
    </TableView>

    <TableView
      v-if="
        documentUnit.contentRelatedIndexing.keywords &&
        documentUnit.contentRelatedIndexing.keywords?.length > 0
      "
    >
      <TableRow>
        <CellItem> Schlagwörter </CellItem>
        <CellItem>
          <ul>
            <li
              v-for="(keyword, index) in documentUnit.contentRelatedIndexing
                .keywords"
              :key="index"
            >
              {{ keyword }}
            </li>
          </ul>
        </CellItem>
      </TableRow>
      <TableRow
        v-if="
          documentUnit.contentRelatedIndexing.fieldsOfLaw &&
          documentUnit.contentRelatedIndexing.fieldsOfLaw?.length > 0
        "
      >
        <CellItem> Sachgebiete </CellItem>
        <CellItem>
          <ul>
            <li
              v-for="(fieldOfLaw, index) in documentUnit.contentRelatedIndexing
                .fieldsOfLaw"
              :key="index"
            >
              {{ fieldOfLaw.identifier }}, {{ fieldOfLaw.text }}
            </li>
          </ul>
        </CellItem>
      </TableRow>

      <TableRow
        v-if="
          documentUnit.contentRelatedIndexing.norms &&
          documentUnit.contentRelatedIndexing.norms?.length > 0
        "
      >
        <CellItem> Normen </CellItem>
        <CellItem>
          <ul>
            <li
              v-for="(norm, index) in documentUnit.contentRelatedIndexing.norms"
              :key="index"
            >
              <ul>
                <li v-for="(singleNorm, i) in norm.singleNorms" :key="i">
                  {{ norm.renderDecision }} - {{ singleNorm.renderDecision }}
                </li>
              </ul>
            </li>
          </ul>
        </CellItem>
      </TableRow>
    </TableView>

    <TableView>
      <TableRow>
        <CellItem>Entscheidungsname</CellItem>
        <TextEditor :value="documentUnit.texts.decisionName"></TextEditor>
      </TableRow>
      <TableRow>
        <CellItem>Titelzeile</CellItem>
        <TextEditor :value="documentUnit.texts.headline"></TextEditor>
      </TableRow>
      <TableRow>
        <CellItem>Leitsatz</CellItem>
        <TextEditor :value="documentUnit.texts.guidingPrinciple"></TextEditor>
      </TableRow>
      <TableRow>
        <CellItem>Orientierungssatz</CellItem>
        <TextEditor :value="documentUnit.texts.headnote"></TextEditor>
      </TableRow>
      <TableRow>
        <CellItem>Tenor</CellItem>
        <TextEditor :value="documentUnit.texts.tenor"></TextEditor>
      </TableRow>
      <TableRow>
        <CellItem>Gründe</CellItem>
        <TextEditor :value="documentUnit.texts.reasons"></TextEditor>
      </TableRow>
      <TableRow>
        <CellItem>Tatbestand</CellItem>
        <TextEditor :value="documentUnit.texts.caseFacts"></TextEditor>
      </TableRow>
      <TableRow>
        <CellItem>Entscheidungsgründe</CellItem>
        <TextEditor :value="documentUnit.texts.decisionReasons"></TextEditor>
      </TableRow>
    </TableView>
  </DocumentUnitWrapper>
</template>
