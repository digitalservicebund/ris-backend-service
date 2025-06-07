<script lang="ts" setup>
import dayjs from "dayjs"
import { storeToRefs } from "pinia"
import Button from "primevue/button"
import { computed, Ref } from "vue"
import Tooltip from "./Tooltip.vue"
import DocumentationUnitSummary from "@/components/DocumentationUnitSummary.vue"
import { DocumentUnitCategoriesEnum } from "@/components/enumDocumentUnitCategories"
import ActiveCitation from "@/domain/activeCitation"
import DocumentUnit from "@/domain/documentUnit"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { useExtraContentSidePanelStore } from "@/stores/extraContentSidePanelStore"
import IconBaselineContentCopy from "~icons/ic/baseline-content-copy"
import IconGenerateText from "~icons/ic/round-auto-fix-high"
import IconImportCategories from "~icons/material-symbols/text-select-move-back-word"

const props = defineProps<{
  data: ActiveCitation
}>()
const extraContentSidePanelStore = useExtraContentSidePanelStore()
const store = useDocumentUnitStore()
const { documentUnit } = storeToRefs(store) as {
  documentUnit: Ref<DocumentUnit | undefined>
}

const isParallelDecision = computed(
  () =>
    props.data.citationType?.label == "Parallelentscheidung" ||
    props.data.citationType?.label == "Teilweise Parallelentscheidung",
)

async function copySummary() {
  if (props.data) await navigator.clipboard.writeText(props.data.renderSummary)
}

async function openCategoryImport(documentNumber?: string) {
  extraContentSidePanelStore.togglePanel(true)
  extraContentSidePanelStore.setSidePanelMode("category-import")
  extraContentSidePanelStore.importDocumentNumber = documentNumber
}

async function generateHeadnote() {
  const text = `${props.data.citationType?.label == "Teilweise Parallelentscheidung" ? "Teilweise " : ""}Parallelentscheidung zu der Entscheidung (${props.data.documentType?.label}) des ${props.data.court?.label} vom ${dayjs(props.data.decisionDate).format("DD.MM.YYYY")} - ${props.data.fileNumber}${props.data.citationType?.label == "Teilweise Parallelentscheidung" ? "." : ", welche vollständig dokumentiert ist."}`

  documentUnit.value!.shortTexts.headnote = text
  await store.updateDocumentUnit()
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
</script>

<template>
  <div class="flex w-full justify-between">
    <DocumentationUnitSummary :data="data"> </DocumentationUnitSummary>

    <!-- Button group -->
    <div class="flex flex-row -space-x-2">
      <Tooltip v-if="isParallelDecision" text="Rubriken importieren">
        <Button
          id="category-import"
          aria-label="Rubriken-Import anzeigen"
          data-testid="import-categories"
          size="small"
          text
          @click="openCategoryImport(data.documentNumber)"
        >
          <template #icon><IconImportCategories /></template
        ></Button>
      </Tooltip>
      <Tooltip
        v-if="isParallelDecision"
        :text="
          !!documentUnit!.shortTexts.headnote
            ? 'Zielrubrik Orientierungssatz bereits ausgefüllt'
            : 'O-Satz generieren'
        "
      >
        <Button
          id="generate-headnote"
          aria-label="O-Satz generieren"
          data-testid="generate-headnote"
          :disabled="!!documentUnit!.shortTexts.headnote"
          size="small"
          text
          @click="generateHeadnote"
        >
          <template #icon><IconGenerateText /></template
        ></Button>
      </Tooltip>
      <Tooltip text="Kopieren">
        <Button
          id="category-import"
          aria-label="Rubriken-Import anzeigen"
          data-testid="copy-summary"
          :disabled="!!documentUnit!.shortTexts.headnote"
          size="small"
          text
          @click="copySummary"
          @keypress.enter="copySummary"
        >
          <template #icon><IconBaselineContentCopy /></template
        ></Button>
      </Tooltip>
    </div>
  </div>
</template>
