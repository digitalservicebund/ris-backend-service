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

const fileNumberInfo = computed(
  () => props.documentUnit.coreData.fileNumbers?.[0],
)

const decisionDateInfo = computed(() =>
  props.documentUnit.coreData.decisionDate
    ? dayjs(props.documentUnit.coreData.decisionDate).format("DD.MM.YYYY")
    : undefined,
)

const documentationOffice = computed(
  () => props.documentUnit.coreData.documentationOffice?.abbreviation,
)

const courtInfo = computed(() => props.documentUnit.coreData.court?.label)

const statusBadge = ref(useStatusBadge(props.documentUnit.status).value)

const firstRowInfos = computed(() => [
  ...(statusBadge.value ? [statusBadge.value] : []),
  {
    label: "Dokumentationsstelle",
    value: documentationOffice.value,
  },
])

const secondRowInfos = computed(() => [
  { label: "Aktenzeichen", value: fileNumberInfo.value },
  { label: "Entscheidungsdatum", value: decisionDateInfo.value },
  { label: "Gericht", value: courtInfo.value },
])

watchEffect(() => {
  statusBadge.value = useStatusBadge(props.documentUnit.status).value
})
</script>

<template>
  <div class="flex w-screen grow">
    <div class="flex w-full flex-col bg-gray-100">
      <DocumentUnitInfoPanel
        :document-unit="documentUnit"
        :first-row="firstRowInfos"
        :heading="documentUnit.documentNumber ?? ''"
        :save-callback="saveCallback"
        :second-row="secondRowInfos"
      />

      <div class="flex grow flex-col items-start">
        <slot :classes="['p-24 w-full grow']" />
      </div>
    </div>
  </div>
</template>
