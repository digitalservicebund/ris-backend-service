<script lang="ts" setup>
import { InfoStatus } from "@/shared/components/enumInfoStatus"
import InfoModal from "@/shared/components/InfoModal.vue"
</script>

<template>
  <div class="flex flex-col items-start gap-y-20">
    <h1 class="text-20 font-bold">Error Modal</h1>
    <InfoModal
      description="The messages are shown here."
      title="The messages title is shown here"
    ></InfoModal>
    <h1 class="text-20 font-bold">Succeed Modal</h1>
    <InfoModal
      description="The messages are shown here."
      :status="InfoStatus.SUCCEED"
      title="The messages title is shown here"
    ></InfoModal>
  </div>
</template>
