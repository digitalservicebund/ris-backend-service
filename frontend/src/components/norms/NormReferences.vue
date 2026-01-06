<script lang="ts" setup>
import dayjs from "dayjs"
import { watch } from "vue"
import EditableList from "@/components/EditableList.vue"
import NormReferenceInput from "@/components/norms/NormReferenceInput.vue"
import NormReferenceSummary from "@/components/norms/NormReferenceSummary.vue"
import NormReference from "@/domain/normReference"

import SingleNorm from "@/domain/singleNorm"

defineProps<{
  label: string
  id: string
}>()

const norms = defineModel<NormReference[]>()

watch(norms, () => {
  norms.value?.forEach((norm) =>
    removeDuplicateSingleNorms(norm as unknown as NormReference),
  )
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

function handleRemoveSingleNorm({
  data,
  index,
}: {
  data: NormReference
  index: number
}) {
  if (!norms.value) return

  const normIndex = norms.value.findIndex((n) => n.localId === data.localId)

  if (normIndex !== -1) {
    const targetNorm = norms.value[normIndex]
    targetNorm.singleNorms?.splice(index, 1)
  }
}
</script>
<template>
  <div :aria-label="label">
    <h2 :id="id" class="ris-label1-bold mb-16">
      {{ label }}
    </h2>
    <div class="flex flex-row">
      <div class="flex-1">
        <EditableList
          v-model="norms"
          :create-entry="() => new NormReference()"
          :edit-component="NormReferenceInput"
          :summary-component="NormReferenceSummary"
          ><template #summary="{ entry }">
            <NormReferenceSummary
              :data="entry"
              @remove-single-norm="handleRemoveSingleNorm"
            /> </template
        ></EditableList>
      </div>
    </div>
  </div>
</template>
