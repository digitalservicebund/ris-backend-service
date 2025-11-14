<script lang="ts" setup>
import { computed } from "vue"
import { CollectiveAgreement } from "@/domain/collectiveAgreement"
import IconOutlineDescription from "~icons/ic/outline-description"

const props = defineProps<{
  data: CollectiveAgreement
}>()

const collectiveAgreement = computed(() => new CollectiveAgreement(props.data))
</script>

<template>
  <div class="flex w-full justify-between">
    <div class="flex flex-row items-center">
      <IconOutlineDescription class="mr-8" />
      {{ collectiveAgreement.renderSummary }}
    </div>
  </div>
</template>
