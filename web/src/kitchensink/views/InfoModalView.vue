<script lang="ts" setup>
import InfoModal from "@/components/InfoModal.vue"
import { InfoStatus } from "@/enum/enumInfoStatus"
</script>

<template>
  <div class="flex flex-col gap-y-20 items-start">
    <h1 class="font-bold text-20">Error Modal</h1>
    <InfoModal
      description="The messages are shown here."
      title="The messages title is shown here"
    ></InfoModal>
    <h1 class="font-bold text-20">Succeed Modal</h1>
    <InfoModal
      description="The messages are shown here."
      :status="InfoStatus.SUCCEED"
      title="The messages title is shown here"
    ></InfoModal>
  </div>
</template>
