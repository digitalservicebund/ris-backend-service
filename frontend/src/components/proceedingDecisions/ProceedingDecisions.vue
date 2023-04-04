<script lang="ts" setup>
import { watch, ref } from "vue"
import DecisionList from "./DecisionList.vue"
import SearchResultList, { SearchResults } from "./SearchResultList.vue"
import ExpandableContent from "@/components/ExpandableContent.vue"
import { ProceedingDecision } from "@/domain/documentUnit"
import { proceedingDecisionFields } from "@/fields/caselaw"
import DocumentUnitService from "@/services/documentUnitService"
import proceedingDecisionService from "@/services/proceedingDecisionService"
import InputGroup from "@/shared/components/input/InputGroup.vue"
import TextButton from "@/shared/components/input/TextButton.vue"

const props = defineProps<{
  documentUnitUuid: string
  proceedingDecisions?: ProceedingDecision[]
}>()

const defaultModel: ProceedingDecision = {
  court: undefined,
  documentType: undefined,
  date: undefined,
  fileNumber: undefined,
}

const proceedingDecisionList = ref<ProceedingDecision[]>()
const searchResults = ref<SearchResults>()
const proceedingDecisionInput = ref<ProceedingDecision>(defaultModel)

function isNotEmpty(decision: ProceedingDecision): boolean {
  return Object.values(decision).some((value) => value !== undefined)
}

async function createProceedingDecision(
  proceedingDecision: ProceedingDecision
) {
  if (isNotEmpty(proceedingDecision)) {
    const response = await proceedingDecisionService.createProceedingDecision(
      props.documentUnitUuid,
      proceedingDecision
    )
    if (response.data) {
      proceedingDecisionList.value = response.data
    } else {
      console.error(response.error)
    }
  }
}

async function linkProceedingDecision(childUuid: string) {
  const response = await proceedingDecisionService.linkProceedingDecision(
    props.documentUnitUuid,
    childUuid
  )
  if (response.data) {
    proceedingDecisionList.value = response.data

    updateSearchResultsLinkStatus(childUuid)
  } else {
    console.error(response.error)
  }
}

async function removeProceedingDecision(decision: ProceedingDecision) {
  const response = await proceedingDecisionService.removeProceedingDecision(
    props.documentUnitUuid,
    decision.uuid as string
  )
  if (response.data) {
    proceedingDecisionList.value = proceedingDecisionList.value?.filter(
      (listItem) => listItem.uuid !== decision.uuid
    )
  } else {
    console.error(response.error)
  }
}

function isLinked(decision: ProceedingDecision): boolean {
  if (!proceedingDecisionList.value) return false

  return proceedingDecisionList.value.some(
    (linkedDecision) => linkedDecision.uuid == decision.uuid
  )
}

function updateSearchResultsLinkStatus(linkedUuid: string) {
  if (searchResults.value == undefined) return

  searchResults.value = searchResults.value.map((searchResult) => {
    if (searchResult.decision.uuid === linkedUuid) {
      searchResult.isLinked = true
    }
    return searchResult
  })
}

async function search() {
  const response = await DocumentUnitService.searchByProceedingDecisionInput(
    proceedingDecisionInput.value
  )
  if (response.data) {
    searchResults.value = response.data.map((searchResult) => {
      return {
        decision: searchResult,
        isLinked: isLinked(searchResult),
      }
    })
  }
}

watch(
  props,
  () => {
    proceedingDecisionList.value = props.proceedingDecisions
  },
  {
    immediate: true,
  }
)
</script>

<template>
  <ExpandableContent>
    <template #header>
      <h1 class="heading-02-regular mb-[1rem]">Vorgehende Entscheidungen</h1>
    </template>

    <DecisionList
      v-if="proceedingDecisionList"
      :decisions="proceedingDecisionList"
      @remove-link="removeProceedingDecision"
    />

    <InputGroup
      v-model="proceedingDecisionInput"
      :column-count="2"
      :fields="proceedingDecisionFields"
    ></InputGroup>

    <TextButton
      aria-label="Nach Entscheidungen suchen"
      class="mr-28"
      label="Suchen"
      @click="search"
    />

    <TextButton
      aria-label="Entscheidung manuell hinzufügen"
      label="Manuell Hinzufügen"
      @click="createProceedingDecision(proceedingDecisionInput)"
    />

    <div v-if="searchResults" class="mb-10 mt-20">
      <SearchResultList
        :search-results="searchResults"
        @link-decision="linkProceedingDecision"
      />
    </div>
  </ExpandableContent>
</template>
