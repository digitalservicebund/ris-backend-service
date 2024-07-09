<script setup lang="ts">
import ActiveCitations from "@/components/ActiveCitations.vue"
import FieldOfLawMain from "@/components/FieldOfLawMain.vue"
import KeyWords from "@/components/KeyWords.vue"
import Norms from "@/components/NormReferences.vue"
</script>

<template>
  <div class="flex flex-col">
    <KeyWords />
    <FieldOfLawMain />
    <Norms />
    <ActiveCitations />
  </div>
</template>
