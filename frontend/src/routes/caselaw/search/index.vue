<script lang="ts" setup>
import Button from "primevue/button"
import Tab from "primevue/tab"
import TabList from "primevue/tablist"
import TabPanel from "primevue/tabpanel"
import TabPanels from "primevue/tabpanels"
import Tabs from "primevue/tabs"
import { computed, onMounted, ref, watch } from "vue"
import { useRoute, useRouter } from "vue-router"
import DecisionSearch from "@/components/search/DecisionSearch.vue"
import PendingProceedingSearch from "@/components/search/PendingProceedingSearch.vue"
import { useInternalUser } from "@/composables/useInternalUser"
import useSessionStore from "@/stores/sessionStore"

const router = useRouter()
const route = useRoute()
const isInternalUser = useInternalUser()
const sessionStore = useSessionStore()
const documentationOfficeAbbreviation =
  sessionStore.user?.documentationOffice?.abbreviation

const tabParam = computed(() => (route.params.tab as string) ?? "decisions")

const value = ref()

const tabMap: Record<string, string> = {
  decisions: "0",
  "pending-proceedings": "1",
}

onMounted(() => {
  value.value = tabMap[tabParam.value] ?? "0"
})

watch(value, async (newVal) => {
  const pathKey =
    Object.entries(tabMap).find(([, v]) => v === newVal)?.[0] ?? "decisions"
  if (pathKey !== tabParam.value) {
    await router.replace({
      name: "caselaw-search-tab",
      params: { tab: pathKey },
    })
  }
})

// Function to push the correct route, depending on the active document kind tab
const handleNewDocumentationUnitClick = async () => {
  if (value.value === "0") {
    await router.push({ name: "caselaw-documentUnit-new" })
  } else if (value.value === "1") {
    await router.push({ name: "caselaw-pending-proceeding-new" })
  }
}
</script>

<template>
  <div class="m-24 flex flex-col">
    <div class="mb-16 flex w-full justify-end">
      <Button
        v-if="isInternalUser"
        label="Neue Dokumentationseinheit"
        @click="handleNewDocumentationUnitClick"
      ></Button>
    </div>
    <Tabs v-model:value="value" lazy>
      <!-- Todo: for development not yet hidden for BFH, hide the TabList
      v-if="documentationOfficeAbbreviation === 'BFH' -->
      <TabList v-if="documentationOfficeAbbreviation !== 'BFH'">
        <Tab data-testid="search-tab-caselaw" value="0">Rechtsprechung</Tab>
        <Tab data-testid="search-tab-pending-proceeding" value="1"
          >Anhängige Verfahren</Tab
        >
      </TabList>
      <TabPanels>
        <TabPanel value="0">
          <DecisionSearch />
        </TabPanel>
        <TabPanel value="1">
          <PendingProceedingSearch />
        </TabPanel>
      </TabPanels>
    </Tabs>
  </div>
</template>
