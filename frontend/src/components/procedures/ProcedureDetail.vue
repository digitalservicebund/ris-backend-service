<script lang="ts" setup>
import dayjs from "dayjs"
import { Procedure } from "@/domain/documentUnit"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"

defineProps<{
  procedure: Procedure
}>()

function renderDocumentUnit(documentUnit: DocumentUnitListEntry): string {
  return [
    ...(documentUnit.courtType ? [`${documentUnit.courtType}`] : []),
    ...(documentUnit.decisionDate
      ? [dayjs(documentUnit.decisionDate).format("DD.MM.YYYY")]
      : []),
    ...(documentUnit.documentType ? [documentUnit.documentType] : []),
    ...(documentUnit.fileNumber ? [documentUnit.fileNumber] : []),
    ...(documentUnit.documentNumber ? [documentUnit.documentNumber] : []),
  ].join(", ")
}
</script>

<template>
  <ul class="py-24">
    <li
      v-for="documentUnit in procedure.documentUnits"
      :key="documentUnit.documentNumber"
      class="ml-208 py-4"
    >
      <router-link
        class="ds-link-01-bold underline"
        tabindex="-1"
        target="_blank"
        :to="{
          name: 'caselaw-documentUnit-documentNumber-categories',
          params: { documentNumber: documentUnit.documentNumber },
        }"
      >
        <button class="underline">
          {{ renderDocumentUnit(documentUnit) }}
        </button>
      </router-link>
    </li>
  </ul>
  <span class="ds-label-02-reg pt-28 italic text-gray-600"
    >erstellt am {{ dayjs(procedure.createdAt).format("DD.MM.YYYY") }}</span
  >
</template>
