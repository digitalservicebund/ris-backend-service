<script setup lang="ts">
import { onBeforeMount } from "vue"
import { useRouter } from "vue-router"
import { Kind } from "@/domain/documentationUnitKind"
import documentUnitService from "@/services/documentUnitService"

const router = useRouter()

onBeforeMount(async () => {
  const createResponse = await documentUnitService.createNew(undefined, {
    kind: Kind.PENDING_PROCEEDING,
  })
  if (createResponse.data)
    await router.replace({
      name: "caselaw-pending-proceeding-documentNumber-categories",
      params: { documentNumber: createResponse.data.documentNumber },
    })
})
</script>

<template>
  <div></div>
</template>
