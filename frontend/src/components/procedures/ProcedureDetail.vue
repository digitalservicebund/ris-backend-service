<script lang="ts" setup>
import dayjs from "dayjs"
import { computed } from "vue"
import LoadingSpinner from "@/components/LoadingSpinner.vue"
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
    class="grid justify-items-center bg-white bg-opacity-60"
  >
    <LoadingSpinner />
  </div>
  <div v-else class="grid grid-cols-[14em_auto] gap-x-24">
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
