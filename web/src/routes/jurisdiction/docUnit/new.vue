<script setup lang="ts">
import { onBeforeMount, ref } from "vue"
import { useRouter } from "vue-router"
import DocUnit from "@/domain/docUnit"
import docUnitService from "@/services/docUnitService"

const router = useRouter()
const status = ref<string | DocUnit>("Dokumentationseinheit wird erstellt...")

onBeforeMount(async () => {
  const newDocUnit = await docUnitService.createNew("KO", "RE")
  status.value = newDocUnit
  await router.replace({
    name: "jurisdiction-docUnit-:documentNumber-files",
    params: { documentNumber: newDocUnit.documentnumber },
  })
})
</script>

<template>
  <div>{{ status }}</div>
</template>
