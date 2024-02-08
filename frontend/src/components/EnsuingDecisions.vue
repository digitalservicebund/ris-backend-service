<script lang="ts" setup>
import { h, computed } from "vue"
import { RouterLink } from "vue-router"
import EnsuingDecisionInputGroup from "./EnsuingDecisionInputGroup.vue"
import { withSummarizer } from "@/components/DataSetSummary.vue"
import EditableList from "@/components/EditableListCaselaw.vue"
import EnsuingDecision from "@/domain/ensuingDecision"
import BaselineArrowOutward from "~icons/ic/baseline-arrow-outward"
import IconBaselineDescription from "~icons/ic/baseline-description"
import IconErrorOutline from "~icons/ic/baseline-error-outline"
import IconOutlineDescription from "~icons/ic/outline-description"

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
    emit("update:modelValue", value)
  },
})
const defaultValue = new EnsuingDecision()

function renderLink(dataEntry: EnsuingDecision) {
  return h(
    RouterLink,
    {
      class: [
        "ds-link-03-bold ml-8 border-b-2 border-blue-800 flex flex-row leading-24 focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800",
      ],
      target: "_blank",
      tabindex: 0,
      onClick: (e: Event) => {
        e.stopPropagation()
      },
      onKeydown: (e: KeyboardEvent) => {
        e.stopPropagation()
      },
      to: {
        name: "caselaw-documentUnit-documentNumber-categories",
        params: { documentNumber: dataEntry.documentNumber },
      },
    },
    () =>
      h(
        "div",
        {
          class: ["flex flex-row items-center"],
        },
        [h(() => dataEntry.documentNumber), h(() => h(BaselineArrowOutward))],
      ),
  )
}

function decisionSummarizer(dataEntry: EnsuingDecision) {
  // Linked DocUnit
  if (dataEntry.hasForeignSource) {
    return h("div", { class: ["flex flex-row items-center"] }, [
      h(h(IconBaselineDescription), {
        class: ["mr-8 "],
      }),
      h("div", { class: ["flex flex-row items-baseline"] }, [
        h("div", { class: ["ds-label-01-reg"] }, dataEntry.renderDecision),
        h("span", { class: ["ds-label-01-reg ml-8"] }, "|"),
        renderLink(dataEntry),
      ]),

      ,
    ])
    // Ghost DocUnit
  } else if (
    !dataEntry.hasMissingRequiredFields ||
    (dataEntry.missingRequiredFields.length === 1 &&
      dataEntry.missingRequiredFields[0] === "decisionDate" &&
      dataEntry.pending)
  ) {
    return h("div", { class: ["flex flex-row items-center"] }, [
      h(
        h(h(IconOutlineDescription), {
          class: ["mr-8 "],
        }),
      ),
      h("div", { class: ["ds-label-01-reg"] }, dataEntry.renderDecision),
    ])
    // Ghost DocUnit with missing fields
  } else {
    return h("div", { class: ["flex flex-row items-center"] }, [
      h(
        h(h(IconErrorOutline), {
          "aria-label": "Fehlerhafte Eingabe",
          class: ["mr-8 text-red-800"],
        }),
      ),
      h(
        "div",
        { class: ["ds-label-01-reg text-red-800"] },
        dataEntry.renderDecision,
      ),
    ])
  }
}

const DecisionSummary = withSummarizer(decisionSummarizer)
</script>

<template>
  <div
    aria-label="Nachgehende Entscheidung"
    class="flex flex-col bg-white p-32"
  >
    <h2 class="ds-heading-03-reg mb-24">Nachgehende Entscheidungen</h2>
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
