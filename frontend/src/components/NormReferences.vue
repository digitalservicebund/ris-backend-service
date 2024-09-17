<script lang="ts" setup>
import * as Sentry from "@sentry/vue"
import { computed } from "vue"
import EditableList from "@/components/EditableList.vue"
import NormReferenceInput from "@/components/NormReferenceInput.vue"
import NormReferenceSummary from "@/components/NormReferenceSummary.vue"
import NormReference from "@/domain/normReference"

import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const store = useDocumentUnitStore()

const norms = computed({
  get: () => store.documentUnit!.contentRelatedIndexing.norms,
  set: (newValues) => {
    store.documentUnit!.contentRelatedIndexing.norms = newValues?.filter(
      (value) => {
        if (Object.keys(value).length === 0) {
          Sentry.captureMessage(
            "NormReference list contains empty objects",
            "error",
          )
          return false
        } else {
          return true
        }
      },
    )
  },
})

const defaultValue = new NormReference() as NormReference
</script>
<template>
  <div aria-label="Norm" class="border-b-1 border-blue-300">
    <h2 class="ds-label-01-bold mb-16">Normen</h2>
    <div class="flex flex-row">
      <div class="flex-1">
        <EditableList
          v-model="norms"
          :default-value="defaultValue"
          :edit-component="NormReferenceInput"
          :summary-component="NormReferenceSummary"
        />
      </div>
    </div>
  </div>
</template>
