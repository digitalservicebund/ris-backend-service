<script lang="ts" setup>
import { watch, ref } from "vue"
import DecisionList from "./DecisionList.vue"
import InlineDecision from "./InlineDecision.vue"
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
const proceedingDecisionSearchResults = ref<ProceedingDecision[]>([])
const proceedingDecisionInput = ref<ProceedingDecision>(defaultModel)

function isNotEmpty(decision: ProceedingDecision): boolean {
  return Object.values(decision).some((value) => value !== undefined)
}

const addProceedingDecision = async (
  proceedingDecision: ProceedingDecision
) => {
  if (isNotEmpty(proceedingDecision)) {
    const response = await proceedingDecisionService.addProceedingDecision(
      props.documentUnitUuid,
      proceedingDecision
    )
    if (response.data) {
      // console.log(response.data)
      proceedingDecisionList.value = response.data
    }
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
    console.log(response.error)
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
          <InlineDecision :decision="proceedingDecision" />
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
