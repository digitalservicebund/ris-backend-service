<script setup lang="ts">
import ActiveCitations from "@/components/ActiveCitations.vue"
import FieldOfLawMain from "@/components/FieldOfLawMain.vue"
import KeyWords from "@/components/KeyWords.vue"
import Norms from "@/components/NormReferences.vue"
import OtherCategories from "@/components/OtherCategories.vue"
import TitleElement from "@/components/TitleElement.vue"
</script>

<template>
  <div class="flex flex-col gap-24 bg-white p-24">
    <TitleElement>Inhaltliche Erschließung</TitleElement>
    <KeyWords />
    <FieldOfLawMain />
    <Norms />
    <ActiveCitations />
    <OtherCategories />
  </div>
</template>
