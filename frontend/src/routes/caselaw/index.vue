<script lang="ts" setup>
import { useRouter } from "vue-router"
import DocumentUnitSearch from "@/components/DocumentUnitSearch.vue"
import TextButton from "@/components/TextButton.vue"

const router = useRouter()
</script>

<template>
  <div class="flex flex-col gap-16 p-16">
    <div class="flex justify-between">
      <h1 class="ds-heading-02-reg">Übersicht Rechtsprechung</h1>

      <TextButton
        label="Neue Dokumentationseinheit"
        @click="router.push({ name: 'caselaw-documentUnit-new' })"
      />
    </div>
    <DocumentUnitSearch />
  </div>
</template>
