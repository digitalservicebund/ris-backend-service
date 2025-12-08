<script setup lang="ts">
import { DocumentUnitCategoriesEnum } from "@/components/enumDocumentUnitCategories"
import NormsList from "@/components/norms/NormsList.vue"
import { contentRelatedIndexingLabels } from "@/domain/decision"
import NormReference from "@/domain/normReference"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const store = useDocumentUnitStore()
</script>

<template>
  <NormsList
    :id="DocumentUnitCategoriesEnum.NORMS"
    v-model="
      store.documentUnit!.contentRelatedIndexing.norms as NormReference[]
    "
    :label="contentRelatedIndexingLabels.norms"
  />
</template>
