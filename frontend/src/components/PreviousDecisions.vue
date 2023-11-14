<script lang="ts" setup>
import { h, computed } from "vue"
import { RouterLink } from "vue-router"
import PreviousDecisionInputGroup from "./PreviousDecisionInputGroup.vue"
import EditableList from "@/components/EditableListCaselaw.vue"
import PreviousDecision from "@/domain/previousDecision"
import { withSummarizer } from "@/shared/components/DataSetSummary.vue"
import IconErrorOutline from "~icons/ic/baseline-error-outline"

const props = defineProps<{
  modelValue: PreviousDecision[] | undefined
}>()

const emit = defineEmits<{
  "update:modelValue": [value?: PreviousDecision[]]
}>()

const proceedingDecisions = computed({
  get: () => {
    return props.modelValue
  },
  set: (value) => {
    if (value) emit("update:modelValue", value)
  },
})
const defaultValue = new PreviousDecision()

function decisionSummarizer(dataEntry: PreviousDecision) {
  if (dataEntry.isReadOnly) {
    return h(
      RouterLink,
      {
        class: ["ds-link-01-bold", "underline"],
        target: "_blank",
        tabindex: -1,
        to: {
          name: "caselaw-documentUnit-documentNumber-categories",
          params: { documentNumber: dataEntry.documentNumber },
        },
      },
      () => dataEntry.renderDecision,
    )
  } else if (dataEntry.hasMissingRequiredFields) {
    return h("div", { class: ["flex flex-row items-center"] }, [
      h(
        h(h(IconErrorOutline), {
          "aria-label": "Fehlerhafte Eingabe",
          class: ["mr-8 text-red-800"],
        }),
      ),
      h(
        "div",
        { class: ["ds-label-02-bold text-red-800"] },
        dataEntry.renderDecision,
      ),
    ])
  } else {
    return h("div", { class: ["link-02-reg"] }, dataEntry.renderDecision)
  }
}

const DecisionSummary = withSummarizer(decisionSummarizer)
</script>

<template>
  <h2 id="proceedingDecisions" class="ds-heading-02-reg mb-[1rem]">
    Rechtszug
  </h2>
  <div aria-label="Rechtszug" class="mb-32 bg-white p-16">
    <h2 class="ds-label-02-bold mb-[1rem]">Vorgehende Entscheidung</h2>
    <div class="flex flex-row">
      <div class="flex-1">
        <EditableList
          v-model="proceedingDecisions"
          :default-value="defaultValue"
          :edit-component="PreviousDecisionInputGroup"
          :summary-component="DecisionSummary"
        />
      </div>
    </div>
  </div>
</template>
