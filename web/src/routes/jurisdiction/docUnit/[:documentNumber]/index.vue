<script setup lang="ts">
import { ref } from "vue"
import { useRoute, useRouter } from "vue-router"
import DocUnitInfoPanel from "@/components/DocUnitInfoPanel.vue"
import NavbarSide from "@/components/NavbarSide.vue"
import DocUnit from "@/domain/docUnit"

defineProps<{ docUnit: DocUnit }>()
const router = useRouter()
const route = useRoute()

const showNavBar = ref(useRoute().query.showNavBar === "true")
const handleToggleNavBar = async () => {
  console.log("hello")
  showNavBar.value = !showNavBar.value
  await router.push({
    ...route,
    query: { ...route.query, showNavBar: String(showNavBar.value) },
  })
}
</script>

<template>
  <v-main>
    <v-container fluid>
      <v-row>
        <NavbarSide
          :document-number="String(docUnit.documentnumber)"
          :visible="showNavBar"
          @toggle-navbar="handleToggleNavBar"
        />
        <v-col :cols="showNavBar ? 10 : 11" class="panel_and_main_area">
          <DocUnitInfoPanel :doc-unit="docUnit" />
          <slot></slot>
        </v-col>
      </v-row>
    </v-container>
  </v-main>
</template>
