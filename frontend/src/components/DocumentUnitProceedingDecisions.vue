<script lang="ts" setup>
import { watch, ref } from "vue"
import ExpandableContent from "@/components/ExpandableContent.vue"
import InputGroup from "@/components/InputGroup.vue"
import TextButton from "@/components/TextButton.vue"
import { proceedingDecisionFields } from "@/domain"
import { ProceedingDecision } from "@/domain/documentUnit"
import ProceedingDecisionService from "@/services/proceedingDecisionService"

const props = defineProps<{
  documentUnitUuid: string
  proceedingDecisions: ProceedingDecision[]
}>()

const proceedingDecisions = ref<ProceedingDecision[]>([])

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
    proceedingDecisions.value = props.proceedingDecisions
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
      :column-count="2"
      :fields="proceedingDecisionFields"
    ></InputGroup>

    <TextButton
      aria-label="Entscheidung manuell hinzufügen"
      class="mt-44"
      label="Manuell Hinzufügen"
      @click="addProceedingDecision()"
    />

    <ModelComponentRepeater
      v-model="values"
      :column-count="2"
      :component="InputGroup"
      :default-value="defaultModel"
      :fields="proceedingDecisionFields"
    >
      <template #removeButton="{ onClick }">
        <TextButton
          aria-label="Entscheidung Entfernen"
          button-type="ghost"
          class="mb-44 mt-6"
          label="Entfernen"
          @click="onClick"
        />
      </template>

      <template #addButton="{ onClick }">
        <TextButton
          aria-label="weitere Entscheidung hinzufügen"
          class="mt-44"
          label="weitere Entscheidung hinzufügen"
          @click="onClick"
        />
      </template>
    </ModelComponentRepeater>
  </ExpandableContent>
</template>
