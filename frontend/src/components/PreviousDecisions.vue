<script lang="ts" setup>
import { h, computed } from "vue"
import { RouterLink } from "vue-router"
import PreviousDecisionInputGroup from "./PreviousDecisionInputGroup.vue"
import { withSummarizer } from "@/components/DataSetSummary.vue"
import EditableList from "@/components/EditableList.vue"
import IconBadge from "@/components/IconBadge.vue"
import PreviousDecision from "@/domain/previousDecision"
import BaselineArrowOutward from "~icons/ic/baseline-arrow-outward"
import IconBaselineDescription from "~icons/ic/baseline-description"
import IconError from "~icons/ic/baseline-error"
import IconOutlineDescription from "~icons/ic/outline-description"

const props = defineProps<{
  modelValue: PreviousDecision[] | undefined
}>()

const emit = defineEmits<{
  "update:modelValue": [value?: PreviousDecision[]]
}>()

// vor gehende nach gehede aktiv zityierung => searchresultlist
const proceedingDecisions = computed({
  get: () => {
    return props.modelValue
  },
  set: (value) => {
    if (value) emit("update:modelValue", value)
  },
})
const defaultValue = new PreviousDecision()

/**
 * Returns a render function with an error icon badge
 */
function errorBadgeSummarizer() {
  return h(IconBadge, {
    backgroundColor: "bg-red-300",
    color: "text-red-900",
    icon: IconError,
    label: "Fehlende Daten",
  })
}

function renderLink(dataEntry: PreviousDecision) {
  return h(
    RouterLink,
    {
      class: [
        "ds-link-03-bold ml-8 border-b-2 border-blue-800 flex flex-row leading-24 focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800",
      ],
      target: "_blank",
      tabindex: 0,
      to: {
        name: "caselaw-documentUnit-documentNumber-preview",
        params: { documentNumber: dataEntry.documentNumber },
      },
      onClick: (e: Event) => {
        e.stopPropagation()
      },
      onKeydown: (e: KeyboardEvent) => {
        e.stopPropagation()
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

function decisionSummarizer(dataEntry: PreviousDecision) {
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
    ])
    // Ghost DocUnit
  } else {
    return h("div", { class: ["flex flex-row items-center"] }, [
      h(
        h(h(IconOutlineDescription), {
          class: ["mr-8 "],
        }),
      ),
      h("div", { class: ["ds-label-01-reg mr-8"] }, dataEntry.renderDecision),
      dataEntry.hasMissingRequiredFields ? errorBadgeSummarizer() : null,
    ])
  }
}

const DecisionSummary = withSummarizer(decisionSummarizer)
</script>

<template>
  <div class="mb-2 flex flex-col gap-24 bg-white p-32">
    <h2 class="ds-heading-03-bold">Rechtszug</h2>
    <div aria-label="Vorgehende Entscheidung">
      <h2 class="ds-heading-03-reg mb-24">Vorgehende Entscheidungen</h2>
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
  </div>
</template>
