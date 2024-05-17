<script lang="ts" setup>
import { computed, h } from "vue"
import { withSummarizer } from "@/components/DataSetSummary.vue"
import EditableList from "@/components/EditableList.vue"
import IconBadge from "@/components/IconBadge.vue"
import NormReferenceInput from "@/components/NormReferenceInput.vue"
import NormReference from "@/domain/normReference"
import SingleNorm from "@/domain/singleNorm"
import IconError from "~icons/ic/baseline-error"
import IconBook from "~icons/material-symbols/book-2"
import IconBreakingNews from "~icons/material-symbols/breaking-news"
import IconArrowRight from "~icons/material-symbols/subdirectory-arrow-right"

const props = defineProps<{
  modelValue: NormReference[] | undefined
}>()

const emit = defineEmits<{
  "update:modelValue": [value?: NormReference[]]
}>()

const ambiguousNormReferenceError = "Mehrdeutiger Verweis"
const missingDataError = "Fehlende Daten"

const norms = computed({
  get: () => {
    return props.modelValue
  },
  set: (value) => {
    if (value) emit("update:modelValue", value)
  },
})

/**
 * Returns a booleand value, if a given normEntry has single norms and resepctively renders the norms' summary in one
 * compact line or in a sublist, below the norm abbreviation.
 * @param normEntry The norm entry to check for single norms
 */
function hasSingleNorms(normEntry: NormReference) {
  if (normEntry.singleNorms)
    return (
      normEntry.singleNorms?.length > 0 && !normEntry.singleNorms[0].isEmpty
    )
  else return false
}

const defaultValue = new NormReference()

/**
 * Summarizer for a legal force
 * @param singleNorm The single Norm that has a legal force
 */
function legalForceSummarizer(singleNorm: SingleNorm) {
  return h("div", { class: ["flex flex-row items-center"] }, [
    h("div", {}, "|"),
    h(IconBreakingNews, { class: ["mr-8 ml-8"] }),
    h("div", { class: ["link-01-reg mr-8"] }, singleNorm.renderLegalForce),
    singleNorm.legalForce?.hasMissingRequiredFields
      ? errorBadgeSummarizer(missingDataError)
      : null,
  ])
}

/**
 * Returns a render function with an error icon badge
 @param errorLabel error message to be displayed in the badge.
 */
function errorBadgeSummarizer(errorLabel: string) {
  return h(IconBadge, {
    backgroundColor: "bg-red-300",
    color: "text-red-900",
    icon: IconError,
    label: errorLabel,
  })
}

/**
 * Summarizer for a normEntry. With no or only one single norm, it is rendered in one line, if more single norms present,
 * they are rendered as sub list.
 * @param normEntry Norm Entry to be summarized.
 */
function decisionSummarizer(normEntry: NormReference) {
  if (normEntry.singleNorms?.length === 1) {
    return h("div", { class: ["flex flex-col gap-32"] }, [
      h("div", { class: ["flex flex-row items-center"] }, [
        h(IconBook, { class: ["mr-8"] }),
        h(
          "div",
          { class: ["link-01-reg mr-8"] },
          normEntry.renderDecision +
            ", " +
            normEntry.singleNorms[0].renderDecision,
        ),
        normEntry.singleNorms[0].legalForce
          ? legalForceSummarizer(normEntry.singleNorms[0])
          : null,
        normEntry.hasAmbiguousNormReference
          ? errorBadgeSummarizer(ambiguousNormReferenceError)
          : null,
      ]),
    ])
  } else {
    return h("div", { class: ["flex flex-col gap-32"] }, [
      h("div", { class: ["flex flex-row items-center"] }, [
        h(IconBook, { class: ["mr-8"] }),
        h("div", { class: ["link-01-reg mr-8"] }, normEntry.renderDecision),
        normEntry.hasAmbiguousNormReference
          ? errorBadgeSummarizer(ambiguousNormReferenceError)
          : null,
      ]),
      hasSingleNorms(normEntry)
        ? h(
            "div",
            { class: ["flex flex-col gap-32"] },
            normEntry.singleNorms?.map((singleNorm) => {
              return !singleNorm.isEmpty
                ? h("div", { class: ["flex flex-row items-center"] }, [
                    h(IconArrowRight, { class: ["mr-8"] }),
                    h("div", { class: ["link-01-reg mr-8"] }, [
                      h("span", normEntry.renderDecision),
                      h(
                        "span",
                        singleNorm.renderDecision.length > 0 ? ", " : "",
                      ),
                      h("span", singleNorm.renderDecision),
                    ]),
                    singleNorm.legalForce
                      ? legalForceSummarizer(singleNorm)
                      : null,
                  ])
                : null
            }),
          )
        : null,
    ])
  }
}

const NormsSummary = withSummarizer(decisionSummarizer)
</script>
<template>
  <div aria-label="Norm" class="bg-white p-32">
    <h2 class="ds-heading-03-reg mb-24">Normen</h2>
    <div class="flex flex-row">
      <div class="flex-1">
        <EditableList
          v-model="norms"
          :default-value="defaultValue"
          :edit-component="NormReferenceInput"
          :summary-component="NormsSummary"
        />
      </div>
    </div>
  </div>
</template>
