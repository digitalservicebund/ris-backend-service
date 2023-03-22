<script lang="ts" setup>
import { watch, ref, computed } from "vue"
import ExpandableContent from "@/components/ExpandableContent.vue"
import InputGroup from "@/components/InputGroup.vue"
import TextButton from "@/components/TextButton.vue"
import { proceedingDecisionFields } from "@/domain"
import { ProceedingDecision } from "@/domain/documentUnit"
import ProceedingDecisionService from "@/services/proceedingDecisionService"

const props = defineProps<{
  documentUnitUuid: string
  proceedingDecisions: ProceedingDecision[] | undefined
}>()

const defaultModel: ProceedingDecision = {
  court: undefined,
  documentType: undefined,
  date: undefined,
  fileNumber: undefined,
}

const values = ref<ProceedingDecision>(defaultModel)

const addProceedingDecision = async (
  proceedingDecision: ProceedingDecision
) => {
  if (proceedingDecision !== undefined) {
    const response = await ProceedingDecisionService.addProceedingDecision(
      props.documentUnitUuid,
      proceedingDecision
    )
    if (response.data) {
      console.log(response.data)
    }
  }
}

watch(
  props,
  () => {
    console.log(props.proceedingDecisions)
    // proceedingDecisions.value = props.proceedingDecisions
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

    <InputGroup
      v-model="values"
      :column-count="2"
      :fields="proceedingDecisionFields"
    ></InputGroup>

    <TextButton
      aria-label="Entscheidung manuell hinzufügen"
      class="mt-44"
      label="Manuell Hinzufügen"
      @click="addProceedingDecision(values)"
    />
  </ExpandableContent>
</template>
