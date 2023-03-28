<script lang="ts" setup>
import { watch, ref } from "vue"
import DecisionList from "./DecisionList.vue"
import ExpandableContent from "@/components/ExpandableContent.vue"
import InputGroup from "@/components/InputGroup.vue"
import TextButton from "@/components/TextButton.vue"
import { proceedingDecisionFields } from "@/domain"
import { ProceedingDecision } from "@/domain/documentUnit"
import DocumentUnitService from "@/services/documentUnitService"
import ProceedingDecisionService from "@/services/proceedingDecisionService"

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
const proceedingDecisionSearchResults = ref<ProceedingDecision[]>([])
const proceedingDecisionInput = ref<ProceedingDecision>(defaultModel)

function isNotEmpty(decision: ProceedingDecision): boolean {
  return Object.values(decision).some((value) => value !== undefined)
}

const addProceedingDecision = async (
  proceedingDecision: ProceedingDecision
) => {
  if (isNotEmpty(proceedingDecision)) {
    const response = await ProceedingDecisionService.addProceedingDecision(
      props.documentUnitUuid,
      proceedingDecision
    )
    if (response.data) {
      // console.log(response.data)
      proceedingDecisionList.value = response.data
    }
  }
}

const search = async () => {
  // console.log("Searching with input:", proceedingDecisionInput.value)
  const response = await DocumentUnitService.searchByProceedingDecisionInput(
    proceedingDecisionInput.value
  )
  if (response.data) {
    console.log("response:", response.data)
    proceedingDecisionSearchResults.value = response.data
  }
}

const addProceedingDecisionViaSearchResults = async () => {
  // TODO
}

watch(
  props,
  () => {
    // console.log(props.proceedingDecisions)
    proceedingDecisionList.value = props.proceedingDecisions
  },
  {
    immediate: true,
  }
)

const buildSearchResultRowString = (proceedingDecision: ProceedingDecision) => {
  return [
    proceedingDecision.court?.type,
    proceedingDecision.court?.location,
    proceedingDecision.documentType?.label,
    proceedingDecision.date,
    proceedingDecision.fileNumber,
    proceedingDecision.documentNumber,
  ]
    .filter((v) => v !== undefined)
    .join(", ")
}
</script>

<template>
  <ExpandableContent>
    <template #header>
      <h1 class="heading-02-regular mb-[1rem]">Vorgehende Entscheidungen</h1>
    </template>

    <DecisionList
      v-if="proceedingDecisionList"
      :decisions="proceedingDecisionList"
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
      @click="addProceedingDecision(proceedingDecisionInput)"
    />

    <div v-if="proceedingDecisionSearchResults.length > 0" class="mb-10 mt-20">
      <strong
        >Suche hat {{ proceedingDecisionSearchResults.length }} Treffer
        ergeben</strong
      >
    </div>
    <div class="table">
      <div
        v-for="proceedingDecision in proceedingDecisionSearchResults"
        :key="proceedingDecision.uuid"
        class="link-01-bold mb-24 mt-12 table-row underline"
      >
        <div class="table-cell">
          {{ buildSearchResultRowString(proceedingDecision) }}
        </div>
        <div class="p-8 table-cell">
          <TextButton
            aria-label="Treffer übernehmen"
            label="Übernehmen"
            @click="addProceedingDecisionViaSearchResults"
          />
        </div>
      </div>
    </div>
  </ExpandableContent>
</template>
