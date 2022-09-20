<script setup lang="ts">
import { ref } from "vue"
import { useRoute, useRouter } from "vue-router"
import DocumentUnitInfoPanel from "@/components/DocumentUnitInfoPanel.vue"
import NavbarSide from "@/components/NavbarSide.vue"
import DocumentUnit from "@/domain/documentUnit"

defineProps<{ documentUnit: DocumentUnit }>()
const router = useRouter()
const route = useRoute()

const showNavBar = ref(route.query.showNavBar !== "false")
const handleToggleNavBar = async () => {
  showNavBar.value = !showNavBar.value
  await router.push({
    ...route,
    query: { ...route.query, showNavBar: String(showNavBar.value) },
  })
}
</script>

<template>
  <div class="flex grow w-screen" role="main">
    <NavbarSide
      :document-number="String(documentUnit.documentNumber)"
      :visible="showNavBar"
      @toggle-navbar="handleToggleNavBar"
    />

    <div class="bg-gray-100 flex flex-col w-full">
      <DocumentUnitInfoPanel :document-unit="documentUnit" />

      <div class="flex flex-col grow items-start">
        <slot :classes="['p-32 w-full grow']" />
      </div>
    </div>
  </div>
</template>
