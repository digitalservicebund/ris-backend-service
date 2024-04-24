<script setup lang="ts">
import dayjs from "dayjs"
import { computed, Ref, ref, watchEffect } from "vue"
import { useRoute } from "vue-router"
import DocumentUnitInfoPanel from "@/components/DocumentUnitInfoPanel.vue"
import NavbarSide from "@/components/NavbarSide.vue"
import SideToggle from "@/components/SideToggle.vue"
import { useCaseLawMenuItems } from "@/composables/useCaseLawMenuItems"
import useQuery from "@/composables/useQueryFromRoute"
import { useStatusBadge } from "@/composables/useStatusBadge"
import DocumentUnit from "@/domain/documentUnit"
import { ServiceResponse } from "@/services/httpClient"

const props = defineProps<{
  documentUnit: DocumentUnit
  saveCallback?: () => Promise<ServiceResponse<void>>
  showNavigationPanel: boolean
}>()

const route = useRoute()

const showNavigationPanelRef: Ref<boolean> = ref(props.showNavigationPanel)

const { pushQueryToRoute } = useQuery<"showNavigationPanel">()

const menuItems = useCaseLawMenuItems(
  props.documentUnit.documentNumber,
  route.query,
)

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

const toggleNavigationPanel = (state?: boolean) => {
  showNavigationPanelRef.value = state || !showNavigationPanelRef.value
  pushQueryToRoute({
    showNavigationPanel: showNavigationPanelRef.value.toString(),
  })
}
</script>

<template>
  <div class="flex w-screen grow">
    <div
      class="sticky top-0 z-50 flex flex-col border-r-1 border-solid border-gray-400 bg-white"
    >
      <SideToggle
        class="sticky top-0 z-20"
        :is-expanded="showNavigationPanelRef"
        label="Navigation"
        size="small"
        @update:is-expanded="toggleNavigationPanel"
      >
        <NavbarSide :is-child="false" :menu-items="menuItems" :route="route" />
      </SideToggle>
    </div>
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
