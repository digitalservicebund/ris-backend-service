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
  <v-main role="main">
    <v-container fluid>
      <v-row>
        <NavbarSide
          :document-number="String(documentUnit.documentnumber)"
          :visible="showNavBar"
          @toggle-navbar="handleToggleNavBar"
        />
        <v-col class="panel-and-main-area" :cols="showNavBar ? 10 : 11">
          <DocumentUnitInfoPanel :document-unit="documentUnit" />
          <slot></slot>
        </v-col>
      </v-row>
    </v-container>
  </v-main>
</template>

<style lang="scss" scoped>
@import "@/styles/variables";

.panel-and-main-area {
  background-color: $white;
}
</style>
