<script lang="ts" setup>
import Tab from "primevue/tab"
import TabList from "primevue/tablist"
import TabPanel from "primevue/tabpanel"
import TabPanels from "primevue/tabpanels"
import Tabs from "primevue/tabs"
import { computed, onMounted, ref, watch } from "vue"
import { useRoute, useRouter } from "vue-router"
import EURLexSearch from "@/components/eurlex/EURLexSearch.vue"
import EUCaselaw from "@/components/inbox/EUCaselaw.vue"
import PendingHandover from "@/components/inbox/PendingHandover.vue"
import useSessionStore from "@/stores/sessionStore"

const route = useRoute()
const router = useRouter()
const sessionStore = useSessionStore()
const documentationOfficeAbbreviation =
  sessionStore.user?.documentationOffice?.abbreviation

const tabParam = computed(() => (route.params.tab as string) ?? "fremdanlagen")

const value = ref()

const tabMap: Record<string, string> = {
  fremdanlagen: "0",
  "eu-rechtsprechung": "1",
  "eur-lex": "2",
}

onMounted(() => {
  value.value = tabMap[tabParam.value] ?? "0"
})

watch(value, async (newVal) => {
  const pathKey =
    Object.entries(tabMap).find(([, v]) => v === newVal)?.[0] ?? "fremdanlagen"
  if (pathKey !== tabParam.value) {
    await router.replace({
      name: "caselaw-inbox-tab",
      params: { tab: pathKey },
    })
  }
})
</script>

<template>
  <Tabs v-model:value="value" class="m-24" lazy>
    <TabList>
      <Tab data-testid="external-handover-tab" value="0">Fremdanlagen</Tab>
      <Tab data-testid="eu-tab" value="1">EU-Rechtsprechung</Tab>
      <Tab
        v-if="
          documentationOfficeAbbreviation === 'BFH' ||
          documentationOfficeAbbreviation === 'BGH' ||
          documentationOfficeAbbreviation === 'DS'
        "
        data-testid="eurlex-tab"
        value="2"
        >EUR-Lex</Tab
      >
    </TabList>
    <TabPanels>
      <TabPanel value="0">
        <PendingHandover />
      </TabPanel>
      <TabPanel value="1">
        <EUCaselaw />
      </TabPanel>
      <TabPanel value="2">
        <EURLexSearch />
      </TabPanel>
    </TabPanels>
  </Tabs>
</template>
