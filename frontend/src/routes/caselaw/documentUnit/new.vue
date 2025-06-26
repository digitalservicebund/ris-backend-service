<script setup lang="ts">
import { onBeforeMount } from "vue"
import { useRouter } from "vue-router"
import documentUnitService from "@/services/documentUnitService"

const router = useRouter()

onBeforeMount(async () => {
  const createResponse = await documentUnitService.createNew()
  if (createResponse.data)
    await router.replace({
      name: "caselaw-documentUnit-documentNumber-attachments",
      params: { documentNumber: createResponse.data.documentNumber },
    })
})
</script>

<template>
  <div></div>
</template>
