<script lang="ts" setup>
import EnsuingDecisions from "@/components/EnsuingDecisions.vue"
import { DocumentUnitCatagoriesEnum } from "@/components/enumDocumentUnitCatagories"
import PreviousDecisions from "@/components/PreviousDecisions.vue"
</script>

<template>
  <div class="flex flex-col gap-24 bg-white p-24">
    <h2 class="ds-heading-03-bold">Rechtszug</h2>
    <PreviousDecisions :id="DocumentUnitCatagoriesEnum.PROCEEDINGS_DECISIONS" />
    <EnsuingDecisions />
  </div>
</template>
