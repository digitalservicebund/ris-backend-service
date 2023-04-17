<script lang="ts" setup>
import { h, ref } from "vue"
import FieldOfLawSelectionList from "./FieldOfLawSelectionList.vue"
import FieldOfLawTree from "./FieldOfLawTree.vue"
import ExpandableDataSet from "@/components/ExpandableDataSet.vue"
import FieldOfLawDirectInputSearch from "@/components/FieldOfLawDirectInputSearch.vue"
import FieldOfLawSearch from "@/components/FieldOfLawSearch.vue"
import { FieldOfLawNode } from "@/domain/fieldOfLaw"
import FieldOfLawService from "@/services/fieldOfLawService"
import { withSummarizer } from "@/shared/components/DataSetSummary.vue"

const props = defineProps<{
  documentUnitUuid: string
}>()

const selectedFieldsOfLaw = ref<FieldOfLawNode[]>([])
const clickedIdentifier = ref("")
const showNorms = ref(false)

const response = await FieldOfLawService.getSelectedFieldsOfLaw(
  props.documentUnitUuid
)

if (response.data) {
  selectedFieldsOfLaw.value = response.data
}

const handleAdd = async (identifier: string) => {
  const response = await FieldOfLawService.addFieldOfLaw(
    props.documentUnitUuid,
    identifier
  )

  if (response.data) {
    selectedFieldsOfLaw.value = response.data
  }
}

const handleRemoveByIdentifier = async (identifier: string) => {
  const response = await FieldOfLawService.removeFieldOfLaw(
    props.documentUnitUuid,
    identifier
  )

  if (response.data) {
    selectedFieldsOfLaw.value = response.data
  }
}

function handleNodeClicked(node: FieldOfLawNode) {
  clickedIdentifier.value = node.identifier
}

function handleResetClickedNode() {
  clickedIdentifier.value = ""
}

function handleLinkedFieldClicked(identifier: string) {
  clickedIdentifier.value = identifier
}

function selectedFieldsOfLawSummarizer(dataEntry: undefined) {
  return h("div", renderDecision(dataEntry))
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function renderDecision(dataEntry: any): string {
  return [
    ...(dataEntry.identifier ? [dataEntry.identifier] : []),
    ...(dataEntry.text ? [dataEntry.text] : []),
  ].join(", ")
}

const SelectedFieldsOfLawSummary = withSummarizer(selectedFieldsOfLawSummarizer)
</script>

<template>
  <ExpandableDataSet
    :data-set="selectedFieldsOfLaw"
    :summary-component="SelectedFieldsOfLawSummary"
    title="Sachgebiete"
  >
    <div class="w-full">
      <div class="flex flex-row">
        <div class="bg-white flex flex-1 flex-col p-20">
          <FieldOfLawSearch
            :show-norms="showNorms"
            @do-show-norms="showNorms = true"
            @node-clicked="handleNodeClicked"
          />
        </div>
        <div class="bg-white flex-1 p-20">
          <FieldOfLawTree
            :clicked-identifier="clickedIdentifier"
            :selected-nodes="selectedFieldsOfLaw"
            :show-norms="showNorms"
            @add-to-list="handleAdd"
            @linked-field:clicked="handleLinkedFieldClicked"
            @remove-from-list="handleRemoveByIdentifier"
            @reset-clicked-node="handleResetClickedNode"
            @toggle-show-norms="showNorms = !showNorms"
          ></FieldOfLawTree>
        </div>
      </div>
      <hr class="border-blue-700 w-full" />
      <div class="bg-white p-20">
        <h1 class="heading-03-regular pb-8">Ausgewählte Sachgebiete</h1>
        <FieldOfLawDirectInputSearch @add-to-list="handleAdd" />
        <FieldOfLawSelectionList
          :selected-fields-of-law="selectedFieldsOfLaw"
          @linked-field:clicked="handleLinkedFieldClicked"
          @node-clicked="handleNodeClicked"
          @remove-from-list="handleRemoveByIdentifier"
        ></FieldOfLawSelectionList>
      </div>
    </div>
  </ExpandableDataSet>
</template>
