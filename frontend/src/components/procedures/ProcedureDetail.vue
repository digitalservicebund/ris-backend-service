<script lang="ts" setup>
import { computed } from "vue"
import DocumentUnitList from "@/components/DocumentUnitList.vue"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import { Procedure } from "@/domain/procedure"
import { ResponseError } from "@/services/httpClient"

const props = defineProps<{
  procedure: Procedure
  docUnits: DocumentUnitListEntry[]
  responseError?: ResponseError
}>()

const emit = defineEmits<{
  deleteDocumentUnit: [
    documentUnitListEntry: DocumentUnitListEntry,
    procedure: Procedure,
  ]
}>()

/**
 * Sets loading state to true, when mismatch between the documentationUnitCount and the actual loaded documentUnits
 */
const isLoading = computed(
  () => !props.docUnits?.length && props.procedure.documentationUnitCount > 0,
)
</script>

<template>
  <div v-if="procedure.documentationUnitCount > 0">
    <DocumentUnitList
      class="grow"
      :document-unit-list-entries="docUnits"
      :is-loading="isLoading"
      :search-response-error="responseError"
      @delete-documentation-unit="emit('deleteDocumentUnit', $event, procedure)"
    >
    </DocumentUnitList>
  </div>
  <div v-else class="pt-36 pr-48 pb-12 pl-40">
    <div class="flex w-full items-center justify-center bg-blue-200 py-16">
      <span>Keine Dokeinheiten sind zugewiesen.</span>
    </div>
  </div>
</template>
