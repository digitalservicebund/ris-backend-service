<script lang="ts" setup>
import { computed } from "vue"
import ActiveCitationInput from "@/components/ActiveCitationInput.vue"
import ActiveCitationSummary from "@/components/ActiveCitationSummary.vue"
import EditableList from "@/components/EditableList.vue"
import ActiveCitation from "@/domain/activeCitation"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const store = useDocumentUnitStore()

const activeCitations = computed({
  get: () => store.documentUnit!.contentRelatedIndexing.activeCitations,
  set: (newValues) => {
    store.documentUnit!.contentRelatedIndexing.activeCitations = newValues
  },
})

const defaultValue = new ActiveCitation()
</script>

<template>
  <div aria-label="Aktivzitierung" class="bg-white p-32">
    <h2 class="ds-heading-03-reg mb-24">Aktivzitierung</h2>
    <div class="flex flex-row">
      <div class="flex-1">
        <EditableList
          v-model="activeCitations"
          :default-value="defaultValue"
          :edit-component="ActiveCitationInput"
          :summary-component="ActiveCitationSummary"
        />
      </div>
    </div>
  </div>
</template>
