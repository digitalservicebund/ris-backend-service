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

const norms = computed({
  get: () => {
    return props.modelValue
  },
  set: (value) => {
    if (value) emit("update:modelValue", value)
  },
})

function hasSingleNorms(normEntry: NormReference) {
  if (normEntry.singleNorms)
    return (
      normEntry.singleNorms?.length > 0 && !normEntry.singleNorms[0].isEmpty
    )
  else return false
}

const defaultValue = new NormReference()

function legalForceSummarizer(singleNorm: SingleNorm) {
  return h("div", { class: ["flex flex-row items-center"] }, [
    h("div", {}, "|"),
    h(IconBreakingNews, { class: ["mr-8 ml-8"] }),
    h("div", { class: ["link-01-reg mr-8"] }, singleNorm.renderLegalForce),
  ])
}

function errorBadgeSummarizer() {
  return h(IconBadge, {
    backgroundColor: "bg-red-300",
    color: "text-red-900",
    icon: IconError,
    label: ambiguousNormReferenceError,
  })
}

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
        normEntry.singleNorms[0].hasLegalForce
          ? legalForceSummarizer(normEntry.singleNorms[0])
          : null,
        normEntry.hasAmbiguousNormReference ? errorBadgeSummarizer() : null,
      ]),
    ])
  } else {
    return h("div", { class: ["flex flex-col gap-32"] }, [
      h("div", { class: ["flex flex-row items-center"] }, [
        h(IconBook, { class: ["mr-8"] }),
        h("div", { class: ["link-01-reg mr-8"] }, normEntry.renderDecision),
        normEntry.hasAmbiguousNormReference ? errorBadgeSummarizer() : null,
      ]),
      hasSingleNorms(normEntry)
        ? h(
            "div",
            { class: ["flex flex-col gap-32"] },
            normEntry.singleNorms?.map((singleNorm) => {
              return !singleNorm.isEmpty
                ? h("div", { class: ["flex flex-row items-center"] }, [
                    h(IconArrowRight, { class: ["mr-8"] }),
                    h(
                      "div",
                      { class: ["link-01-reg mr-8"] },
                      normEntry.renderDecision +
                        ", " +
                        singleNorm.renderDecision,
                    ),
                    singleNorm.hasLegalForce
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
