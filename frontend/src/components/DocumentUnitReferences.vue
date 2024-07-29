<script lang="ts" setup>
import { computed } from "vue"
import EditableList from "@/components/EditableList.vue"
import ReferenceInput from "@/components/ReferenceInput.vue"
import ReferenceSummary from "@/components/ReferenceSummary.vue"
import Reference from "@/domain/reference"

// Todo: uncomment when partial saving merged
// const references = computed({
//   get: () => {
//     return store.documentUnit?.references
//   },
//   set: (value) => {
//     store.documentUnit.references = value
//   },
// })

const references = computed(() => [])

const defaultValue = new Reference()
</script>

<template>
  <div aria-label="Fundstellen" class="w-full bg-white p-32">
    <h2 class="ds-heading-03-reg mb-24">Fundstellen</h2>
    <div class="flex flex-row">
      <EditableList
        v-model="references"
        :default-value="defaultValue"
        :edit-component="ReferenceInput"
        :summary-component="ReferenceSummary"
      />
    </div>
  </div>
</template>
