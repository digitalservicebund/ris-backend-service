<script lang="ts" setup>
import { computed } from "vue"
import DocumentUnit, { Source } from "@/domain/documentUnit"
import DateUtil from "@/utils/dateUtil"

const { documentUnit } = defineProps<{
  documentUnit: DocumentUnit
}>()
const source = computed(() => formatSource(documentUnit?.coreData?.source))
const createdBy = computed(() =>
  formatEditor(
    documentUnit?.managementData.createdByDocOffice,
    documentUnit.managementData.createdByName,
  ),
)
const lastEditedBy = computed(() =>
  formatEditor(
    documentUnit?.managementData.lastEditedByDocOffice,
    documentUnit.managementData.lastEditedByName,
  ),
)
const formatTimestamp = (date?: string) =>
  date ? DateUtil.formatDateTime(date) : "–"
const formatEditor = (docOffice?: string, name?: string) => {
  if (!docOffice) return "–"
  if (!name) return docOffice
  return `${docOffice} (${name})`
}
const formatSource = (source?: Source) => {
  if (!source) return "–"
  if (!source.reference) return source.value
  // TODO: Add creatingDocOffice(?) and properly format all kinds of references
  return `${source.value} aus ${source.reference.legalPeriodical?.abbreviation} ("DOC OFFICE")`
}
</script>

<template>
  <dl class="my-16 grid w-fit auto-rows-auto grid-cols-[repeat(4,auto)] gap-48">
    <div>
      <dt class="ds-label-02-bold">Angelegt am</dt>
      <dd class="ds-label-02-reg">
        {{ formatTimestamp(documentUnit.managementData.createdAtDateTime) }}
      </dd>
    </div>
    <div>
      <dt class="ds-label-02-bold">Von</dt>
      <dd class="ds-label-02-reg">{{ lastEditedBy }}</dd>
    </div>
    <div>
      <dt class="ds-label-02-bold">Quelle</dt>
      <dd class="ds-label-02-reg">{{ source }}</dd>
    </div>
    <div />
    <div>
      <dt class="ds-label-02-bold">Zuletzt bearbeitet am</dt>
      <dd class="ds-label-02-reg">
        {{ formatTimestamp(documentUnit.managementData.lastEditedAtDateTime) }}
      </dd>
    </div>
    <div>
      <dt class="ds-label-02-bold">Von</dt>
      <dd class="ds-label-02-reg">{{ createdBy }}</dd>
    </div>
    <div>
      <dt class="ds-label-02-bold">Vorgang</dt>
      <dd class="ds-label-02-reg">
        {{ documentUnit?.coreData?.procedure?.label ?? "–" }}
      </dd>
    </div>
    <div>
      <dt class="ds-label-02-bold">Erstveröffentlichung am</dt>
      <dd class="ds-label-02-reg">
        {{
          formatTimestamp(documentUnit.managementData.firstPublishedAtDateTime)
        }}
      </dd>
    </div>
  </dl>
</template>
