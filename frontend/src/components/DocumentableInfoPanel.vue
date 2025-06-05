<script setup lang="ts" generic="TDocument">
import dayjs from "dayjs"
import { computed, ref, toRaw, watchEffect } from "vue"
import { RouteLocationRaw, useRoute } from "vue-router"
import IconBadge from "@/components/IconBadge.vue"
import { Documentable } from "@/components/input/types"
import SaveButton from "@/components/SaveDocumentUnitButton.vue"
import { useInternalUser } from "@/composables/useInternalUser"
import { useStatusBadge } from "@/composables/useStatusBadge"
import DocumentUnit from "@/domain/documentUnit"
import IconError from "~icons/ic/baseline-error"

interface Props<T extends Documentable> {
  document: T
  duplicateManagementRoute?: RouteLocationRaw
  onSave?: () => Promise<void>
}

const props = withDefaults(defineProps<Props<Documentable>>(), {
  duplicateManagementRoute: "",
  onSave: async () => {},
})

const route = useRoute()

const isInternalUser = useInternalUser()

const fileNumberInfo = computed(() => {
  return props.document.coreData.fileNumbers?.[0] || ""
})

const decisionDateInfo = computed(() => {
  return props.document.coreData.decisionDate
    ? dayjs(props.document.coreData.decisionDate).format("DD.MM.YYYY")
    : ""
})

const hasPendingDuplicateWarning = computed(() => {
  if (props.document instanceof DocumentUnit) {
    return (props.document.managementData.duplicateRelations ?? []).some(
      (warning) => warning.status === "PENDING",
    )
  }
  return false
})

const courtInfo = computed(() => {
  return props.document.coreData.court?.label || ""
})

const formattedInfo = computed(() => {
  const parts = [
    courtInfo.value,
    fileNumberInfo.value,
    decisionDateInfo.value,
  ].filter((part) => part.trim() !== "")
  return parts.join(", ")
})

const statusBadge = ref(useStatusBadge(props.document.status).value)

const isRouteWithSaveButton = computed(
  () =>
    route.path.includes("categories") ||
    route.path.includes("attachments") ||
    route.path.includes("references") ||
    route.path.includes("managementdata"),
)

const hasErrorStatus = computed(() => props.document.status?.withError)

watchEffect(() => {
  statusBadge.value = useStatusBadge(props.document.status).value
})
</script>

<template>
  <div
    class="sticky top-0 z-30 flex h-[64px] flex-row items-center border-b border-solid border-gray-400 bg-blue-100 px-24 py-12"
    data-testid="document-unit-info-panel"
  >
    <h1 class="ris-body1-bold">{{ props.document.documentNumber }}</h1>
    <span v-if="formattedInfo.length > 0" class="m-4"> | </span>
    <span
      class="overflow-hidden text-ellipsis whitespace-nowrap"
      data-testid="document-unit-info-panel-items"
    >
      {{ formattedInfo }}</span
    >
    <IconBadge
      v-if="statusBadge"
      :background-color="statusBadge.backgroundColor"
      class="ml-12"
      :color="statusBadge.color"
      :icon="toRaw(statusBadge.icon)"
      :label="statusBadge.label"
    />
    <IconBadge
      v-if="hasErrorStatus"
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
        :to="props.duplicateManagementRoute"
      >
        Bitte prüfen</RouterLink
      >
      <span v-if="isRouteWithSaveButton && isInternalUser">|</span>
    </div>
    <SaveButton
      v-if="isRouteWithSaveButton"
      aria-label="Speichern Button"
      data-testid="document-unit-save-button"
      @click="props.onSave"
    />
  </div>
</template>
