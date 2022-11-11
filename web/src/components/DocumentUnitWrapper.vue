<script setup lang="ts">
import { useRoute, useRouter } from "vue-router"
import DocumentUnitInfoPanel from "@/components/DocumentUnitInfoPanel.vue"
import NavbarSide from "@/components/NavbarSide.vue"
import SideToggle from "@/components/SideToggle.vue"
import { useToggleStateInRouteQuery } from "@/composables/useToggleStateInRouteQuery"
import DocumentUnit from "@/domain/documentUnit"

defineProps<{ documentUnit: DocumentUnit }>()

const route = useRoute()
const router = useRouter()
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
        :document-number="documentUnit.documentNumber"
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
