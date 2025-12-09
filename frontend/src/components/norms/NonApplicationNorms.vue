<script setup lang="ts">
import { DocumentUnitCategoriesEnum } from "@/components/enumDocumentUnitCategories"
import NormReferences from "@/components/norms/NormReferences.vue"
import { contentRelatedIndexingLabels } from "@/domain/decision"
import NormReference from "@/domain/normReference"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const store = useDocumentUnitStore()
</script>

<template>
  <NormReferences
    :id="DocumentUnitCategoriesEnum.NON_APPLICATION_NORMS"
    v-model="
      store.documentUnit!.contentRelatedIndexing
        .nonApplicationNorms as NormReference[]
    "
    :label="contentRelatedIndexingLabels.nonApplicationNorms"
  />
</template>
