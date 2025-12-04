<script lang="ts" setup>
import * as Sentry from "@sentry/vue"
import dayjs from "dayjs"
import { computed } from "vue"
import EditableList from "@/components/EditableList.vue"

import NonApplicationNormInput from "@/components/NonApplicationNormInput.vue"
import NonApplicationNormSummary from "@/components/NonApplicationNormSummary.vue"
import { contentRelatedIndexingLabels } from "@/domain/decision"
import NonApplicationNorm from "@/domain/nonApplicationNorm"
import SingleNorm from "@/domain/singleNorm"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const store = useDocumentUnitStore()

const nonApplicationNorms = computed({
  get: () => store.documentUnit!.contentRelatedIndexing.nonApplicationNorms,
  set: async (newValues) => {
    store.documentUnit!.contentRelatedIndexing.nonApplicationNorms =
      newValues?.filter((value) => {
        if (Object.keys(value).length === 0) {
          Sentry.captureMessage(
            "NonApplicationNorm list contains empty objects",
            "error",
          )
          return false
        }
        removeDuplicateSingleNorms(value as NonApplicationNorm)
        return true // Keep the value in the norms array
      })
  },
})

function removeDuplicateSingleNorms(norm: NonApplicationNorm): void {
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
  <div :aria-label="contentRelatedIndexingLabels.nonApplicationNorms">
    <h2 id="nonApplicationNorms" class="ris-label1-bold mb-16">
      Nichtanwendungsgesetz
    </h2>
    <div class="flex flex-row">
      <div class="flex-1">
        <EditableList
          v-model="nonApplicationNorms"
          :create-entry="() => new NonApplicationNorm()"
          :edit-component="NonApplicationNormInput"
          :summary-component="NonApplicationNormSummary"
        />
      </div>
    </div>
  </div>
</template>
