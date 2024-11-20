<script lang="ts" setup>
import * as Sentry from "@sentry/vue"
import { computed, nextTick, ref, useTemplateRef } from "vue"
import FieldOfLawExpandableContainer from "@/components/field-of-law/FieldOfLawExpandableContainer.vue"
import FieldOfLawSearchInput from "@/components/field-of-law/FieldOfLawSearchInput.vue"
import FieldOfLawSearchResultList from "@/components/field-of-law/FieldOfLawSearchResults.vue"
import FieldOfLawTree from "@/components/field-of-law/FieldOfLawTree.vue"
import { Page } from "@/components/Pagination.vue"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import service from "@/services/fieldOfLawService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import StringsUtil from "@/utils/stringsUtil"

type FieldOfLawTreeType = InstanceType<typeof FieldOfLawTree>
const treeRef = useTemplateRef<FieldOfLawTreeType>("treeRef")

const showNorms = ref(false)
const nodeOfInterest = ref<FieldOfLaw | undefined>(undefined)

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
  if (
    StringsUtil.isEmpty(identifier.value) &&
    StringsUtil.isEmpty(description.value) &&
    StringsUtil.isEmpty(norm.value)
  ) {
    removeNodeOfInterest()
  }

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
      nodeOfInterest.value = results.value[0]
    }
    showNorms.value = !!norm.value
  } else {
    currentPage.value = undefined
    results.value = undefined
    console.error("Error searching for Nodes")
  }
}

const addFieldOfLaw = async (fieldOfLaw: FieldOfLaw) => {
  if (
    !localModelValue.value?.find(
      (entry) => entry.identifier === fieldOfLaw.identifier,
    )
  ) {
    localModelValue.value?.push(fieldOfLaw)
  }
  await setScrollPosition()
}

const setScrollPosition = async () => {
  const container = document.documentElement // Replace with specific scrollable container if needed

  await nextTick(() => {
    // Get all elements with the class 'field-of-law'
    const fieldOfLawElements = document.querySelectorAll(
      ".field-of-law",
    ) as NodeListOf<HTMLElement>

    if (fieldOfLawElements.length > 0) {
      // Get the height of the last added field-of-law element
      const lastItem = fieldOfLawElements[fieldOfLawElements.length - 1]
      if (lastItem) {
        const addedHeight = lastItem.getBoundingClientRect().height

        // Adjust the scroll position by the height of the last item
        container.scrollTop += addedHeight
      }
    }
  })
}

const removeFieldOfLaw = (fieldOfLaw: FieldOfLaw) => {
  localModelValue.value =
    localModelValue.value?.filter(
      (entry) => entry.identifier !== fieldOfLaw.identifier,
    ) ?? []
}

function setNodeOfInterest(node: FieldOfLaw) {
  nodeOfInterest.value = node
}

function removeNodeOfInterest() {
  nodeOfInterest.value = undefined
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

async function addFromList(fieldOfLaw: FieldOfLaw) {
  await addFieldOfLaw(fieldOfLaw)
  setNodeOfInterest(fieldOfLaw)
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
  nodeOfInterest.value = undefined
  showNorms.value = false
  treeRef.value?.collapseTree()
}
</script>

<template>
  <FieldOfLawExpandableContainer
    v-if="localModelValue"
    :fields-of-law="localModelValue"
    @editing-done="resetSearch"
    @node:clicked="setNodeOfInterest"
    @node:remove="removeFieldOfLaw"
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
        @linked-field:clicked="setNodeOfInterest"
        @node:add="addFromList"
        @search="submitSearch"
      />

      <FieldOfLawTree
        v-if="localModelValue"
        ref="treeRef"
        v-model="localModelValue"
        :node-of-interest="nodeOfInterest"
        :search-results="results"
        :show-norms="showNorms"
        @linked-field:select="setNodeOfInterest"
        @node-of-interest:reset="removeNodeOfInterest"
        @node:select="addFieldOfLaw"
        @node:unselect="removeFieldOfLaw"
        @toggle-show-norms="showNorms = !showNorms"
      />
    </div>
  </FieldOfLawExpandableContainer>
</template>
