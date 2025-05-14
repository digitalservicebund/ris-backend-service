<script lang="ts" setup>
import Tab from "primevue/tab"
import TabList from "primevue/tablist"
import TabPanel from "primevue/tabpanel"
import TabPanels from "primevue/tabpanels"
import Tabs from "primevue/tabs"
import { onMounted, ref, watch } from "vue"
import { useRoute } from "vue-router"
import EURLexSearch from "@/components/eurlex/EURLexSearch.vue"
import EUCaselaw from "@/components/inbox/EUCaselaw.vue"
import PendingHandover from "@/components/inbox/PendingHandover.vue"
import useQuery from "@/composables/useQueryFromRoute"

const { pushQueryToRoute } = useQuery()
const route = useRoute()

const value = ref()

const tabMap: Record<string, string> = {
  fremdanlagen: "0",
  "eu-rechtsprechung": "1",
  "eur-lex": "2",
}

onMounted(() => {
  const tabParam = route.query.tab as string
  value.value = tabMap[tabParam] ?? "0"
})

watch(value, (newVal) => {
  const newTab = Object.keys(tabMap).find((key) => tabMap[key] === newVal)
  if (newTab) {
    pushQueryToRoute({
      tab: newTab,
    })
  }
})
</script>

<template>
  <Tabs v-model:value="value" class="m-32" lazy>
    <TabList>
      <Tab data-testid="external-handover-tab" value="0">Fremdanlagen</Tab>
      <Tab data-testid="eu-tab" value="1">EU-Rechtsprechung</Tab>
      <Tab data-testid="eur-lex" value="2">EUR-Lex</Tab>
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
