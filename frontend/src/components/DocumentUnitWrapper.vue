<script setup lang="ts">
import dayjs from "dayjs"
import { computed } from "vue"
import { useRoute, useRouter } from "vue-router"
import { useCaseLawMenuItems } from "@/composables/useCaseLawMenuItems"
import { useToggleStateInRouteQuery } from "@/composables/useToggleStateInRouteQuery"
import DocumentUnit from "@/domain/documentUnit"
import DocumentUnitInfoPanel from "@/shared/components/DocumentUnitInfoPanel.vue"
import NavbarSide from "@/shared/components/NavbarSide.vue"
import SideToggle from "@/shared/components/SideToggle.vue"

const props = defineProps<{ documentUnit: DocumentUnit }>()
const route = useRoute()
const router = useRouter()
const documentNumber = computed(() => props.documentUnit.documentNumber)
const menuItems = useCaseLawMenuItems(documentNumber, route)
const goBackRoute = { name: "caselaw" }
const navigationIsOpen = useToggleStateInRouteQuery(
  "showNavBar",
  route,
  router.replace
)

const fileNumberInfo = computed(
  () => props.documentUnit.coreData.fileNumbers?.[0]
)

const decisionDateInfo = computed(() =>
  props.documentUnit.coreData.decisionDate
    ? dayjs(props.documentUnit.coreData.decisionDate).format("DD.MM.YYYY")
    : undefined
)

const documentationOffice = computed(
  () => props.documentUnit.coreData.documentationOffice?.label
)

const courtInfo = computed(() => props.documentUnit.coreData.court?.label)

const propertyInfos = computed(() => [
  { label: "Aktenzeichen", value: fileNumberInfo.value },
  { label: "Entscheidungsdatum", value: decisionDateInfo.value },
  { label: "Gericht", value: courtInfo.value },
  { label: "Dokumentationsstelle", value: documentationOffice.value },
])
</script>

<template>
  <div class="flex grow w-screen">
    <SideToggle
      v-model:is-expanded="navigationIsOpen"
      class="border-gray-400 border-r-1 border-solid"
      label="Navigation"
    >
      <NavbarSide
        go-back-label="Zurück"
        :go-back-route="goBackRoute"
        :menu-items="menuItems"
      />
    </SideToggle>

    <div class="bg-gray-100 flex flex-col w-full">
      <DocumentUnitInfoPanel
        :heading="documentUnit.documentNumber ?? ''"
        :property-infos="propertyInfos"
      />

      <div class="flex flex-col grow items-start">
        <slot :classes="['p-[2rem] w-full grow']" />
      </div>
    </div>
  </div>
</template>
