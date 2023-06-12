<script lang="ts" setup>
import { h, watch, ref } from "vue"
import { RouterLink } from "vue-router"
import ComboboxInput from "../ComboboxInput.vue"
import DecisionList from "./DecisionList.vue"
import SearchResultList, { SearchResults } from "./SearchResultList.vue"
import ExpandableDataSet from "@/components/ExpandableDataSet.vue"
import ProceedingDecision from "@/domain/proceedingDecision"
import comboboxItemService from "@/services/comboboxItemService"
import documentUnitService from "@/services/documentUnitService"
import proceedingDecisionService from "@/services/proceedingDecisionService"
import { withSummarizer } from "@/shared/components/DataSetSummary.vue"
import CheckboxInput from "@/shared/components/input/CheckboxInput.vue"
import DateInput from "@/shared/components/input/DateInput.vue"
import InputField, {
  LabelPosition,
} from "@/shared/components/input/InputField.vue"
import TextButton from "@/shared/components/input/TextButton.vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import Pagination, { Page } from "@/shared/components/Pagination.vue"

const props = defineProps<{
  documentUnitUuid: string
  proceedingDecisions?: ProceedingDecision[]
}>()

const proceedingDecisions = ref<ProceedingDecision[]>()

const searchResults = ref<SearchResults>()
const searchResultsCurrentPage = ref<Page<ProceedingDecision>>()
const searchResultsPerPage = 30
const input = ref<ProceedingDecision>(new ProceedingDecision())

function isNotEmpty({
  court,
  decisionDate,
  fileNumber,
  documentType,
}: ProceedingDecision): boolean {
  return [court, decisionDate, fileNumber, documentType].some(
    (property) => property != undefined
  )
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
  input.value = new ProceedingDecision()
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

async function search(page = 0) {
  const response = await documentUnitService.searchByProceedingDecisionInput(
    page,
    searchResultsPerPage,
    input.value as ProceedingDecision
  )
  if (response.data) {
    searchResultsCurrentPage.value = response.data
    searchResults.value = response.data.content.map((searchResult) => {
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

watch(
  input,
  () => {
    if (!input.value.dateKnown) input.value.decisionDate = undefined
  },
  {
    immediate: true,
    deep: true,
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

      <div class="fake-input-group">
        <div class="fake-input-group__row pb-32">
          <InputField
            id="court"
            class="fake-input-group__row__field flex-col"
            label="Gericht *"
          >
            <ComboboxInput
              id="court"
              v-model="input.court"
              aria-label="Gericht Rechtszug"
              :item-service="comboboxItemService.getCourts"
              placeholder="Gerichtstyp Gerichtsort"
            ></ComboboxInput>
          </InputField>

          <div class="fake-input-group__row__field flex-col">
            <InputField id="date" class="w-full" label="Entscheidungsdatum *">
              <DateInput
                id="decisionDate"
                v-model="input.decisionDate"
                aria-label="Entscheidungsdatum Rechtszug"
                :disabled="input.dateUnknown"
              ></DateInput>
            </InputField>
            <InputField
              id="dateUnknown"
              label="Datum unbekannt"
              :label-position="LabelPosition.RIGHT"
            >
              <CheckboxInput
                id="dateUnknown"
                v-model="input.dateUnknown"
                aria-label="Datum Unbekannt"
              ></CheckboxInput>
            </InputField>
          </div>
        </div>

        <div class="fake-input-group__row pb-32">
          <InputField
            id="fileNumber"
            class="fake-input-group__row__field flex-col"
            label="Aktenzeichen *"
          >
            <TextInput
              id="fileNumber"
              v-model="input.fileNumber"
              aria-label="Aktenzeichen Rechtszug"
            ></TextInput>
          </InputField>

          <InputField
            id="documentType"
            class="fake-input-group__row__field flex-col"
            label="Dokumenttyp"
          >
            <ComboboxInput
              id="documentType"
              v-model="input.documentType"
              aria-label="Dokumenttyp Rechtszug"
              :item-service="comboboxItemService.getDocumentTypes"
              placeholder="Bitte auswählen"
            ></ComboboxInput>
          </InputField>
        </div>
      </div>

      <div>
        <TextButton
          aria-label="Nach Entscheidungen suchen"
          button-type="secondary"
          class="mr-28"
          label="Suchen"
          @click="search(0)"
        />

        <TextButton
          aria-label="Entscheidung manuell hinzufügen"
          button-type="tertiary"
          label="Manuell Hinzufügen"
          @click="createProceedingDecision(input as ProceedingDecision)"
        />
      </div>

      <div v-if="searchResultsCurrentPage" class="mb-10 mt-20">
        <Pagination
          navigation-position="bottom"
          :page="searchResultsCurrentPage"
          @update-page="search"
        >
          <SearchResultList
            :search-results="searchResults"
            @link-decision="linkProceedingDecision"
          />
        </Pagination>
      </div>
    </ExpandableDataSet>
  </div>
</template>

<style lang="scss" scoped>
.fake-input-group {
  display: flex;
  width: 100%;
  flex-direction: column;

  &__row {
    display: flex;
    width: 100%;
    flex-wrap: wrap;
    gap: 2rem;

    &__field {
      display: flex;
      width: calc((100% - 2rem) / 2);
      min-width: 15rem;
      align-items: start;
    }
  }
}
</style>
