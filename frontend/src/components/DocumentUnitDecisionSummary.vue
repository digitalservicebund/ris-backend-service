<script lang="ts" setup>
import { computed, onMounted, ref } from "vue"
import Tooltip from "./Tooltip.vue"
import DecisionSummary from "@/components/DecisionSummary.vue"
import IconBadge from "@/components/IconBadge.vue"
import TextButton from "@/components/input/TextButton.vue"
import ActiveCitation from "@/domain/activeCitation"
import EnsuingDecision from "@/domain/ensuingDecision"
import PreviousDecision from "@/domain/previousDecision"
import FeatureToggleService from "@/services/featureToggleService"
import { useExtraContentSidePanelStore } from "@/stores/extraContentSidePanelStore"
import IconBaselineContentCopy from "~icons/ic/baseline-content-copy"
import IconBaselineDescription from "~icons/ic/baseline-description"
import IconError from "~icons/ic/baseline-error"
import IconOutlineDescription from "~icons/ic/outline-description"
import IconImportCategories from "~icons/material-symbols/text-select-move-back-word"

const props = defineProps<{
  data: ActiveCitation | EnsuingDecision | PreviousDecision
}>()
const featureToggle = ref()
const extraContentSidePanelStore = useExtraContentSidePanelStore()

const iconComponent = computed(() => {
  return props.data?.hasForeignSource
    ? IconBaselineDescription
    : IconOutlineDescription
})

const showErrorBadge = computed(() => {
  if (props.data instanceof ActiveCitation) {
    return props.data?.hasForeignSource
      ? !props.data.citationTypeIsSet
      : props.data?.hasMissingRequiredFields
  }
  return props.data?.hasMissingRequiredFields
})

async function copySummary() {
  if (props.data) await navigator.clipboard.writeText(props.data.renderDecision)
}

async function openCategoryImport(documentNumber?: string) {
  extraContentSidePanelStore.togglePanel(true)
  extraContentSidePanelStore.setSidePanelMode("category-import")
  extraContentSidePanelStore.importDocumentNumber = documentNumber
}

onMounted(async () => {
  featureToggle.value = (
    await FeatureToggleService.isEnabled("neuris.category-importer")
  ).data
})
</script>

<template>
  <div class="flex w-full justify-between">
    <div class="flex flex-row items-center">
      <component :is="iconComponent" class="mr-8" />
      <div v-if="data?.hasForeignSource" class="flex flex-row items-baseline">
        <div v-if="data?.hasForeignSource" class="flex flex-row items-baseline">
          <DecisionSummary class="mr-8" :decision="data"></DecisionSummary>
        </div>
      </div>
      <div v-else class="ds-label-01-reg mr-8">{{ data?.renderDecision }}</div>
      <IconBadge
        v-if="showErrorBadge"
        background-color="bg-red-300"
        color="text-red-900"
        :icon="IconError"
        label="Fehlende Daten"
      />
    </div>

    <div class="flex flex-row -space-x-2">
      <Tooltip
        v-if="
          data instanceof ActiveCitation &&
          (data.citationType?.label == 'Parallelentscheidung' ||
            data.citationType?.label == 'Teilweise Parallelentscheidung') &&
          featureToggle
        "
        text="Rubriken importieren"
      >
        <TextButton
          id="category-import"
          aria-label="Rubriken-Import anzeigen"
          button-type="tertiary"
          data-testid="import-categories"
          :icon="IconImportCategories"
          size="small"
          @click="openCategoryImport(data.documentNumber)"
        />
      </Tooltip>
      <Tooltip v-if="data instanceof ActiveCitation" text="Kopieren">
        <TextButton
          id="category-import"
          aria-label="Rubriken-Import anzeigen"
          button-type="tertiary"
          data-testid="copy-summary"
          :icon="IconBaselineContentCopy"
          size="small"
          @click="copySummary"
          @keypress.enter="copySummary"
        />
      </Tooltip>
    </div>
  </div>
</template>
