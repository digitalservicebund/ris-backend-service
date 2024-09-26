<script lang="ts" setup>
import ListInput from "@/components/input/listInput/ListInput.vue"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

defineProps<{
  label: string
}>()

const store = useDocumentUnitStore()
</script>

<template>
  <div class="gap-0">
    <ListInput
      id="collectiveAgreements"
      v-model="store.documentUnit!.contentRelatedIndexing.collectiveAgreements"
      :label="label"
    ></ListInput>
  </div>
</template>
