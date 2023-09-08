<script lang="ts" setup>
import dayjs from "dayjs"
import { storeToRefs } from "pinia"
import { toRefs, watchEffect, onUnmounted, computed } from "vue"
import { RouterView, useRoute, useRouter } from "vue-router"
import NormUnitInfoPanel from "@/components/NormUnitInfoPanel.vue"
import { useNormMenuItems } from "@/composables/useNormMenuItems"
import { useToggleStateInRouteQuery } from "@/composables/useToggleStateInRouteQuery"
import NavbarSide from "@/shared/components/NavbarSide.vue"
import SideToggle from "@/shared/components/SideToggle.vue"
import { useLoadedNormStore } from "@/stores/loadedNorm"

const props = defineProps<{ normGuid: string }>()

const route = useRoute()
const router = useRouter()
const { normGuid } = toRefs(props)
const goBackRoute = { name: "norms" }
const navigationIsOpen = useToggleStateInRouteQuery(
  "showNavBar",
  route,
  router.replace,
)
const store = useLoadedNormStore()
const { findDocumentation } = useLoadedNormStore()
const { loadedNorm } = storeToRefs(store)

const normIsExportable = computed(
  () => (loadedNorm.value?.files ?? []).length > 0,
)

const announcementInfo = computed(() => {
  const gazette =
    loadedNorm.value?.metadataSections?.OFFICIAL_REFERENCE?.[0]
      ?.PRINT_ANNOUNCEMENT?.[0]?.ANNOUNCEMENT_GAZETTE?.[0]
  const page =
    loadedNorm.value?.metadataSections?.OFFICIAL_REFERENCE?.[0]
      ?.PRINT_ANNOUNCEMENT?.[0]?.PAGE?.[0]
  return gazette && page ? `${gazette} S. ${page}` : undefined
})

const propertyInfos = computed(() => [
  {
    label: "Fundstelle",
    value: announcementInfo.value ? String(announcementInfo.value) : "",
  },
  {
    label: "FNA",
    value:
      loadedNorm.value?.metadataSections?.SUBJECT_AREA?.[0]?.SUBJECT_FNA?.[0],
  },
  {
    label: "Inkrafttreten",
    value: entryIntoForceInfo.value ? String(entryIntoForceInfo.value) : "",
  },
])

const ENTRY_INTO_FORCE_DATE_MAP: Record<string, string> = {
  UNDEFINED_UNKNOWN: "unbestimmt (unbekannt)",
  UNDEFINED_FUTURE: "unbestimmt (zukünftig)",
  UNDEFINED_NOT_PRESENT: "nicht vorhanden",
}

const entryIntoForceInfo = computed(() => {
  const entryIntoForceItem =
    loadedNorm.value?.metadataSections?.ENTRY_INTO_FORCE?.find(
      (item) => item.DATE,
    )
  const undefinedDateItem =
    loadedNorm.value?.metadataSections?.ENTRY_INTO_FORCE?.find(
      (item) => item.UNDEFINED_DATE,
    )

  if (entryIntoForceItem) {
    const entryIntoForceDate = entryIntoForceItem.DATE?.[0]
    if (entryIntoForceDate) {
      return dayjs(entryIntoForceDate).format("DD.MM.YYYY")
    }
  } else if (undefinedDateItem) {
    const undefinedDate = undefinedDateItem.UNDEFINED_DATE?.[0]
    if (undefinedDate) {
      return ENTRY_INTO_FORCE_DATE_MAP[undefinedDate] || undefinedDate
    }
  }

  return undefined
})

watchEffect(() => store.load(props.normGuid))
onUnmounted(() => (loadedNorm.value = undefined))

const documentationRouteIsOpen = computed(
  () => route.name == "norms-norm-normGuid-documentation-documentationGuid",
)
const openDocumentationGuid = computed(() =>
  documentationRouteIsOpen.value
    ? route.params.documentationGuid?.toString()
    : undefined,
)
const openDocumentation = findDocumentation(openDocumentationGuid)

const menuItems = useNormMenuItems(
  normGuid,
  route,
  openDocumentation,
  normIsExportable,
)
</script>

<template>
  <div class="flex w-screen grow overflow-hidden">
    <SideToggle v-model:is-expanded="navigationIsOpen" label="Navigation">
      <NavbarSide
        go-back-label="Zur Übersicht"
        :go-back-route="goBackRoute"
        :menu-items="menuItems"
      />
    </SideToggle>

    <div
      v-if="loadedNorm"
      class="w-full border-l-1 border-gray-400 bg-gray-100"
    >
      <NormUnitInfoPanel
        :heading="loadedNorm.metadataSections?.NORM?.[0]?.RIS_ABBREVIATION?.[0]"
        :property-infos="propertyInfos"
      />
      <RouterView class="p-48" />
    </div>

    <span v-else>Lade Norm...</span>
  </div>
</template>
