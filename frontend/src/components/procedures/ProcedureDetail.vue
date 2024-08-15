<script lang="ts" setup>
import { computed } from "vue"
import DocumentUnitList from "@/components/DocumentUnitList.vue"
import DropdownInput from "@/components/input/DropdownInput.vue"
import InputField from "@/components/input/InputField.vue"
import { Procedure } from "@/domain/documentUnit"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import { UserGroup } from "@/domain/userGroup"
import { ResponseError } from "@/services/httpClient"

const props = defineProps<{
  procedure: Procedure
  userGroups: UserGroup[]
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
  () =>
    !props.procedure.documentUnits &&
    props.procedure.documentationUnitCount > 0,
)
</script>

<template>
  <InputField
    id="emptyDropdown"
    v-slot="{ id }"
    label="Zugewiesene Benutzer:innen"
  >
    <DropdownInput
      :id="id"
      aria-label="dropdown input"
      :items="userGroups.map(({ name, id }) => ({ label: name, value: id }))"
      placeholder="Benutzer:innen zuweisen"
    />
  </InputField>
  <div
    v-if="procedure.documentationUnitCount > 0"
    class="pb-12 pl-24 pr-48 pt-36"
  >
    <DocumentUnitList
      class="grow"
      :document-unit-list-entries="procedure.documentUnits"
      is-deletable
      :is-loading="isLoading"
      :search-response-error="responseError"
      @delete-document-unit="emit('deleteDocumentUnit', $event, procedure)"
    >
    </DocumentUnitList>
  </div>
  <div v-else class="pb-12 pl-40 pr-48 pt-36">
    <div class="flex w-full items-center justify-center bg-blue-200 py-16">
      <span>Keine Dokeinheiten sind zugewiesen.</span>
    </div>
  </div>
</template>
