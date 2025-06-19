<script setup lang="ts">
import { storeToRefs } from "pinia"
import { computed, Ref } from "vue"
import ActiveCitations from "@/components/ActiveCitations.vue"
import CategoryWrapper from "@/components/CategoryWrapper.vue"
import { DocumentUnitCategoriesEnum } from "@/components/enumDocumentUnitCategories"
import FieldsOfLaw from "@/components/field-of-law/FieldsOfLaw.vue"
import KeyWords from "@/components/KeyWords.vue"
import Norms from "@/components/NormReferences.vue"
import OtherCategories from "@/components/OtherCategories.vue"
import TitleElement from "@/components/TitleElement.vue"
import { DocumentationUnit } from "@/domain/documentationUnit"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { isDecision } from "@/utils/typeGuards"

const store = useDocumentUnitStore()
const { documentUnit } = storeToRefs(store) as {
  documentUnit: Ref<DocumentationUnit | undefined>
}

const hasKeywords = computed(
  () =>
    !!store.documentUnit?.contentRelatedIndexing?.keywords &&
    store.documentUnit?.contentRelatedIndexing?.keywords?.length > 0,
)
</script>

<template>
  <div class="flex flex-col gap-24 bg-white p-24">
    <TitleElement>Inhaltliche Erschließung</TitleElement>
    <CategoryWrapper
      :id="DocumentUnitCategoriesEnum.KEYWORDS"
      v-slot="slotProps"
      label="Schlagwörter"
      :should-show-button="!hasKeywords"
    >
      <KeyWords data-testid="keywords" @reset="slotProps.reset" />
    </CategoryWrapper>
    <FieldsOfLaw data-testid="fieldsOfLaw" />
    <Norms data-testid="norms" />
    <ActiveCitations
      v-if="isDecision(documentUnit)"
      data-testid="activeCitations"
    />
    <OtherCategories v-if="isDecision(documentUnit)" />
  </div>
</template>
