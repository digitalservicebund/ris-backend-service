<script lang="ts" setup>
import dayjs from "dayjs"
import { computed, ref, toRaw, watchEffect } from "vue"
import IconBadge from "@/components/IconBadge.vue"
import SaveButton from "@/components/SaveDocumentUnitButton.vue"
import { useStatusBadge } from "@/composables/useStatusBadge"
import DocumentUnit from "@/domain/documentUnit"
import { ServiceResponse } from "@/services/httpClient"
import IconError from "~icons/ic/baseline-error"

interface Props {
  documentUnit: DocumentUnit
  heading?: string
  saveCallback?: () => Promise<ServiceResponse<void>>
}

const props = withDefaults(defineProps<Props>(), {
  heading: "",
  saveCallback: undefined,
})

const fileNumberInfo = computed(
  () => props.documentUnit?.coreData.fileNumbers?.[0] || "",
)

const decisionDateInfo = computed(() =>
  props.documentUnit?.coreData.decisionDate
    ? dayjs(props.documentUnit.coreData.decisionDate).format("DD.MM.YYYY")
    : "",
)

const courtInfo = computed(
  () => props.documentUnit?.coreData.court?.label || "",
)

const formattedInfo = computed(() => {
  const parts = [
    courtInfo.value,
    fileNumberInfo.value,
    decisionDateInfo.value,
  ].filter((part) => part.trim() !== "")
  return parts.join(", ")
})

const statusBadge = ref(useStatusBadge(props.documentUnit?.status).value)

watchEffect(() => {
  statusBadge.value = useStatusBadge(props.documentUnit?.status).value
})
</script>

<template>
  <div
    class="sticky top-0 z-30 flex h-[64px] flex-row items-center border-b border-solid border-gray-400 bg-blue-100 px-24 py-12"
  >
    <h1 class="text font-bold">{{ heading }}</h1>
    <span v-if="formattedInfo.length > 0" class="m-4"> | </span>
    <span
      class="overflow-hidden text-ellipsis whitespace-nowrap"
      data-testid="document-unit-info-panel-items"
    >
      {{ formattedInfo }}</span
    >
    <IconBadge
      :background-color="statusBadge.backgroundColor"
      class="ml-12"
      :color="statusBadge.color"
      :icon="toRaw(statusBadge.icon)"
      :label="statusBadge.label"
    />
    <IconBadge
      v-if="props.documentUnit?.status?.withError"
      background-color="bg-red-300"
      class="ml-12"
      color="text-red-900"
      :icon="IconError"
      label="Fehler"
    />

    <span class="flex-grow"></span>
    <SaveButton
      v-if="saveCallback"
      aria-label="Speichern Button"
      :service-callback="saveCallback"
    />
  </div>
</template>
