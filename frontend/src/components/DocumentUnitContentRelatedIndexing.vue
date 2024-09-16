<script setup lang="ts">
import ActiveCitations from "@/components/ActiveCitations.vue"
import FieldOfLawMain from "@/components/FieldOfLawMain.vue"
import KeyWords from "@/components/KeyWords.vue"
import Norms from "@/components/NormReferences.vue"
import OtherCategories from "@/components/OtherCategories.vue"
</script>

<template>
  <div class="flex flex-col gap-24 bg-white p-24">
    <h2 class="ds-heading-03-bold">Inhaltliche Erschließung</h2>
    <KeyWords />
    <FieldOfLawMain />
    <Norms />
    <ActiveCitations />
    <OtherCategories />
  </div>
</template>
