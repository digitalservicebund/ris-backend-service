<script lang="ts" setup generic="T extends ListItem">
import { computed } from "vue"
import DecisionSummary from "@/components/DecisionSummary.vue"
import IconBadge from "@/components/IconBadge.vue"
import ActiveCitation from "@/domain/activeCitation"
import ListItem from "@/domain/editableListItem" // NOSONAR: import is needed for extension
import IconBaselineDescription from "~icons/ic/baseline-description"
import IconError from "~icons/ic/baseline-error"
import IconOutlineDescription from "~icons/ic/outline-description"

const props = defineProps<{
  data: T
}>()

const showErrorBadge = computed(() => {
  if (props.data instanceof ActiveCitation) {
    return props.data?.hasForeignSource
      ? !props.data.citationTypeIsSet
      : props.data?.hasMissingRequiredFields
  }
  return props.data?.hasMissingRequiredFields
})
</script>

<template>
  <span class="flex w-full flex-row flex-wrap items-center">
    <DecisionSummary
      :document-number="props.data.documentNumber"
      :icon="
        props.data.documentNumber
          ? IconBaselineDescription
          : IconOutlineDescription
      "
      :summary="props.data.renderSummary ?? ''"
    ></DecisionSummary>

    <IconBadge
      v-if="showErrorBadge"
      background-color="bg-red-300"
      :icon="IconError"
      icon-color="text-red-900"
      label="Fehlende Daten"
    />
  </span>
</template>
