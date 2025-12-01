<script lang="ts" setup>
import dayjs from "dayjs"
import { computed } from "vue"
import BorderNumberLinkView from "@/components/BorderNumberLinkView.vue"
import IconBadge from "@/components/IconBadge.vue"
import Correction from "@/domain/correction"
import IconError from "~icons/ic/baseline-error"
import IconOutlineFileDownloadDone from "~icons/ic/outline-file-download-done"

const props = defineProps<{
  data: Correction
}>()

const correction = computed(() => new Correction(props.data))

function formatDate(date: string) {
  return dayjs(date, "YYYY-MM-DD", true).format("DD.MM.YYYY")
}
</script>

<template>
  <div class="flex w-full justify-between">
    <div class="flex flex-row items-center">
      <IconOutlineFileDownloadDone class="mr-8" />
      {{ correction.type }}
      <span v-if="correction.description">, {{ correction.description }}</span>
      <span v-if="correction.date">, {{ formatDate(correction.date) }}</span>
      <span v-if="correction.borderNumbers?.length" class="mx-4">|</span>
      <span v-if="correction.borderNumbers" class="flex flex-row gap-4">
        <BorderNumberLinkView
          v-for="borderNumber in correction.borderNumbers"
          :key="borderNumber"
          :border-number="borderNumber"
        />
      </span>
      <IconBadge
        v-if="correction.hasMissingRequiredFields"
        background-color="bg-red-300"
        class="ml-4"
        :icon="IconError"
        icon-color="text-red-900"
        label="Fehlende Daten"
      />
    </div>
  </div>
</template>
