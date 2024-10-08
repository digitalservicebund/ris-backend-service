<script lang="ts" setup>
import * as Sentry from "@sentry/vue"
import { computed, h, ref } from "vue"
import FieldOfLawSelectionList from "./FieldOfLawSelectionList.vue"
import FieldOfLawTree from "./FieldOfLawTree.vue"
import { withSummarizer } from "@/components/DataSetSummary.vue"
import ExpandableDataSet from "@/components/ExpandableDataSet.vue"
import FieldOfLawDirectInputSearch from "@/components/FieldOfLawDirectInputSearch.vue"
import FieldOfLawSearch from "@/components/FieldOfLawSearch.vue"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const showNorms = ref(false)
const selectedNode = ref<FieldOfLaw | undefined>(undefined)

const store = useDocumentUnitStore()
const localModelValue = computed({
  get: () => store.documentUnit!.contentRelatedIndexing.fieldsOfLaw,
  set: (newValues) => {
    store.documentUnit!.contentRelatedIndexing.fieldsOfLaw = newValues?.filter(
      (value) => {
        if (Object.keys(value).length === 0) {
          Sentry.captureMessage(
            "FieldOfLaw list contains empty objects",
            "error",
          )
          return false
        } else {
          return true
        }
      },
    )
  },
})

const addFeldOfLaw = (fieldOfLaw: FieldOfLaw) => {
  if (
    !localModelValue.value?.find(
      (entry) => entry.identifier === fieldOfLaw.identifier,
    )
  ) {
    localModelValue.value?.push(fieldOfLaw)
  }
}

const removeFieldOfLaw = (fieldOfLaw: FieldOfLaw) => {
  localModelValue.value =
    localModelValue.value?.filter(
      (entry) => entry.identifier !== fieldOfLaw.identifier,
    ) ?? []
}

function setSelectedNode(node: FieldOfLaw) {
  selectedNode.value = node
}

function removeSelectedNode() {
  selectedNode.value = undefined
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function selectedFieldsOfLawSummarizer(dataEntry: any) {
  return h("div", [
    h(
      "span",
      {
        class: "text-blue-800",
      },
      dataEntry.identifier,
    ),
    ", " + dataEntry.text,
  ])
}

const SelectedFieldsOfLawSummary = withSummarizer(selectedFieldsOfLawSummarizer)
</script>

<template>
  <div class="border-b-1 border-t-1 border-blue-300 py-24">
    <ExpandableDataSet
      v-if="localModelValue"
      :data-set="localModelValue"
      :summary-component="SelectedFieldsOfLawSummary"
      title="Sachgebiete"
    >
      <div class="w-full">
        <div class="flex flex-row gap-24">
          <div class="flex flex-1 flex-col">
            <FieldOfLawSearch
              @do-show-norms="showNorms = true"
              @linked-field:select="setSelectedNode"
              @node:select="setSelectedNode"
              @node:unselect="removeSelectedNode"
            />
          </div>
          <div class="flex-1">
            <FieldOfLawTree
              v-if="localModelValue"
              v-model="localModelValue"
              :selected-node="selectedNode"
              :show-norms="showNorms"
              @linked-field:select="setSelectedNode"
              @node:select="addFeldOfLaw"
              @node:unselect="removeFieldOfLaw"
              @selected-node:reset="removeSelectedNode"
              @toggle-show-norms="showNorms = !showNorms"
            ></FieldOfLawTree>
          </div>
        </div>
        <hr class="w-full border-blue-700" />
        <div class="bg-white p-20">
          <h1 class="ds-heading-03-reg pb-8">Ausgewählte Sachgebiete</h1>
          <FieldOfLawDirectInputSearch @add-to-list="addFeldOfLaw" />
          <FieldOfLawSelectionList
            v-model="localModelValue"
            @node:remove="removeFieldOfLaw"
            @node:select="setSelectedNode"
          ></FieldOfLawSelectionList>
        </div>
      </div>
    </ExpandableDataSet>
  </div>
</template>
