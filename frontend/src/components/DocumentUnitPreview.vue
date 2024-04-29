<script setup lang="ts">
import TextEditor from "../components/input/TextEditor.vue"
import DocumentUnitWrapper from "@/components/DocumentUnitWrapper.vue"
import PreviewLeftCell from "@/components/preview/PreviewLeftCell.vue"
import PreviewRightCell from "@/components/preview/PreviewRightCell.vue"
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
    <TableView class="w-full">
      <tr v-if="props.documentUnit.coreData.court">
        <PreviewLeftCell>Gericht</PreviewLeftCell>
        <PreviewRightCell>
          {{ props.documentUnit.coreData.court.type }}
          {{ props.documentUnit.coreData.court.location }}
        </PreviewRightCell>
      </tr>
      <tr
        v-if="
          props.documentUnit.coreData.deviatingCourts &&
          props.documentUnit.coreData.deviatingCourts.length > 0
        "
      >
        <PreviewLeftCell>Fehlerhaftes Gericht</PreviewLeftCell>
        <PreviewRightCell>
          {{ props.documentUnit.coreData.deviatingCourts.toString() }}
        </PreviewRightCell>
      </tr>
      <tr
        v-if="
          props.documentUnit.coreData.fileNumbers &&
          props.documentUnit.coreData.fileNumbers.length > 0
        "
      >
        <PreviewLeftCell> Aktenzeichen</PreviewLeftCell>
        <PreviewRightCell>
          {{ props.documentUnit.coreData.fileNumbers.toString() }}
        </PreviewRightCell>
      </tr>
      <tr
        v-if="
          props.documentUnit.coreData.deviatingFileNumbers &&
          props.documentUnit.coreData.deviatingFileNumbers.length > 0
        "
      >
        <PreviewLeftCell> Abweichendes Aktenzeichen</PreviewLeftCell>
        <PreviewRightCell>
          {{ props.documentUnit.coreData.deviatingFileNumbers.toString() }}
        </PreviewRightCell>
      </tr>
      <tr v-if="props.documentUnit.coreData.decisionDate">
        <PreviewLeftCell>Entscheidungsdatum</PreviewLeftCell>
        <PreviewRightCell>{{
          props.documentUnit.coreData.decisionDate
        }}</PreviewRightCell>
      </tr>
      <tr v-if="props.documentUnit.coreData.appraisalBody">
        <PreviewLeftCell>Spruchkörper</PreviewLeftCell>
        <PreviewRightCell>{{
          props.documentUnit.coreData.appraisalBody
        }}</PreviewRightCell>
      </tr>
      <tr v-if="props.documentUnit.coreData.documentType">
        <PreviewLeftCell>Dokumenttyp</PreviewLeftCell>
        <PreviewRightCell>{{
          props.documentUnit.coreData.documentType.label
        }}</PreviewRightCell>
      </tr>
      <tr v-if="props.documentUnit.coreData.ecli">
        <PreviewLeftCell>ECLI</PreviewLeftCell>
        <PreviewRightCell>{{
          props.documentUnit.coreData.ecli
        }}</PreviewRightCell>
      </tr>
      <tr
        v-if="
          props.documentUnit.coreData.deviatingEclis &&
          props.documentUnit.coreData.deviatingEclis.length > 0
        "
      >
        <PreviewLeftCell>Abweichender ECLI</PreviewLeftCell>
        <PreviewRightCell
          >{{ props.documentUnit.coreData.deviatingEclis.toString() }}
        </PreviewRightCell>
      </tr>
      <tr v-if="documentUnit.coreData.procedure">
        <PreviewLeftCell>Vorgang</PreviewLeftCell>
        <PreviewRightCell>{{
          props.documentUnit.coreData.procedure
        }}</PreviewRightCell>
      </tr>
      <tr
        v-if="
          props.documentUnit.coreData.previousProcedures &&
          props.documentUnit.coreData.previousProcedures.length > 0
        "
      >
        <PreviewLeftCell>Vorgangshistorie</PreviewLeftCell>
        <PreviewRightCell
          >{{
            props.documentUnit.coreData.previousProcedures
              ?.toReversed()
              .toString()
          }}
        </PreviewRightCell>
      </tr>
      <tr v-if="props.documentUnit.coreData.legalEffect">
        <PreviewLeftCell>Rechtskraft</PreviewLeftCell>
        <PreviewRightCell>{{
          props.documentUnit.coreData.legalEffect
        }}</PreviewRightCell>
      </tr>
      <tr v-if="props.documentUnit.coreData.region">
        <PreviewLeftCell>Region</PreviewLeftCell>
        <PreviewRightCell>{{
          props.documentUnit.coreData.region
        }}</PreviewRightCell>
      </tr>
    </TableView>

    <TableView
      v-if="
        props.documentUnit.previousDecisions ||
        props.documentUnit.ensuingDecisions
      "
      class="w-full"
    >
      <tr
        v-if="
          props.documentUnit.previousDecisions &&
          props.documentUnit.previousDecisions?.length > 0
        "
        class="w-full"
      >
        <PreviewLeftCell>Vorinstanz</PreviewLeftCell>
        <PreviewRightCell>
          <div
            v-for="(previousDecision, index) in props.documentUnit
              .previousDecisions"
            :key="index"
          >
            {{ previousDecision.renderDecision }}
          </div>
        </PreviewRightCell>
      </tr>
      <tr
        v-if="
          props.documentUnit.ensuingDecisions &&
          props.documentUnit.ensuingDecisions?.length > 0
        "
      >
        <PreviewLeftCell>Nachgehende Entscheidungen</PreviewLeftCell>
        <PreviewRightCell>
          <ul>
            <li
              v-for="(ensuingDecision, index) in props.documentUnit
                .ensuingDecisions"
              :key="index"
            >
              {{ ensuingDecision.renderDecision }}
            </li>
          </ul>
        </PreviewRightCell>
      </tr>
    </TableView>

    <TableView v-if="props.documentUnit.contentRelatedIndexing" class="w-full">
      <tr
        v-if="
          props.documentUnit.contentRelatedIndexing.keywords &&
          props.documentUnit.contentRelatedIndexing.keywords?.length > 0
        "
      >
        <PreviewLeftCell> Schlagwörter </PreviewLeftCell>
        <PreviewRightCell>
          <div
            v-for="(keyword, index) in props.documentUnit.contentRelatedIndexing
              .keywords"
            :key="index"
          >
            {{ keyword }}
          </div>
        </PreviewRightCell>
      </tr>
      <tr
        v-if="
          props.documentUnit.contentRelatedIndexing.fieldsOfLaw &&
          props.documentUnit.contentRelatedIndexing.fieldsOfLaw?.length > 0
        "
      >
        <PreviewLeftCell> Sachgebiete </PreviewLeftCell>
        <PreviewRightCell>
          <div
            v-for="(fieldOfLaw, index) in props.documentUnit
              .contentRelatedIndexing.fieldsOfLaw"
            :key="index"
          >
            {{ fieldOfLaw.identifier }}, {{ fieldOfLaw.text }}
          </div>
        </PreviewRightCell>
      </tr>

      <tr
        v-if="
          props.documentUnit.contentRelatedIndexing.norms &&
          props.documentUnit.contentRelatedIndexing.norms?.length > 0
        "
      >
        <PreviewLeftCell> Normen </PreviewLeftCell>
        <PreviewRightCell>
          <div
            v-for="(norm, index) in props.documentUnit.contentRelatedIndexing
              .norms"
            :key="index"
          >
            <div v-for="(singleNorm, i) in norm.singleNorms" :key="i">
              {{ norm.renderDecision }} - {{ singleNorm.renderDecision }}
            </div>
          </div>
        </PreviewRightCell>
      </tr>
    </TableView>

    <TableView class="w-full">
      <tr>
        <PreviewLeftCell>Entscheidungsname</PreviewLeftCell>
        <PreviewRightCell
          ><TextEditor
            field-size="max"
            :value="props.documentUnit.texts.decisionName"
        /></PreviewRightCell>
      </tr>
      <tr>
        <PreviewLeftCell>Titelzeile</PreviewLeftCell>
        <PreviewRightCell>
          <TextEditor
            field-size="max"
            :value="props.documentUnit.texts.headline"
          />
        </PreviewRightCell>
      </tr>
      <tr>
        <PreviewLeftCell>Leitsatz</PreviewLeftCell>
        <PreviewRightCell
          ><TextEditor
            field-size="max"
            :value="props.documentUnit.texts.guidingPrinciple"
        /></PreviewRightCell>
      </tr>
      <tr>
        <PreviewLeftCell>Orientierungssatz</PreviewLeftCell>
        <PreviewRightCell>
          <TextEditor
            field-size="max"
            :value="props.documentUnit.texts.headnote"
          />
        </PreviewRightCell>
      </tr>
      <tr>
        <PreviewLeftCell>Tenor</PreviewLeftCell>
        <PreviewRightCell>
          <TextEditor
            field-size="max"
            :value="props.documentUnit.texts.tenor"
          />
        </PreviewRightCell>
      </tr>
      <tr>
        <PreviewLeftCell>Gründe</PreviewLeftCell>
        <PreviewRightCell>
          <TextEditor
            field-size="max"
            :value="props.documentUnit.texts.reasons"
          />
        </PreviewRightCell>
      </tr>
      <tr>
        <PreviewLeftCell>Tatbestand</PreviewLeftCell>
        <PreviewRightCell>
          <TextEditor
            field-size="max"
            :value="props.documentUnit.texts.caseFacts"
          />
        </PreviewRightCell>
      </tr>
      <tr>
        <PreviewLeftCell>Entscheidungsgründe</PreviewLeftCell>
        <PreviewRightCell>
          <TextEditor
            field-size="max"
            :value="props.documentUnit.texts.decisionReasons"
          />
        </PreviewRightCell>
      </tr>
    </TableView>
  </DocumentUnitWrapper>
</template>
