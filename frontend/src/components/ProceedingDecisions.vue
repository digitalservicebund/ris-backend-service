<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { Ref } from "vue"
import EnsuingDecisions from "@/components/EnsuingDecisions.vue"
import PreviousDecisions from "@/components/PreviousDecisions.vue"
import TitleElement from "@/components/TitleElement.vue"
import { Decision } from "@/domain/decision"
import PendingProceeding from "@/domain/pendingProceeding"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { isDocumentUnit } from "@/utils/typeGuards"

const store = useDocumentUnitStore()
const { documentUnit } = storeToRefs(store) as {
  documentUnit: Ref<Decision | PendingProceeding | undefined>
}
</script>

<template>
  <div class="flex flex-col gap-24 bg-white p-24">
    <TitleElement>Rechtszug</TitleElement>
    <PreviousDecisions id="previousDecisions" />
    <EnsuingDecisions
      v-if="isDocumentUnit(documentUnit)"
      id="ensuingDecisions"
    />
  </div>
</template>
