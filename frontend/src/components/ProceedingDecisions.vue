<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { Ref } from "vue"
import EnsuingDecisions from "@/components/EnsuingDecisions.vue"
import PreviousDecisions from "@/components/PreviousDecisions.vue"
import TitleElement from "@/components/TitleElement.vue"
import { DocumentationUnit } from "@/domain/documentationUnit"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { isDecision } from "@/utils/typeGuards"

const store = useDocumentUnitStore()
const { documentUnit } = storeToRefs(store) as {
  documentUnit: Ref<DocumentationUnit | undefined>
}
</script>

<template>
  <div class="flex flex-col gap-24 bg-white p-24">
    <TitleElement>Rechtszug</TitleElement>
    <PreviousDecisions id="previousDecisions" />
    <EnsuingDecisions v-if="isDecision(documentUnit)" id="ensuingDecisions" />
  </div>
</template>
