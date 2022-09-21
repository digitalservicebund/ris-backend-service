<script lang="ts" setup>
import { ref } from "vue"
import SaveDocumentUnitButton from "@/components/SaveDocumentUnitButton.vue"
import { UpdateStatus } from "@/enum/enumUpdateStatus"

const updateStatus = ref(UpdateStatus)
</script>

<template>
  <div class="flex flex-col gap-y-20">
    <p class="text-20">Status: Before update</p>
    <SaveDocumentUnitButton
      aria-label="Save Document Unit"
      :update-status="updateStatus.BEFORE_UPDATE"
    />
    <p class="text-20">Status: On update</p>
    <SaveDocumentUnitButton
      aria-label="Save Document Unit"
      :update-status="updateStatus.ON_UPDATE"
    />
    <p class="text-20">Status: Error</p>
    <SaveDocumentUnitButton
      aria-label="Save Document Unit"
      :update-status="updateStatus.ERROR"
    />
    <p class="text-20">Status: Succeed</p>
    <SaveDocumentUnitButton
      aria-label="Save Document Unit"
      :update-status="updateStatus.SUCCEED"
    />
  </div>
</template>
