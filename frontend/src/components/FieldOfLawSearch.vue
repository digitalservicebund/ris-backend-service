<script lang="ts" setup>
import { ref } from "vue"
import FieldOfLawListEntry from "@/components/FieldOfLawListEntry.vue"
import { FieldOfLawNode } from "@/domain/fieldOfLaw"
import service from "@/services/fieldOfLawService"
import TextButton from "@/shared/components/input/TextButton.vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import Pagination, { Page } from "@/shared/components/Pagination.vue"

const emit = defineEmits<{
  (event: "linkedField:clicked", identifier: string): void
  (event: "node-clicked", identifier: string): void
  (event: "do-show-norms"): void
}>()

const searchStr = ref("")
const results = ref<FieldOfLawNode[]>()
const currentPage = ref<Page<FieldOfLawNode>>()
const itemsPerPage = 10

async function submitSearch(page: number) {
  const response = await service.searchForFieldsOfLaw(
    page,
    itemsPerPage,
    searchStr.value
  )
  if (response.data) {
    currentPage.value = response.data
    results.value = response.data.content
    results.value?.[0] && emit("node-clicked", results.value[0].identifier)
    searchStr.value.includes("norm:") && emit("do-show-norms")
  } else {
    currentPage.value = undefined
    results.value = undefined
    console.error("Error searching for Nodes")
  }
}
</script>

<template>
  <h1 class="heading-03-regular pb-8">Suche</h1>
  <div class="flex flex-col">
    <div class="pb-28">
      <div class="flex flex-row items-stretch">
        <div class="grow">
          <TextInput
            id="FieldOfLawSearchTextInput"
            v-model="searchStr"
            aria-label="Sachgebiete Suche"
            full-height
            @enter-released="submitSearch(0)"
          />
        </div>
        <div class="pl-8">
          <TextButton
            aria-label="Sachgebietssuche ausführen"
            button-type="secondary"
            class="w-fit"
            label="Suchen"
            @click="submitSearch(0)"
          />
        </div>
      </div>
    </div>
    <Pagination
      v-if="currentPage"
      navigation-position="bottom"
      :page="currentPage"
      @update-page="submitSearch"
    >
      <FieldOfLawListEntry
        v-for="(fieldOfLawNode, idx) in results"
        :key="idx"
        :field-of-law="fieldOfLawNode"
        @linked-field:clicked="(identifier) => emit('node-clicked', identifier)"
        @node-clicked="emit('node-clicked', fieldOfLawNode.identifier)"
      />
    </Pagination>
  </div>
</template>
