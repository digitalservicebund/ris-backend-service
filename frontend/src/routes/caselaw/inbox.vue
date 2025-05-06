<script lang="ts" setup>
import Tab from "primevue/tab"
import TabList from "primevue/tablist"
import TabPanel from "primevue/tabpanel"
import TabPanels from "primevue/tabpanels"
import Tabs from "primevue/tabs"
import EURLexSearch from "@/components/eurlex/EURLexSearch.vue"
import PendingHandover from "@/components/pending-handover/PendingHandover.vue"
</script>

<template>
  <Tabs class="m-32" value="2">
    <TabList>
      <Tab value="0">Fremdanlagen</Tab>
      <Tab value="1">EU-Rechtsprechung</Tab>
      <Tab value="2">EUR-Lex</Tab>
    </TabList>
    <TabPanels>
      <TabPanel value="0">
        <PendingHandover />
      </TabPanel>
      <TabPanel value="1"></TabPanel>
      <TabPanel value="2">
        <EURLexSearch />
      </TabPanel>
    </TabPanels>
  </Tabs>
</template>
