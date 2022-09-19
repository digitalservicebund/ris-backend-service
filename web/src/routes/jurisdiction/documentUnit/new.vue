<script setup lang="ts">
import { onBeforeMount } from "vue"
import { useRouter } from "vue-router"
import documentUnitService from "@/services/documentUnitService"

const router = useRouter()

onBeforeMount(async () => {
  const newDocumentUnit = (await documentUnitService.createNew("KO", "RE")).data
  await router.replace({
    name: "jurisdiction-documentUnit-:documentNumber-files",
    params: { documentNumber: newDocumentUnit.documentNumber },
  })
})
</script>

<template>
  <div></div>
</template>
