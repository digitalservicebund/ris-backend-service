<script lang="ts" setup>
import * as Sentry from "@sentry/vue"
import dayjs from "dayjs"
import { computed } from "vue"
import EditableList from "@/components/EditableList.vue"
import { DocumentUnitCategoriesEnum } from "@/components/enumDocumentUnitCategories"
import NormReferenceInput from "@/components/NormReferenceInput.vue"
import NormReferenceSummary from "@/components/NormReferenceSummary.vue"
import NormReference from "@/domain/normReference"

import SingleNorm from "@/domain/singleNorm"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const store = useDocumentUnitStore()

const norms = computed({
  get: () => store.documentUnit!.contentRelatedIndexing.norms,
  set: async (newValues) => {
    store.documentUnit!.contentRelatedIndexing.norms = newValues?.filter(
      (value) => {
        if (Object.keys(value).length === 0) {
          Sentry.captureMessage(
            "NormReference list contains empty objects",
            "error",
          )
          return false
        }
        removeDuplicateSingleNorms(value as NormReference)
        return true // Keep the value in the norms array
      },
    )
  },
})

function removeDuplicateSingleNorms(norm: NormReference): void {
  if (!norm.singleNorms || !Array.isArray(norm.singleNorms)) {
    return // Exit if singleNorms is not an array
  }

  const uniqueSingleNorms = new Set<string>()

  norm.singleNorms = norm.singleNorms.filter((singleNorm) => {
    const uniqueKey = generateUniqueSingleNormKey(singleNorm)

    // Check uniqueness and add to the set if it's new
    if (!uniqueSingleNorms.has(uniqueKey)) {
      uniqueSingleNorms.add(uniqueKey)
      return true // Keep this singleNorm
    }
    return false // Filter out duplicates
  })
}

function generateUniqueSingleNormKey(singleNorm: SingleNorm): string {
  return JSON.stringify({
    singleNorm: singleNorm.singleNorm ?? "",
    dateOfVersion: singleNorm.dateOfVersion
      ? dayjs(singleNorm.dateOfVersion).format("DD.MM.YYYY")
      : "",
    dateOfRelevance: singleNorm.dateOfRelevance ?? "",
  })
}
</script>
<template>
  <div aria-label="Norm">
    <h2 :id="DocumentUnitCategoriesEnum.NORMS" class="ris-label1-bold mb-16">
      Normen
    </h2>
    <div class="flex flex-row">
      <div class="flex-1">
        <EditableList
          v-model="norms"
          :create-entry="() => new NormReference()"
          :edit-component="NormReferenceInput"
          :summary-component="NormReferenceSummary"
        />
      </div>
    </div>
  </div>
</template>
