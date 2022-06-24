<script setup lang="ts">
import { onBeforeMount } from "vue"
import { useRouter } from "vue-router"
import docUnitService from "../../../services/docUnitService"

const router = useRouter()

onBeforeMount(async () => {
  const newDocUnit = await docUnitService.createNew("BGH", "RE")
  await router.replace({
    name: "jurisdiction-docUnit-:id-files",
    params: { id: newDocUnit.id },
  })
})
</script>

<template>
  <div></div>
</template>
