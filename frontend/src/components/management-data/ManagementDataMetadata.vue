<script lang="ts" setup>
import { computed } from "vue"
import DocumentUnit, { Source } from "@/domain/documentUnit"
import Reference from "@/domain/reference"
import DateUtil from "@/utils/dateUtil"

const { documentUnit } = defineProps<{
  documentUnit: DocumentUnit
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
    <div>
      <dt class="ds-label-02-bold">Angelegt am</dt>
      <dd class="ds-label-02-reg">{{ createdAt }}</dd>
    </div>
    <div>
      <dt class="ds-label-02-bold">Von</dt>
      <dd class="ds-label-02-reg">{{ createdBy }}</dd>
    </div>
    <div>
      <dt class="ds-label-02-bold">Quelle</dt>
      <dd class="ds-label-02-reg">{{ source }}</dd>
    </div>
    <div />
    <div>
      <dt class="ds-label-02-bold">Zuletzt bearbeitet am</dt>
      <dd class="ds-label-02-reg">{{ lastUpdatedAt }}</dd>
    </div>
    <div>
      <dt class="ds-label-02-bold">Von</dt>
      <dd class="ds-label-02-reg">{{ lastUpdatedBy }}</dd>
    </div>
    <div>
      <dt class="ds-label-02-bold">Vorgang</dt>
      <dd class="ds-label-02-reg">{{ procedure }}</dd>
    </div>
    <div>
      <dt class="ds-label-02-bold">Erstveröffentlichung am</dt>
      <dd class="ds-label-02-reg">{{ firstPublishedAt }}</dd>
    </div>
  </dl>
</template>
