<script setup lang="ts">
import { onBeforeMount } from "vue"
import { useRouter } from "vue-router"
import LegalPeriodicalEdition from "@/domain/legalPeriodicalEdition"
import legalPeriodicalEditionService from "@/services/legalPeriodicalEditionService"

const props = defineProps<{
  legalPeriodicalId: string
}>()
const router = useRouter()
onBeforeMount(async () => {
  const createResponse = await legalPeriodicalEditionService.save(
    new LegalPeriodicalEdition({
      legalPeriodical: { uuid: props.legalPeriodicalId },
    }),
  )
  if (createResponse.data)
    await router.replace({
      name: "caselaw-legal-periodical-editionId-edition",
      params: { editionId: createResponse.data.id },
    })
})
</script>

<template>
  <div></div>
</template>
