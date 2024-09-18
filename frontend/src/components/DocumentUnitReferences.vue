<script lang="ts" setup>
import { computed } from "vue"
import DocumentUnitReferenceInput from "@/components/DocumentUnitReferenceInput.vue"
import EditableList from "@/components/EditableList.vue"
import ReferenceSummary from "@/components/ReferenceSummary.vue"
import Reference from "@/domain/reference"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const store = useDocumentUnitStore()

const references = computed({
  get: () => store.documentUnit!.references as Reference[],
  set: (newValues) => {
    store.documentUnit!.references = newValues
  },
})

const defaultValue = new Reference() as Reference
</script>

<template>
  <div class="flex w-full flex-1 grow flex-col gap-24 p-24">
    <div aria-label="Fundstellen" class="bg-white p-24">
      <h2 class="ds-label-01-bold mb-16">Fundstellen bearbeiten</h2>
      <div class="flex flex-row">
        <EditableList
          v-model="references"
          :default-value="defaultValue"
          :edit-component="DocumentUnitReferenceInput"
          :summary-component="ReferenceSummary"
        />
      </div>
    </div>
  </div>
</template>
