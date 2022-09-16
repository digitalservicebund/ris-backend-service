<script setup lang="ts">
import { onBeforeMount } from "vue"
import { useRouter } from "vue-router"
import documentUnitService from "@/services/documentUnitService"

const router = useRouter()

onBeforeMount(async () => {
  const newDocumentUnit = (await documentUnitService.createNew("KO", "RE")).data
  await router.replace({
    name: "jurisdiction-docUnit-:documentNumber-files",
    params: { documentNumber: newDocumentUnit.documentnumber },
  })
})
</script>

<template>
  <div></div>
</template>
