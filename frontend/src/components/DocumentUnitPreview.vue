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
    :document-unit="props.documentUnit"
    :show-navigation-panel="showNavigationPanel"
  >
    <TitleElement>Preview</TitleElement>

    <TableView>
      <TableRow>
        <CellItem> Gericht</CellItem>
        <CellItem>
          {{ props.documentUnit.coreData.court?.type }}
        </CellItem>
      </TableRow>
      <TableRow>
        <CellItem> Fehlerhaftes Gericht</CellItem>
        <CellItem>
          {{ props.documentUnit.coreData.deviatingCourts?.toString() }}
        </CellItem>
      </TableRow>
      <TableRow>
        <CellItem> Aktenzeichen</CellItem>
        <CellItem>
          {{ props.documentUnit.coreData.fileNumbers?.toString() }}
        </CellItem>
      </TableRow>
      <TableRow>
        <CellItem> Abweichendes Aktenzeichen</CellItem>
        <CellItem>
          {{ props.documentUnit.coreData.deviatingFileNumbers?.toString() }}
        </CellItem>
      </TableRow>
      <TableRow>
        <CellItem>Entscheidungsdatum</CellItem>
        <CellItem>{{ props.documentUnit.coreData.decisionDate }}</CellItem>
      </TableRow>
      <TableRow>
        <CellItem>Spruchkörper</CellItem>
        <CellItem>{{ props.documentUnit.coreData.appraisalBody }}</CellItem>
      </TableRow>
      <TableRow>
        <CellItem>Dokumenttyp</CellItem>
        <CellItem>{{ props.documentUnit.coreData.documentType }}</CellItem>
      </TableRow>
      <TableRow>
        <CellItem>ECLI</CellItem>
        <CellItem>{{ props.documentUnit.coreData.ecli }}</CellItem>
      </TableRow>
      <TableRow>
        <CellItem>Abweichender ECLI</CellItem>
        <CellItem
          >{{ props.documentUnit.coreData.deviatingEclis?.toString() }}
        </CellItem>
      </TableRow>
      <TableRow>
        <CellItem>Vorgang</CellItem>
        <CellItem>{{ props.documentUnit.coreData.procedure }}</CellItem>
      </TableRow>
      <TableRow>
        <CellItem>Vorgangshistorie</CellItem>
        <CellItem
          >{{
            props.documentUnit.coreData.previousProcedures
              ?.toReversed()
              .toString()
          }}
        </CellItem>
      </TableRow>
      <TableRow>
        <CellItem>Rechtskraft</CellItem>
        <CellItem>{{ props.documentUnit.coreData.legalEffect }}</CellItem>
      </TableRow>
      <TableRow>
        <CellItem>Region</CellItem>
        <CellItem>{{ props.documentUnit.coreData.region }}</CellItem>
      </TableRow>
    </TableView>

    <TableView
      v-if="
        props.documentUnit.previousDecisions ||
        props.documentUnit.ensuingDecisions
      "
    >
      <TableRow
        v-if="
          props.documentUnit.previousDecisions &&
          props.documentUnit.previousDecisions?.length > 0
        "
      >
        <CellItem>Vorinstanz</CellItem>
        <CellItem>
          <ul>
            <li
              v-for="(previousDecision, index) in props.documentUnit
                .previousDecisions"
              :key="index"
            >
              {{ previousDecision.renderDecision }}
            </li>
          </ul>
        </CellItem>
      </TableRow>
      <TableRow
        v-if="
          props.documentUnit.ensuingDecisions &&
          props.documentUnit.ensuingDecisions?.length > 0
        "
      >
        <CellItem>Nachgehende Entscheidungen</CellItem>
        <CellItem>
          <ul>
            <li
              v-for="(ensuingDecision, index) in props.documentUnit
                .ensuingDecisions"
              :key="index"
            >
              {{ ensuingDecision.renderDecision }}
            </li>
          </ul>
        </CellItem>
      </TableRow>
    </TableView>

    <TableView>
      <TableRow>
        <CellItem
          v-if="
            props.documentUnit.contentRelatedIndexing.keywords &&
            props.documentUnit.contentRelatedIndexing.keywords?.length > 0
          "
        >
          Schlagwörter
        </CellItem>
        <CellItem>
          <ul>
            <li
              v-for="(keyword, index) in props.documentUnit
                .contentRelatedIndexing.keywords"
              :key="index"
            >
              {{ keyword }}
            </li>
          </ul>
        </CellItem>
      </TableRow>
      <TableRow>
        <CellItem
          v-if="
            props.documentUnit.contentRelatedIndexing.fieldsOfLaw &&
            props.documentUnit.contentRelatedIndexing.fieldsOfLaw?.length > 0
          "
        >
          Sachgebiete
        </CellItem>
        <CellItem>
          <ul>
            <li
              v-for="(fieldOfLaw, index) in props.documentUnit
                .contentRelatedIndexing.fieldsOfLaw"
              :key="index"
            >
              {{ fieldOfLaw.identifier }}, {{ fieldOfLaw.text }}
            </li>
          </ul>
        </CellItem>
      </TableRow>

      <TableRow>
        <CellItem
          v-if="
            props.documentUnit.contentRelatedIndexing.norms &&
            props.documentUnit.contentRelatedIndexing.norms?.length > 0
          "
        >
          Normen
        </CellItem>
        <CellItem>
          <ul>
            <li
              v-for="(norm, index) in props.documentUnit.contentRelatedIndexing
                .norms"
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
        <TextEditor :value="props.documentUnit.texts.decisionName"></TextEditor>
      </TableRow>
      <TableRow>
        <CellItem>Titelzeile</CellItem>
        <TextEditor :value="props.documentUnit.texts.headline"></TextEditor>
      </TableRow>
      <TableRow>
        <CellItem>Leitsatz</CellItem>
        <TextEditor
          :value="props.documentUnit.texts.guidingPrinciple"
        ></TextEditor>
      </TableRow>
      <TableRow>
        <CellItem>Orientierungssatz</CellItem>
        <TextEditor :value="props.documentUnit.texts.headnote"></TextEditor>
      </TableRow>
      <TableRow>
        <CellItem>Tenor</CellItem>
        <TextEditor :value="props.documentUnit.texts.tenor"></TextEditor>
      </TableRow>
      <TableRow>
        <CellItem>Gründe</CellItem>
        <TextEditor :value="props.documentUnit.texts.reasons"></TextEditor>
      </TableRow>
      <TableRow>
        <CellItem>Tatbestand</CellItem>
        <TextEditor :value="props.documentUnit.texts.caseFacts"></TextEditor>
      </TableRow>
      <TableRow>
        <CellItem>Entscheidungsgründe</CellItem>
        <TextEditor
          :value="props.documentUnit.texts.decisionReasons"
        ></TextEditor>
      </TableRow>
    </TableView>
  </DocumentUnitWrapper>
</template>
