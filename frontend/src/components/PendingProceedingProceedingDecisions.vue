<script lang="ts" setup>
import EnsuingDecisions from "@/components/EnsuingDecisions.vue"
import TitleElement from "@/components/TitleElement.vue"
</script>

<template>
  <div class="flex flex-col gap-24 bg-white p-24">
    <TitleElement>Rechtszug</TitleElement>
    <PendingProceedingProceedingDecisions id="previousDecisions" />
    <EnsuingDecisions id="ensuingDecisions" />
  </div>
</template>
