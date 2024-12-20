<script lang="ts" setup>
import dayjs from "dayjs"
import { computed, onMounted, ref } from "vue"
import Tooltip from "./Tooltip.vue"
import DecisionSummary from "@/components/DecisionSummary.vue"
import { DocumentUnitCategoriesEnum } from "@/components/enumDocumentUnitCategories"
import IconBadge from "@/components/IconBadge.vue"
import TextButton from "@/components/input/TextButton.vue"
import ActiveCitation from "@/domain/activeCitation"
import EnsuingDecision from "@/domain/ensuingDecision"
import PreviousDecision from "@/domain/previousDecision"
import FeatureToggleService from "@/services/featureToggleService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { useExtraContentSidePanelStore } from "@/stores/extraContentSidePanelStore"
import IconBaselineContentCopy from "~icons/ic/baseline-content-copy"
import IconBaselineDescription from "~icons/ic/baseline-description"
import IconError from "~icons/ic/baseline-error"
import IconOutlineDescription from "~icons/ic/outline-description"
import IconGenerateText from "~icons/ic/round-auto-fix-high"
import IconImportCategories from "~icons/material-symbols/text-select-move-back-word"

const props = defineProps<{
  data: ActiveCitation | EnsuingDecision | PreviousDecision
}>()
const featureToggle = ref()
const extraContentSidePanelStore = useExtraContentSidePanelStore()
const documentUnitStore = useDocumentUnitStore()

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

const isParallelDecision = computed(
  () =>
    props.data instanceof ActiveCitation &&
    (props.data.citationType?.label == "Parallelentscheidung" ||
      props.data.citationType?.label == "Teilweise Parallelentscheidung") &&
    featureToggle,
)

async function copySummary() {
  if (props.data) await navigator.clipboard.writeText(props.data.renderDecision)
}

async function openCategoryImport(documentNumber?: string) {
  extraContentSidePanelStore.togglePanel(true)
  extraContentSidePanelStore.setSidePanelMode("category-import")
  extraContentSidePanelStore.importDocumentNumber = documentNumber
}

async function generateHeadnote() {
  const text = `${props.data instanceof ActiveCitation && props.data.citationType?.label == "Teilweise Parallelentscheidung" ? "Teilweise " : ""}Parallelentscheidung zu der Entscheidung (${props.data.documentType?.label}) des ${props.data.court?.label} (${props.data.court?.location}) vom ${dayjs(props.data.decisionDate).format("DD.MM.YYYY")} - ${props.data.fileNumber}${props.data instanceof ActiveCitation && props.data.citationType?.label == "Teilweise Parallelentscheidung" ? "." : ", welche vollständig dokumentiert ist."}`

  documentUnitStore.documentUnit!.shortTexts.headnote = text
  await documentUnitStore.updateDocumentUnit()
  //scroll to headnote
  const element = document.getElementById(DocumentUnitCategoriesEnum.TEXTS)
  const headerOffset = 80
  const elementPosition = element ? element.getBoundingClientRect().top : 0
  const offsetPosition = elementPosition + window.scrollY - headerOffset
  window.scrollTo({
    top: offsetPosition,
    behavior: "smooth",
  })
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
      <Tooltip v-if="isParallelDecision" text="Rubriken importieren">
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
      <Tooltip
        v-if="isParallelDecision"
        :text="
          !!documentUnitStore.documentUnit!.shortTexts.headnote
            ? 'Zielrubrik Orientierungssatz bereits ausgefüllt'
            : 'O-Satz generieren'
        "
      >
        <TextButton
          id="generate-headnote"
          aria-label="O-Satz generieren"
          button-type="tertiary"
          data-testid="generate-headnote"
          :disabled="!!documentUnitStore.documentUnit!.shortTexts.headnote"
          :icon="IconGenerateText"
          size="small"
          @click="generateHeadnote"
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
