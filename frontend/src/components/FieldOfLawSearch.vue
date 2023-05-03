<script lang="ts" setup>
import { ref } from "vue"
import FieldOfLawListEntry from "@/components/FieldOfLawListEntry.vue"
import { FieldOfLawNode, Page } from "@/domain/fieldOfLaw"
import FieldOfLawService from "@/services/fieldOfLawService"
import TextButton from "@/shared/components/input/TextButton.vue"
import TextInput from "@/shared/components/input/TextInput.vue"

const emit = defineEmits<{
  (event: "linkedField:clicked", identifier: string): void
  (event: "node-clicked", identifier: string): void
  (event: "do-show-norms"): void
}>()

const searchStr = ref("")
const results = ref<Page<FieldOfLawNode>>()
const currentPage = ref(0)
const RESULTS_PER_PAGE = 10

async function submitSearch(isNewSearch = true) {
  if (isNewSearch) currentPage.value = 0
  await FieldOfLawService.searchForFieldsOfLaw(
    searchStr.value,
    currentPage.value,
    RESULTS_PER_PAGE
  ).then((response) => {
    if (!response.data) return
    results.value = response.data
    if (results.value.content.length > 0 && isNewSearch) {
      emit("node-clicked", results.value.content[0].identifier)
      if (searchStr.value.includes("norm:")) {
        emit("do-show-norms")
      }
    }
  })
}

async function handlePagination(backwards: boolean) {
  if (backwards && results.value?.first) return
  if (!backwards && results.value?.last) return

  currentPage.value += backwards ? -1 : 1
  await submitSearch(false)
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
    <div v-if="results">
      <FieldOfLawListEntry
        v-for="(fieldOfLawNode, idx) in results.content"
        :key="idx"
        :field-of-law="fieldOfLawNode"
        @linked-field:clicked="(identifier) => emit('node-clicked', identifier)"
        @node-clicked="emit('node-clicked', fieldOfLawNode.identifier)"
      />
      <div
        v-if="results.numberOfElements < results.totalElements"
        class="flex flex-row justify-center pt-16"
      >
        <div
          class="link pr-6"
          :class="results.first ? 'disabled-link' : ''"
          @click="handlePagination(true)"
          @keyup.enter="handlePagination(true)"
        >
          zurück
        </div>
        <div class="page-count">
          {{ currentPage + 1 }} von {{ results.totalPages }}
        </div>
        <div
          class="link pl-6"
          :class="results.last ? 'disabled-link' : ''"
          @click="handlePagination(false)"
          @keyup.enter="handlePagination(false)"
        >
          vor
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.link {
  cursor: pointer;
  text-decoration: underline;

  &:active {
    text-decoration-thickness: 4px;
  }

  &:focus {
    border: 4px solid #004b76;
  }
}

.disabled-link {
  color: gray;
  cursor: default;
}

.page-count {
  color: gray;
}
</style>
