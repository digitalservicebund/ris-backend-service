<script lang="ts" setup>
import { ref } from "vue"
import TextButton from "@/components/TextButton.vue"
import TextInput from "@/components/TextInput.vue"
import TokenizeText from "@/components/TokenizeText.vue"
import { FieldOfLawNode, Page } from "@/domain/fieldOfLaw"
import FieldOfLawService from "@/services/fieldOfLawService"

const searchStr = ref("")
const results = ref<Page<FieldOfLawNode>>()
const currentPage = ref(0)
const RESULTS_PER_PAGE = 10

function submitSearch(resetPage = true) {
  if (resetPage) currentPage.value = 0
  FieldOfLawService.searchForFieldsOfLaw(
    searchStr.value,
    currentPage.value,
    RESULTS_PER_PAGE
  ).then((response) => {
    if (!response.data) return
    results.value = response.data
  })
}

function handlePagination(backwards: boolean) {
  if (backwards && results.value?.first) return
  if (!backwards && results.value?.last) return

  currentPage.value += backwards ? -1 : 1
  submitSearch(false)
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
      <div
        v-for="(node, idx) in results.content"
        :key="idx"
        class="flex flex-row"
      >
        <div class="identifier">
          {{ node.identifier }}
        </div>
        <div class="font-size-14px pl-6 pt-2 text-blue-800">
          <TokenizeText :keywords="node.linkedFields ?? []" :text="node.text" />
        </div>
      </div>
      <div
        v-if="results.numberOfElements < results.totalElements"
        class="flex flex-row justify-center"
      >
        <div
          class="link pr-6"
          :class="results.first ? 'disabled-link' : ''"
          @click="handlePagination(true)"
          @keyup.enter="handlePagination(true)"
        >
          zurück
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
}

.disabled-link {
  color: gray;
  cursor: default;
}

.identifier {
  font-size: 16px;
  white-space: nowrap;
}

.font-size-14px {
  font-size: 14px;
}
</style>
