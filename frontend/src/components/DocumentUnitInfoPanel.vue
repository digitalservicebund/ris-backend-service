<script lang="ts" setup>
import dayjs from "dayjs"
import { computed, ref, toRaw, watchEffect } from "vue"
import { useRoute } from "vue-router"
import IconBadge from "@/components/IconBadge.vue"
import SaveButton from "@/components/SaveDocumentUnitButton.vue"
import { useInternalUser } from "@/composables/useInternalUser"
import { useStatusBadge } from "@/composables/useStatusBadge"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import IconError from "~icons/ic/baseline-error"

interface Props {
  heading?: string
}

const props = withDefaults(defineProps<Props>(), {
  heading: "",
})

const route = useRoute()

const documentUnitStore = useDocumentUnitStore()

const isInternalUser = useInternalUser()

const fileNumberInfo = computed(
  () => documentUnitStore.documentUnit?.coreData.fileNumbers?.[0] || "",
)

const decisionDateInfo = computed(() =>
  documentUnitStore.documentUnit?.coreData.decisionDate
    ? dayjs(documentUnitStore.documentUnit.coreData.decisionDate).format(
        "DD.MM.YYYY",
      )
    : "",
)

const hasPendingDuplicateWarning = computed(
  () =>
    documentUnitStore.documentUnit &&
    (
      documentUnitStore.documentUnit.managementData.duplicateRelations ?? []
    ).some((warning) => warning.status === "PENDING"),
)

const courtInfo = computed(
  () => documentUnitStore.documentUnit?.coreData.court?.label || "",
)

const formattedInfo = computed(() => {
  const parts = [
    courtInfo.value,
    fileNumberInfo.value,
    decisionDateInfo.value,
  ].filter((part) => part.trim() !== "")
  return parts.join(", ")
})

const isRouteWithSaveButton = computed(
  () =>
    route.path.includes("categories") ||
    route.path.includes("attachments") ||
    route.path.includes("references") ||
    route.path.includes("managementdata"),
)

const managementDataRoute = computed(() => ({
  name: "caselaw-documentUnit-documentNumber-managementdata",
  params: { documentNumber: documentUnitStore.documentUnit!.documentNumber },
}))

const statusBadge = ref(
  useStatusBadge(documentUnitStore.documentUnit?.status).value,
)

watchEffect(() => {
  statusBadge.value = useStatusBadge(
    documentUnitStore.documentUnit?.status,
  ).value
})
</script>

<template>
  <div
    class="sticky top-0 z-30 flex h-[64px] flex-row items-center border-b border-solid border-gray-400 bg-blue-100 px-24 py-12"
  >
    <h1 class="ris-body1-bold">{{ props.heading }}</h1>
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
      v-if="documentUnitStore.documentUnit?.status?.withError"
      background-color="bg-red-300"
      class="ml-12"
      color="text-red-900"
      :icon="IconError"
      label="Fehler"
    />

    <span class="flex-grow"></span>
    <div
      v-if="hasPendingDuplicateWarning"
      class="flex items-center gap-12 whitespace-nowrap"
    >
      <IconBadge
        background-color="bg-red-300"
        class="ml-12"
        color="text-red-900"
        data-testid="duplicate-icon"
        :icon="IconError"
        label="Dublettenverdacht"
      />
      <RouterLink
        v-if="isInternalUser"
        class="ris-link1-bold text-red-900"
        :to="managementDataRoute"
      >
        Bitte prüfen</RouterLink
      >
      <span v-if="isRouteWithSaveButton && isInternalUser">|</span>
    </div>
    <SaveButton
      v-if="isRouteWithSaveButton"
      aria-label="Speichern Button"
      data-testid="document-unit-save-button"
    />
  </div>
</template>
