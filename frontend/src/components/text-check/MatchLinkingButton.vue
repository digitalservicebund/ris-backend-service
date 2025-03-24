<script setup lang="ts">
import { storeToRefs } from "pinia"
import { getCategoryLabel } from "@/components/text-check/categoryLabels"
import Tooltip from "@/components/Tooltip.vue"
import { useScroll } from "@/composables/useScroll"
import router from "@/router"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { Match } from "@/types/textCheck"
import BaselineArrowOutward from "~icons/ic/baseline-arrow-outward"

const props = defineProps<{
  match: Match
  jumpToMatch?: (match: Match) => void
}>()

const { scrollIntoViewportById } = useScroll()

const { documentUnit } = storeToRefs(useDocumentUnitStore())

async function navigateToCategoriesAndJumpToMatch(match: Match) {
  await router.push({
    name: "caselaw-documentUnit-documentNumber-categories",
    params: {
      documentNumber: documentUnit.value!.documentNumber,
    },
  })
  await scrollIntoViewportById(match.category)
  if (props.jumpToMatch) {
    props.jumpToMatch(match)
  }
}
</script>

<template>
  <Tooltip :text="`Zu ${getCategoryLabel(match.category)} springen`">
    <button
      aria-label="jump-to-match"
      class="ds-link-01-bold whitespace-nowrap leading-24 no-underline focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
      @click="() => navigateToCategoriesAndJumpToMatch(match)"
    >
      <BaselineArrowOutward class="mb-4 inline w-24" />
    </button>
  </Tooltip>
</template>
