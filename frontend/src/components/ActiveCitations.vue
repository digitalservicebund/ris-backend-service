<script lang="ts" setup>
import { computed } from "vue"
import ActiveCitationInput from "@/components/ActiveCitationInput.vue"
import ActiveCitationSummary from "@/components/ActiveCitationSummary.vue"
import EditableList from "@/components/EditableList.vue"
import { DocumentUnitCategoriesEnum } from "@/components/enumDocumentUnitCategories"
import ActiveCitation from "@/domain/activeCitation"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const store = useDocumentUnitStore()

const activeCitations = computed({
  get: () => store.documentUnit!.contentRelatedIndexing.activeCitations,
  set: (newValues) => {
    store.documentUnit!.contentRelatedIndexing.activeCitations = newValues
  },
})
</script>

<template>
  <div aria-label="Aktivzitierung">
    <h2
      :id="DocumentUnitCategoriesEnum.ACTIVE_CITATIONS"
      class="ris-label1-bold mb-16"
    >
      Aktivzitierung
    </h2>
    <EditableList
      v-model="activeCitations"
      :create-entry="() => new ActiveCitation()"
      :edit-component="ActiveCitationInput"
      :summary-component="ActiveCitationSummary"
    />
  </div>
</template>
