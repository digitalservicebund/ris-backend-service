<script lang="ts" setup>
import Tab from "primevue/tab"
import TabList from "primevue/tablist"
import TabPanel from "primevue/tabpanel"
import TabPanels from "primevue/tabpanels"
import Tabs from "primevue/tabs"
import EURLexSearch from "@/components/eurlex/EURLexSearch.vue"
import EUCaselaw from "@/components/inbox/EUCaselaw.vue"
import PendingHandover from "@/components/inbox/PendingHandover.vue"
</script>

<template>
  <Tabs class="m-32" lazy value="0">
    <TabList>
      <Tab data-testId="external-handover-tab" value="0">Fremdanlagen</Tab>
      <Tab data-testId="eu-tab" value="1">EU-Rechtsprechung</Tab>
      <Tab value="2">EUR-Lex</Tab>
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
