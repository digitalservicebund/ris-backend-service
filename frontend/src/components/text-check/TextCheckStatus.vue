<script setup lang="ts">
import LoadingSpinner from "@/components/LoadingSpinner.vue"
import { ResponseError } from "@/services/httpClient"
import IconErrorOutline from "~icons/ic/baseline-error-outline"

interface Props {
  loading?: boolean
  responseError?: ResponseError
}

defineProps<Props>()
</script>
<template>
  <div class="flex w-full flex-col items-end">
    <div v-if="loading">
      <div class="flex items-center gap-x-8">
        <span class="text-gray-900">Rechtschreibprüfung wird geprüft</span>
        <LoadingSpinner class="mr-4 border-gray-900" size="extra-small" />
      </div>
    </div>
    <div v-else-if="responseError">
      <div class="flex items-center gap-x-8">
        <span class="text-gray-900">{{ responseError?.title }}</span>
        <IconErrorOutline class="text-gray-900" />
      </div>
    </div>
  </div>
</template>
