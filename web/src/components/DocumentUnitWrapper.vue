<script setup lang="ts">
import { ref, watch } from "vue"
import { useRoute, useRouter } from "vue-router"
import DocumentUnitInfoPanel from "@/components/DocumentUnitInfoPanel.vue"
import NavbarSide from "@/components/NavbarSide.vue"
import SideToggle from "@/components/SideToggle.vue"
import DocumentUnit from "@/domain/documentUnit"

defineProps<{ documentUnit: DocumentUnit }>()

const navigationIsOpen = ref(true)
const route = useRoute()

watch(
  () => route.query.showNavBar,
  (showNavBar) => {
    if (showNavBar) {
      navigationIsOpen.value = route.query.showNavBar === "true"
    }
  },
  { immediate: true }
)

const router = useRouter()

watch(navigationIsOpen, () =>
  router.replace({
    ...route,
    query: { ...route.query, showNavBar: navigationIsOpen.value.toString() },
  })
)
</script>

<template>
  <div class="flex grow w-screen">
    <SideToggle v-model:is-expanded="navigationIsOpen" aria-label="Navigation">
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
