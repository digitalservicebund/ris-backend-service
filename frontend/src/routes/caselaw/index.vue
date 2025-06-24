<script lang="ts" setup>
import Button from "primevue/button"
import { useRouter } from "vue-router"
import DocumentUnitSearch from "@/components/DocumentUnitSearch.vue"
import { useInternalUser } from "@/composables/useInternalUser"

const router = useRouter()
const isInternalUser = useInternalUser()
</script>

<template>
  <div class="flex flex-col gap-16 p-24">
    <div class="flex justify-between">
      <h1 class="ris-heading2-regular">Übersicht Rechtsprechung</h1>

      <Button
        v-if="isInternalUser"
        label="Neue Dokumentationseinheit"
        @click="router.push({ name: 'caselaw-pending-proceeding-new' })"
      ></Button>
    </div>
    <DocumentUnitSearch />
  </div>
</template>
