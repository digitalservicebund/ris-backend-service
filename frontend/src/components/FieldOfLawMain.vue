<script lang="ts" setup>
import * as Sentry from "@sentry/vue"
import { computed, ref, useTemplateRef } from "vue"
import ExpandableFieldOfLawList from "@/components/FieldOfLawExpandableContainer.vue"
import FieldOfLawSearchInput from "@/components/FieldOfLawSearchInput.vue"
import FieldOfLawSearchResultList from "@/components/FieldOfLawSearchResultList.vue"
import FieldOfLawTree from "@/components/FieldOfLawTree.vue"
import { Page } from "@/components/Pagination.vue"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import service from "@/services/fieldOfLawService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

type FieldOfLawTreeType = InstanceType<typeof FieldOfLawTree>
const treeRef = useTemplateRef<FieldOfLawTreeType>("treeRef")

const showNorms = ref(false)
const selectedNode = ref<FieldOfLaw | undefined>(undefined)

const description = ref("")
const identifier = ref("")
const norm = ref("")
const results = ref<FieldOfLaw[]>()
const currentPage = ref<Page<FieldOfLaw>>()
const itemsPerPage = 10

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

async function submitSearch(page: number) {
  // if (StringsUtil.isEmpty(searchStr.value)) {
  //   return removeSelectedNode()
  // }

  const response = await service.searchForFieldsOfLaw(
    page,
    itemsPerPage,
    description.value,
    identifier.value,
    norm.value,
  )
  if (response.data) {
    currentPage.value = response.data
    results.value = response.data.content

    if (results.value?.[0]) {
      selectedNode.value = results.value[0]
    }
    showNorms.value = !!norm.value
  } else {
    currentPage.value = undefined
    results.value = undefined
    console.error("Error searching for Nodes")
  }
}

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

function updateIdentifierSearchTerm(newValue?: string) {
  identifier.value = newValue ? newValue : ""
}

function updateDescriptionSearchTerm(newValue?: string) {
  description.value = newValue ? newValue : ""
}

function updateNormSearchTerm(newValue?: string) {
  norm.value = newValue ? newValue : ""
}

function addFromList(fieldOfLaw: FieldOfLaw) {
  addFieldOfLaw(fieldOfLaw)
  setSelectedNode(fieldOfLaw)
}

function resetSearch() {
  // reset search params
  identifier.value = ""
  description.value = ""
  norm.value = ""
  // reset search results list
  currentPage.value = undefined
  results.value = undefined
  // reset tree
  selectedNode.value = undefined
  showNorms.value = false
  treeRef.value?.collapseTree()
}
</script>

<template>
  <ExpandableFieldOfLawList
    v-if="localModelValue"
    :data-set="localModelValue"
    @editing-done="resetSearch"
    @node:remove="removeFieldOfLaw"
    @node:select="setSelectedNode"
  >
    <FieldOfLawSearchInput
      :description="description"
      :identifier="identifier"
      :norm="norm"
      @search="submitSearch(0)"
      @update:description="updateDescriptionSearchTerm"
      @update:identifier="updateIdentifierSearchTerm"
      @update:norm="updateNormSearchTerm"
    />

    <div class="flex w-full flex-row gap-24">
      <FieldOfLawSearchResultList
        :current-page="currentPage"
        :results="results"
        @linked-field:select="setSelectedNode"
        @node:select="addFromList"
        @search="submitSearch"
      />

      <FieldOfLawTree
        v-if="localModelValue"
        ref="treeRef"
        v-model="localModelValue"
        :search-results="results"
        :selected-node="selectedNode"
        :show-norms="showNorms"
        @linked-field:select="setSelectedNode"
        @node:select="addFieldOfLaw"
        @node:unselect="removeFieldOfLaw"
        @selected-node:reset="removeSelectedNode"
        @toggle-show-norms="showNorms = !showNorms"
      />
    </div>
  </ExpandableFieldOfLawList>
</template>
