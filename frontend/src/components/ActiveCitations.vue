<script lang="ts" setup>
import { h, computed } from "vue"
import { RouterLink } from "vue-router"
import ActiveCitationInput from "@/components/ActiveCitationInput.vue"
import { withSummarizer } from "@/components/DataSetSummary.vue"
import EditableList from "@/components/EditableList.vue"
import ActiveCitation from "@/domain/activeCitation"
import BaselineArrowOutward from "~icons/ic/baseline-arrow-outward"
import IconBaselineDescription from "~icons/ic/baseline-description"
import IconErrorOutline from "~icons/ic/baseline-error-outline"
import IconOutlineDescription from "~icons/ic/outline-description"

const props = defineProps<{
  modelValue: ActiveCitation[] | undefined
}>()

const emit = defineEmits<{ "update:modelValue": [value?: ActiveCitation[]] }>()

const activeCitations = computed({
  get: () => {
    return props.modelValue
  },
  set: (value) => {
    if (value) emit("update:modelValue", value)
  },
})

const defaultValue = new ActiveCitation()

function renderLink(dataEntry: ActiveCitation) {
  return h(
    RouterLink,
    {
      class: [
        "ds-link-03-bold ml-8 border-b-2 border-blue-800 flex flex-row leading-24 focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800",
      ],
      target: "_blank",
      tabindex: 0,
      to: {
        name: "caselaw-documentUnit-documentNumber-categories",
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
          onClick: (e: Event) => {
            e.stopPropagation()
          },
          class: ["flex flex-row items-center"],
        },
        [h(() => dataEntry.documentNumber), h(() => h(BaselineArrowOutward))],
      ),
  )
}

function decisionSummarizer(dataEntry: ActiveCitation) {
  // Linked DocUnit
  if (dataEntry.hasForeignSource) {
    if (!dataEntry.citationTypeIsSet) {
      return h("div", { class: ["flex flex-row items-center"] }, [
        h(h(IconErrorOutline), {
          "aria-label": "Fehlerhafte Eingabe",
          class: ["mr-8 text-red-800"],
        }),
        h("div", { class: ["flex flex-row items-baseline"] }, [
          h(
            "div",
            { class: ["ds-label-01-reg text-red-800"] },
            dataEntry.renderDecision,
          ),
          h("span", { class: ["ds-label-01-reg ml-8"] }, "|"),
          renderLink(dataEntry),
        ]),
      ])
    } else {
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
    }
    // Ghost DocUnit with missing fields
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
        { class: ["ds-label-01-reg text-red-800"] },
        dataEntry.renderDecision,
      ),
    ])
    // Ghost DocUnit
  } else {
    return h("div", { class: ["flex flex-row items-center"] }, [
      h(
        h(h(IconOutlineDescription), {
          class: ["mr-8 "],
        }),
      ),
      h("div", { class: ["ds-label-01-reg"] }, dataEntry.renderDecision),
    ])
  }
}

const CitationsSummary = withSummarizer(decisionSummarizer)
</script>

<template>
  <div aria-label="Aktivzitierung" class="bg-white p-32">
    <h2 class="ds-heading-03-reg mb-24">Aktivzitierung</h2>
    <div class="flex flex-row">
      <div class="flex-1">
        <EditableList
          v-model="activeCitations"
          :default-value="defaultValue"
          :edit-component="ActiveCitationInput"
          :summary-component="CitationsSummary"
        />
      </div>
    </div>
  </div>
</template>
