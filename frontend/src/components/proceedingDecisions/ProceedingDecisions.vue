<script lang="ts" setup>
import { h, watch, ref } from "vue"
import { RouterLink } from "vue-router"
import DecisionList from "./DecisionList.vue"
import SearchResultList, { SearchResults } from "./SearchResultList.vue"
import ExpandableDataSet from "@/components/ExpandableDataSet.vue"
import { ProceedingDecision } from "@/domain/proceedingDecision"
import { proceedingDecisionFields } from "@/fields/caselaw"
import DocumentUnitService from "@/services/documentUnitService"
import proceedingDecisionService from "@/services/proceedingDecisionService"
import { withSummarizer } from "@/shared/components/DataSetSummary.vue"
import InputGroup from "@/shared/components/input/InputGroup.vue"
import TextButton from "@/shared/components/input/TextButton.vue"

const props = defineProps<{
  documentUnitUuid: string
  proceedingDecisions?: ProceedingDecision[]
}>()

const proceedingDecisions = ref<ProceedingDecision[]>()
const searchResults = ref<SearchResults>()
const input = ref<ProceedingDecision>(new ProceedingDecision({}))

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
      proceedingDecisions.value = response.data
    } else {
      console.error(response.error)
    }
  }
  resetInput()
}

function resetInput() {
  input.value = new ProceedingDecision({})
}

async function linkProceedingDecision(childUuid: string) {
  const response = await proceedingDecisionService.linkProceedingDecision(
    props.documentUnitUuid,
    childUuid
  )
  if (response.data) {
    proceedingDecisions.value = response.data

    updateSearchResultsLinkStatus(childUuid)
  } else {
    console.error(response.error)
  }
  resetInput()
}

async function removeProceedingDecision(decision: ProceedingDecision) {
  const response = await proceedingDecisionService.removeProceedingDecision(
    props.documentUnitUuid,
    decision.uuid as string
  )
  if (response.data) {
    proceedingDecisions.value = proceedingDecisions.value?.filter(
      (listItem) => listItem.uuid !== decision.uuid
    )
    updateSearchResultsLinkStatus(decision.uuid as string)
  } else {
    console.error(response.error)
  }
}

function isLinked(decision: ProceedingDecision): boolean {
  if (!proceedingDecisions.value) return false

  return proceedingDecisions.value.some(
    (proceedingDecision) => proceedingDecision.uuid == decision.uuid
  )
}

function updateSearchResultsLinkStatus(uuid: string) {
  if (searchResults.value == undefined) return

  searchResults.value = searchResults.value.map((searchResult) => {
    if (searchResult.decision.uuid === uuid) {
      searchResult.isLinked = !searchResult.isLinked
    }
    return searchResult
  })
}

async function search() {
  const response = await DocumentUnitService.searchByProceedingDecisionInput(
    input.value as ProceedingDecision
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

function decisionSummarizer(dataEntry: ProceedingDecision) {
  return h("div", { tabindex: dataEntry.hasLink ? 0 : -1 }, [
    dataEntry.hasLink
      ? h(
          RouterLink,
          {
            class: ["link-01-bold", "underline"],
            target: "_blank",
            tabindex: -1,
            to: {
              name: "caselaw-documentUnit-:documentNumber-categories",
              params: { documentNumber: dataEntry.documentNumber },
            },
          },
          dataEntry.renderDecision
        )
      : h("span", { class: ["link-02-reg"] }, dataEntry.renderDecision),
  ])
}

const DecisionSummary = withSummarizer(decisionSummarizer)

watch(
  props,
  () => {
    proceedingDecisions.value = props.proceedingDecisions
  },
  {
    immediate: true,
  }
)
</script>

<template>
  <div class="mb-[4rem]">
    <h1 class="heading-02-regular mb-[1rem]">Rechtszug</h1>
    <ExpandableDataSet
      as-column
      :data-set="proceedingDecisions"
      fallback-text="Noch keine vorhergehende Entscheidung hinzugefügt."
      :summary-component="DecisionSummary"
      title="Vorgehende Entscheidungen"
    >
      <DecisionList
        v-if="proceedingDecisions"
        :decisions="proceedingDecisions"
        @remove-link="removeProceedingDecision"
      />

      <InputGroup
        v-model="input"
        :column-count="2"
        :fields="proceedingDecisionFields"
      ></InputGroup>

      <div>
        <TextButton
          aria-label="Nach Entscheidungen suchen"
          button-type="secondary"
          class="mr-28"
          label="Suchen"
          @click="search"
        />

        <TextButton
          aria-label="Entscheidung manuell hinzufügen"
          button-type="tertiary"
          label="Manuell Hinzufügen"
          @click="createProceedingDecision(input as ProceedingDecision)"
        />
      </div>

      <div class="mb-10 mt-20">
        <SearchResultList
          :search-results="searchResults"
          @link-decision="linkProceedingDecision"
        />
      </div>
    </ExpandableDataSet>
  </div>
</template>
