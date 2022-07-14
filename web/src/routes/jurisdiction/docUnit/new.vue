<script setup lang="ts">
import { onBeforeMount } from "vue"
import { useRouter } from "vue-router"
import docUnitService from "@/services/docUnitService"

const router = useRouter()

onBeforeMount(async () => {
  const newDocUnit = await docUnitService.createNew("KO", "RE")
  await router.replace({
    name: "jurisdiction-docUnit-:documentNumber-files",
    params: { documentNumber: newDocUnit.documentnumber },
  })
})
</script>

<template>
  <div></div>
</template>
