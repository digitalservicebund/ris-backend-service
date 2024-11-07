<script lang="ts" setup>
import * as Sentry from "@sentry/vue"
import { computed, h, ref } from "vue"
import { withSummarizer } from "@/components/DataSetSummary.vue"
import ExpandableDataSet from "@/components/ExpandableDataSet.vue"
import FieldOfLawSearch from "@/components/FieldOfLawSearch.vue"
import FieldOfLawTree from "@/components/FieldOfLawTree.vue"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const showNorms = ref(false)
const selectedNode = ref<FieldOfLaw | undefined>(undefined)
const searchResultList = ref<FieldOfLaw[] | undefined>(undefined)

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

const addFieldOfLaw = (fieldOfLaw: FieldOfLaw) => {
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

function setSearchResults(searchResults: FieldOfLaw[]) {
  searchResultList.value = searchResults
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
              @search-results="setSearchResults"
            />
          </div>
          <div class="flex-1">
            <FieldOfLawTree
              v-if="localModelValue"
              v-model="localModelValue"
              :search-results="searchResultList"
              :selected-node="selectedNode"
              :show-norms="showNorms"
              @linked-field:select="setSelectedNode"
              @node:select="addFieldOfLaw"
              @node:unselect="removeFieldOfLaw"
              @selected-node:reset="removeSelectedNode"
              @toggle-show-norms="showNorms = !showNorms"
            ></FieldOfLawTree>
          </div>
        </div>
      </div>
    </ExpandableDataSet>
  </div>
</template>
