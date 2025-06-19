<script lang="ts" setup>
import { computed } from "vue"
import { DocumentationUnit } from "@/domain/documentationUnit"
import Reference from "@/domain/reference"
import { Source } from "@/domain/source"
import DateUtil from "@/utils/dateUtil"

const { documentUnit } = defineProps<{
  documentUnit: DocumentationUnit
}>()

const createdBy = computed(() =>
  formatEditor(
    documentUnit?.managementData.createdByDocOffice,
    documentUnit.managementData.createdByName,
  ),
)
const lastUpdatedBy = computed(() =>
  formatEditor(
    documentUnit?.managementData.lastUpdatedByDocOffice,
    documentUnit.managementData.lastUpdatedByName,
  ),
)
const source = computed(() => formatSource(documentUnit?.coreData?.source))
const procedure = computed(
  () => documentUnit?.coreData?.procedure?.label ?? "–",
)
const createdAt = computed(() =>
  formatTimestamp(documentUnit?.managementData?.createdAtDateTime),
)
const lastUpdatedAt = computed(() =>
  formatTimestamp(documentUnit?.managementData?.lastUpdatedAtDateTime),
)
const firstPublishedAt = computed(() =>
  formatTimestamp(documentUnit?.managementData?.firstPublishedAtDateTime),
)

const formatTimestamp = (date?: string) =>
  date ? DateUtil.formatDateTime(date) : "–"

const formatEditor = (docOffice?: string, name?: string) => {
  if (!docOffice && !name) return "–"
  if (!name) return docOffice
  if (!docOffice) return name
  return `${docOffice} (${name})`
}

const formatSource = (source?: Source) => {
  if (!source) return "–"
  if (!source.reference) return source.value
  const reference = new Reference(source.reference)
  const referenceText = `${source.value} aus ${reference.renderSummary}`
  if (!documentUnit.coreData.creatingDocOffice) return referenceText
  return `${referenceText} (${documentUnit.coreData.creatingDocOffice.abbreviation})`
}
</script>

<template>
  <dl class="my-16 grid w-fit auto-rows-auto grid-cols-[repeat(4,auto)] gap-48">
    <div data-testid="management-data-created-at">
      <dt class="ris-label2-bold">Angelegt am</dt>
      <dd class="ris-label2-regular">{{ createdAt }}</dd>
    </div>
    <div data-testid="management-data-created-by">
      <dt class="ris-label2-bold">Von</dt>
      <dd class="ris-label2-regular">{{ createdBy }}</dd>
    </div>
    <div data-testid="management-data-source">
      <dt class="ris-label2-bold">Quelle</dt>
      <dd class="ris-label2-regular">{{ source }}</dd>
    </div>
    <div />
    <div data-testid="management-data-last-updated-at">
      <dt class="ris-label2-bold">Zuletzt bearbeitet am</dt>
      <dd class="ris-label2-regular">{{ lastUpdatedAt }}</dd>
    </div>
    <div data-testid="management-data-last-updated-by">
      <dt class="ris-label2-bold">Von</dt>
      <dd class="ris-label2-regular">{{ lastUpdatedBy }}</dd>
    </div>
    <div data-testid="management-data-procedure">
      <dt class="ris-label2-bold">Vorgang</dt>
      <dd class="ris-label2-regular">{{ procedure }}</dd>
    </div>
    <div data-testid="management-data-first-published-at">
      <dt class="ris-label2-bold">Erstveröffentlichung am</dt>
      <dd class="ris-label2-regular">{{ firstPublishedAt }}</dd>
    </div>
  </dl>
</template>
