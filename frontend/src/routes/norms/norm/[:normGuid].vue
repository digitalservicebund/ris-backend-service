<script lang="ts" setup>
import dayjs from "dayjs"
import { storeToRefs } from "pinia"
import { toRefs, watchEffect, onUnmounted, computed } from "vue"
import { RouterView, useRoute, useRouter } from "vue-router"
import DocumentUnitInfoPanel from "@/components/DocumentUnitInfoPanel.vue"
import NavbarSide from "@/components/NavbarSide.vue"
import SideToggle from "@/components/SideToggle.vue"
import { useNormMenuItems } from "@/composables/useNormMenuItems"
import { useToggleStateInRouteQuery } from "@/composables/useToggleStateInRouteQuery"
import { useLoadedNormStore } from "@/stores/loadedNorm"

const props = defineProps<{ normGuid: string }>()

const route = useRoute()
const router = useRouter()
const { normGuid } = toRefs(props)
const menuItems = useNormMenuItems(normGuid, route)
const goBackRoute = { name: "norms" }
const navigationIsOpen = useToggleStateInRouteQuery(
  "showNavBar",
  route,
  router.replace
)
const store = useLoadedNormStore()
const { loadedNorm } = storeToRefs(store)

const ENTRY_INTO_FORCE_DATE_MAP: Record<string, string> = {
  UNDEFINED_UNKNOWN: "unbestimmt (unbekannt)",
  UNDEFINED_FUTURE: "unbestimmt (zukünftig)",
  UNDEFINED_NOT_PRESENT: "nicht vorhanden",
}

const announcementInfo = computed(() => {
  const gazette = loadedNorm.value?.printAnnouncementGazette
  const page = loadedNorm.value?.printAnnouncementPage
  return gazette && page ? `${gazette} S. ${page}` : undefined
})

const entryIntoForceInfo = computed(() => {
  if (loadedNorm.value?.entryIntoForceDate) {
    return dayjs(loadedNorm.value.entryIntoForceDate).format("DD.MM.YYYY")
  } else if (loadedNorm.value?.entryIntoForceDateState) {
    return ENTRY_INTO_FORCE_DATE_MAP[loadedNorm.value.entryIntoForceDateState]
  } else {
    return undefined
  }
})

const propertyInfos = computed(() => [
  { label: "Fundstelle", value: announcementInfo.value },
  { label: "FNA", value: loadedNorm.value?.subjectFna },
  { label: "Inkrafttreten", value: entryIntoForceInfo.value },
])

watchEffect(() => store.load(props.normGuid))
onUnmounted(() => (loadedNorm.value = undefined))
</script>

<template>
  <div class="flex grow overflow-hidden w-screen">
    <SideToggle v-model:is-expanded="navigationIsOpen" label="Navigation">
      <NavbarSide
        go-back-label="Zur Übersicht"
        :go-back-route="goBackRoute"
        :menu-items="menuItems"
      />
    </SideToggle>

    <div
      v-if="loadedNorm"
      class="bg-gray-100 border-gray-400 border-l-1 w-full"
    >
      <DocumentUnitInfoPanel
        :heading="loadedNorm.risAbbreviation"
        :property-infos="propertyInfos"
      />
      <RouterView class="p-48" />
    </div>

    <span v-else>Lade Norm...</span>
  </div>
</template>
