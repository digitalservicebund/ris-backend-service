<script setup lang="ts">
import { computed } from "vue"
import { useRoute, useRouter } from "vue-router"
import DocumentUnitInfoPanel from "@/components/DocumentUnitInfoPanel.vue"
import NavbarSide from "@/components/NavbarSide.vue"
import SideToggle from "@/components/SideToggle.vue"
import { useCaseLawMenuItems } from "@/composables/useCaseLawMenuItems"
import { useToggleStateInRouteQuery } from "@/composables/useToggleStateInRouteQuery"
import DocumentUnit from "@/domain/documentUnit"

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
</script>

<template>
  <div class="flex grow w-screen">
    <SideToggle v-model:is-expanded="navigationIsOpen" label="Navigation">
      <NavbarSide
        class="border-gray-400 border-r-1 border-solid"
        go-back-label="ZURÜCK"
        :go-back-route="goBackRoute"
        :menu-items="menuItems"
      />
    </SideToggle>

    <div class="bg-gray-100 flex flex-col w-full">
      <DocumentUnitInfoPanel :document-unit="documentUnit" />

      <div class="flex flex-col grow items-start">
        <slot :classes="['p-[2rem] w-full grow']" />
      </div>
    </div>
  </div>
</template>
