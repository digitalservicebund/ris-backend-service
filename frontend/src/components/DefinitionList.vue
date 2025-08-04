<script lang="ts" setup>
import { storeToRefs } from "pinia"
import DefinitionInput from "@/components/DefinitionInput.vue"
import DefinitionSummary from "@/components/DefinitionSummary.vue"
import EditableList from "@/components/EditableList.vue"
import Definition from "@/domain/definition"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

defineProps<{
  label: string
}>()

const store = useDocumentUnitStore()
const { documentUnit: decision } = storeToRefs(store)
</script>

<template>
  <div id="definitions" class="ris-label2-regular mb-16">
    {{ label }}
  </div>
  <div
    aria-label="Definitionen"
    class="border-b-1 border-blue-300"
    data-testid="Definitionen"
  >
    <div class="flex flex-row">
      <div class="flex-1">
        <EditableList
          v-model="decision!.contentRelatedIndexing.definitions"
          :create-entry="() => new Definition()"
          :edit-component="DefinitionInput"
          :summary-component="DefinitionSummary"
        />
      </div>
    </div>
  </div>
</template>
