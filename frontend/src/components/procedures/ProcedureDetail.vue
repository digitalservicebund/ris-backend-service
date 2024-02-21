<script lang="ts" setup>
import { computed } from "vue"
import DocumentUnitList from "@/components/DocumentUnitList.vue"
import LoadingSpinner from "@/components/LoadingSpinner.vue"
import { Procedure } from "@/domain/documentUnit"

const props = defineProps<{
  procedure: Procedure
}>()

const isLoading = computed(
  () => !props.procedure.documentUnits && props.procedure.documentUnitCount > 0,
)
</script>

<template>
  <div
    v-if="isLoading"
    class="grid justify-items-center bg-white bg-opacity-60"
  >
    <LoadingSpinner />
  </div>
  <div v-else-if="procedure.documentUnits" class="px-24 pb-12 pt-36">
    <DocumentUnitList
      class="grow"
      :document-unit-list-entries="procedure.documentUnits"
      :is-deletable="false"
      :is-loading="isLoading"
    >
    </DocumentUnitList>
  </div>
</template>
