<script lang="ts" setup>
import dayjs from "dayjs"
import { computed } from "vue"
import { Procedure } from "@/domain/documentUnit"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"

const props = defineProps<{
  procedure: Procedure
}>()

const isLoading = computed(
  () => !props.procedure.documentUnits && props.procedure.documentUnitCount > 0,
)

function renderDocumentUnit(documentUnit: DocumentUnitListEntry): string {
  return [
    ...(documentUnit.court ? [`${documentUnit.court.label}`] : []),
    ...(documentUnit.decisionDate
      ? [dayjs(documentUnit.decisionDate).format("DD.MM.YYYY")]
      : []),
    ...(documentUnit.documentType ? [documentUnit.documentType.label] : []),
    ...(documentUnit.fileNumber ? [documentUnit.fileNumber] : []),
    ...(documentUnit.documentNumber ? [documentUnit.documentNumber] : []),
  ].join(", ")
}
</script>

<template>
  <div
    v-if="isLoading"
    aria-label="Ladestatus"
    class="grid justify-items-center bg-white bg-opacity-60"
  >
    <div
      class="inline-block h-32 w-32 animate-spin rounded-full border-[3px] border-solid border-blue-900 border-r-transparent align-[-0.125em] motion-reduce:animate-[spin_1.5s_linear_infinite]"
      role="status"
    ></div>
  </div>
  <div v-else class="grid grid-cols-[14em_auto] gap-x-24">
    <div class="flex items-end">
      <span class="bottom ds-label-02-reg pt-28 italic text-gray-600"
        >erstellt am {{ dayjs(procedure.createdAt).format("DD.MM.YYYY") }}</span
      >
    </div>
    <ul class="py-24 text-left">
      <li
        v-for="documentUnit in procedure.documentUnits"
        :key="documentUnit.documentNumber"
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
          <button class="text-left underline">
            {{ renderDocumentUnit(documentUnit) }}
          </button>
        </router-link>
      </li>
    </ul>
  </div>
</template>
