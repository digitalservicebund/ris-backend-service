<script lang="ts" setup>
import Button from "primevue/button"
import InputText from "primevue/inputtext"
import Tab from "primevue/tab"
import TabList from "primevue/tablist"
import TabPanel from "primevue/tabpanel"
import TabPanels from "primevue/tabpanels"
import Tabs from "primevue/tabs"
import { ref } from "vue"
import EURLexList from "@/components/eurlex/EURLexList.vue"
import DateInput from "@/components/input/DateInput.vue"
import InputField from "@/components/input/InputField.vue"
import { Page } from "@/components/Pagination.vue"
import EURLexResult from "@/domain/eurlex"
import service from "@/services/eurlexService"

const searchResults = ref<Page<EURLexResult>>()
const page = ref(0)

async function handleSearchButtonClicked() {
  searchResults.value = (await service.get(page.value)).data
}

function resetSearch() {}

const isEmptySearch = true
</script>

<template>
  <Tabs class="m-32" value="2">
    <TabList>
      <Tab value="0">Fremdanlagen</Tab>
      <Tab value="1">EU-Rechtsprechung</Tab>
      <Tab value="2">EUR-Lex</Tab>
    </TabList>
    <TabPanels>
      <TabPanel value="0"></TabPanel>
      <TabPanel value="1"></TabPanel>
      <TabPanel value="2">
        <div class="pyb-24 mb-32 flex flex-col bg-blue-200">
          <div
            class="m-40 grid grid-flow-col grid-cols-[auto_1fr_auto_1fr] grid-rows-[auto_auto_auto] gap-x-12 gap-y-20 lg:gap-x-32"
          >
            <!-- Column 1 -->
            <div class="ris-body1-regular ml-3 flex flex-row items-center">
              Aktenzeichen
            </div>
            <div class="ris-body1-regular flex flex-row items-center">
              Gericht
            </div>
            <div></div>
            <!-- Column 2 -->
            <div>
              <InputField
                id="fileNumber"
                label="Aktenzeichen"
                visually-hide-label
              >
                <InputText
                  aria-label="Aktenzeichen Suche"
                  fluid
                  size="small"
                ></InputText>
              </InputField>
            </div>
            <div class="flex flex-row gap-10">
              <InputField
                id="courtType"
                label="Gerichtstyp"
                visually-hide-label
              >
                <InputText
                  aria-label="Gerichtstyp Suche"
                  fluid
                  placeholder="Gerichtstyp"
                  size="small"
                ></InputText>
              </InputField>
              <span class="pt-6">-</span>
              <InputField
                id="courtLocation"
                label="Gerichtsort"
                visually-hide-label
              >
                <InputText
                  aria-label="Gerichtsort Suche"
                  fluid
                  placeholder="Ort"
                  size="small"
                ></InputText>
              </InputField>
            </div>
            <div></div>
            <!-- Column 3 -->
            <div
              class="ris-body1-regular flex flex-row items-center pl-24 lg:pl-48"
            >
              Dokumentnummer
            </div>
            <div
              class="ris-body1-regular flex flex-row items-center pl-24 lg:pl-48"
            >
              Datum
            </div>
            <div></div>
            <!-- Column 4 -->
            <div class="">
              <InputField id="celex" label="Celex" visually-hide-label>
                <InputText
                  aria-label="Celex-Nummer Suche"
                  fluid
                  size="small"
                ></InputText>
              </InputField>
            </div>
            <div class="flex flex-row gap-10">
              <InputField
                id="decisionDateStartField"
                data-testid="decision-date-input"
                label="Entscheidungsdatum"
                visually-hide-label
              >
                <DateInput
                  id="decisionDateStartInput"
                  aria-label="Entscheidungsdatum Suche Start"
                ></DateInput>
              </InputField>
              <span class="pt-6">-</span>
              <InputField
                id="decisionDateEndField"
                data-testid="decision-date-end-input"
                label="Entscheidungsdatum Ende"
                visually-hide-label
              >
                <DateInput
                  id="decisionDateEndInput"
                  aria-label="Entscheidungsdatum Suche Ende"
                  placeholder="TT.MM.JJJJ (optional)"
                ></DateInput>
              </InputField>
            </div>
            <div class="flex flex-row">
              <Button
                aria-label="Nach Dokumentationseinheiten suchen"
                class="self-start"
                label="Ergebnisse anzeigen"
                size="small"
                @click="handleSearchButtonClicked"
              ></Button>

              <Button
                v-if="!isEmptySearch"
                aria-label="Suche zurücksetzen"
                class="ml-8 self-start"
                label="Suche zurücksetzen"
                size="small"
                text
                @click="resetSearch"
              ></Button>
            </div>
          </div>
        </div>
        <EURLexList :page-entries="searchResults" />
      </TabPanel>
    </TabPanels>
  </Tabs>
</template>
