<script lang="ts" setup>
import { ref } from "vue"
import FieldOfLawListEntry from "@/components/FieldOfLawListEntry.vue"
import { FieldOfLawNode } from "@/domain/fieldOfLaw"
import service from "@/services/fieldOfLawService"
import TextButton from "@/shared/components/input/TextButton.vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import Pagination from "@/shared/components/Pagination.vue"

const emit = defineEmits<{
  (event: "linkedField:clicked", identifier: string): void
  (event: "node-clicked", identifier: string): void
  (event: "do-show-norms"): void
}>()

const searchStr = ref("")
const results = ref<FieldOfLawNode[]>()
const itemsPerPage = 10

const paginationComponentRef = ref<InstanceType<typeof Pagination>>()

async function submitSearch() {
  await paginationComponentRef.value?.updateItems(0, searchStr.value)
  results.value?.[0] && emit("node-clicked", results.value[0].identifier)
  searchStr.value.includes("norm:") && emit("do-show-norms")
}

async function handleUpdateItems(newItems: FieldOfLawNode[]) {
  results.value = newItems
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
            @enter-released="submitSearch"
          />
        </div>
        <div class="pl-8">
          <TextButton
            aria-label="Sachgebietssuche ausführen"
            button-type="secondary"
            class="w-fit"
            label="Suchen"
            @click="submitSearch"
          />
        </div>
      </div>
    </div>
    <Pagination
      ref="paginationComponentRef"
      :item-service="service.searchForFieldsOfLaw"
      :items-per-page="itemsPerPage"
      @update-items="handleUpdateItems"
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
