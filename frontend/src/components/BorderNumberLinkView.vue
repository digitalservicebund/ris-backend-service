<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { computed } from "vue"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const props = defineProps<{
  borderNumber: number
}>()

const { documentUnit } = storeToRefs(useDocumentUnitStore())

const borderNumberExists = computed(() =>
  documentUnit.value?.managementData.borderNumbers.includes(
    `${props.borderNumber}`,
  ),
)

const borderNumberLinkClasses = computed(() => {
  return borderNumberExists.value
    ? 'text-white bg-blue-700 before:content-["Rd_"] pr-2 pl-2'
    : 'text-red-900 bg-red-200 before:content-["⚠Rd_"] pr-2 pl-2'
})
</script>
<template>
  <span :class="borderNumberLinkClasses">{{ borderNumber }}</span>
</template>
