<script lang="ts" setup>
import { h, computed } from "vue"
import EnsuingDecisionInputGroup from "./EnsuingDecisionInputGroup.vue"
import EditableList from "@/components/EditableListCaselaw.vue"
import EnsuingDecision from "@/domain/ensuingDecision"
import { withSummarizer } from "@/shared/components/DataSetSummary.vue"

const props = defineProps<{
  modelValue: EnsuingDecision[] | undefined
}>()

const emit = defineEmits<{
  "update:modelValue": [value?: EnsuingDecision[]]
}>()

const ensuingDecisions = computed({
  get: () => {
    return props.modelValue
  },
  set: (value) => {
    if (value) emit("update:modelValue", value)
  },
})
const defaultValue = new EnsuingDecision()

function decisionSummarizer(dataEntry: EnsuingDecision) {
  if (
    !dataEntry.hasMissingRequiredFields ||
    (dataEntry.missingRequiredFields.length === 1 &&
      dataEntry.missingRequiredFields[0] === "decisionDate" &&
      dataEntry.isPending)
  ) {
    return h("div", { class: ["link-02-reg"] }, dataEntry.renderDecision)
  } else {
    return h("div", { class: ["flex flex-row items-center"] }, [
      h(
        "span",
        {
          "aria-label": "Fehlerhafte Eingabe",
          class: ["material-icons pr-8 text-red-800"],
        },
        "error_outline",
      ),
      h(
        "div",
        { class: ["ds-label-02-bold text-red-800"] },
        dataEntry.renderDecision,
      ),
    ])
  }
}

const DecisionSummary = withSummarizer(decisionSummarizer)
</script>

<template>
  <div aria-label="Rechtszug" class="mb-32 bg-white p-16">
    <h2 class="ds-label-02-bold mb-[1rem]">Nachgehende Entscheidung</h2>
    <div class="flex flex-row">
      <div class="flex-1">
        <EditableList
          v-model="ensuingDecisions"
          :default-value="defaultValue"
          :edit-component="EnsuingDecisionInputGroup"
          :summary-component="DecisionSummary"
        />
      </div>
    </div>
  </div>
</template>
