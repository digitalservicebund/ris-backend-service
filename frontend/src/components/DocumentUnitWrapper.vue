<script setup lang="ts">
import dayjs from "dayjs"
import { computed, ref, watchEffect } from "vue"
import DocumentUnitInfoPanel from "@/components/DocumentUnitInfoPanel.vue"
import { useStatusBadge } from "@/composables/useStatusBadge"
import DocumentUnit from "@/domain/documentUnit"
import { ServiceResponse } from "@/services/httpClient"

const props = defineProps<{
  documentUnit: DocumentUnit
  saveCallback?: () => Promise<ServiceResponse<void>>
  showNavigationPanel: boolean
}>()
</script>

<template>
  <div class="flex w-screen grow">
    <slot :classes="['p-24 w-full grow']" />
  </div>
</template>
