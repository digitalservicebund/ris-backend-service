<script setup lang="ts">
import { storeToRefs } from "pinia"
import Tooltip from "@/components/Tooltip.vue"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import BaselineArrowOutward from "~icons/ic/baseline-arrow-outward"

defineProps<{
  category: string
}>()
const { documentUnit } = storeToRefs(useDocumentUnitStore())

function categoriesRoute(category: string) {
  return {
    name: "caselaw-documentUnit-documentNumber-categories",
    hash: `#${category}`,
    params: { documentNumber: documentUnit.value!.documentNumber },
  }
}
</script>

<template>
  <Tooltip text="Zum Rechtschreibfehler springen">
    <RouterLink :to="categoriesRoute(category)">
      <button
        aria-label="jump-to-match"
        class="ds-link-01-bold whitespace-nowrap leading-24 no-underline focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
      >
        <BaselineArrowOutward class="mb-4 inline w-24" />
      </button>
    </RouterLink>
  </Tooltip>
</template>
