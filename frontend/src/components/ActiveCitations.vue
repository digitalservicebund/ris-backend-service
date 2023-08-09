<script lang="ts" setup>
import { h, computed } from "vue"
import { RouterLink } from "vue-router"
import ActiveCitationInput from "@/components/ActiveCitationInput.vue"
import ActiveCitation from "@/domain/activeCitation"
import { withSummarizer } from "@/shared/components/DataSetSummary.vue"
import EditableList from "@/shared/components/EditableListCaselaw.vue"

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

function decisionSummarizer(activeCitation: ActiveCitation) {
  if (activeCitation.hasForeignSource) {
    return h("div", { class: ["flex flex-row items-center"] }, [
      !activeCitation.citationStyleIsSet &&
        renderValidationAlert("Art der Zitierung"),
      h(
        RouterLink,
        {
          class: [
            "ds-link-01-bold",
            "underline",
            !activeCitation.citationStyleIsSet && "pl-32",
          ],
          target: "_blank",
          tabindex: -1,
          to: {
            name: "caselaw-documentUnit-documentNumber-categories",
            params: { documentNumber: activeCitation.documentNumber },
          },
        },
        () => activeCitation.renderDecision,
      ),
    ])
  } else if (activeCitation.hasMissingRequiredFields) {
    return h("div", { class: ["flex flex-row items-center"] }, [
      renderValidationAlert(),
      h(
        "div",
        { class: ["ds-label-02-bold text-red-800"] },
        activeCitation.renderDecision,
      ),
    ])
  } else {
    return h("div", { class: ["link-02-reg"] }, activeCitation.renderDecision)
  }
}

function renderValidationAlert(message?: string) {
  return [
    h(
      "span",
      {
        "aria-label": "Fehlerhafte Eingabe",
        class: ["material-icons pr-8 text-red-800"],
      },
      "error_outline",
    ),
    message,
  ]
}

const CitationsSummary = withSummarizer(decisionSummarizer)
</script>

<template>
  <div aria-label="Aktivzitierung" class="bg-white p-16">
    <h2 id="activeCitations" class="ds-label-02-bold mb-[1rem]">
      Aktivzitierung
    </h2>
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
