<script lang="ts" setup>
import { watch, ref } from "vue"
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
const proceedingDecisionInput = ref<ProceedingDecision>(defaultModel)

const addProceedingDecision = async (
  proceedingDecision: ProceedingDecision
) => {
  if (proceedingDecision !== undefined) {
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
  const response =
    await DocumentUnitService.searchForDocumentUnityByProceedingDecisionInput(
      proceedingDecisionInput.value
    )
  if (response.data) {
    console.log("response:", response.data)
    // TODO
  }
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

    <p
      v-for="decision in proceedingDecisionList"
      :key="decision.uuid"
      class="link-01-bold mb-24 mt-12"
    >
      {{ decision.court?.type }} {{ decision.court?.location }}
      {{ decision.documentType?.label }}
      {{ decision.date }}
      {{ decision.fileNumber }}
    </p>
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
  </ExpandableContent>
</template>
