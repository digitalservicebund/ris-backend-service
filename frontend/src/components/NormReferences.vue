<script lang="ts" setup>
import { computed, h } from "vue"
import { withSummarizer } from "@/components/DataSetSummary.vue"
import EditableList from "@/components/EditableList.vue"
import NormReferenceInput from "@/components/NormReferenceInput.vue"
import NormReference from "@/domain/normReference"
import IconBook from "~icons/material-symbols/book-2"
import IconArrowRight from "~icons/material-symbols/subdirectory-arrow-right"

const props = defineProps<{
  modelValue: NormReference[] | undefined
}>()

const emit = defineEmits<{
  "update:modelValue": [value?: NormReference[]]
}>()

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

function decisionSummarizer(normEntry: NormReference) {
  return h("div", { class: ["flex flex-col gap-32"] }, [
    h("div", { class: ["flex flex-row items-center"] }, [
      h(IconBook, { class: ["mr-8"] }),
      h("div", { class: ["link-01-reg"] }, normEntry.renderDecision),
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
                    { class: ["link-01-reg"] },
                    singleNorm.renderDecision,
                  ),
                ])
              : null
          }),
        )
      : null,
  ])
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
