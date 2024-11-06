<script lang="ts" setup>
import { ref, watch } from "vue"
import FieldOfLawListEntry from "@/components/FieldOfLawListEntry.vue"
import FieldOfLawTree from "@/components/FieldOfLawTree.vue"
import InputField from "@/components/input/InputField.vue"
import TextInput from "@/components/input/TextInput.vue"
import LoadingSpinner from "@/components/LoadingSpinner.vue"
import Pagination, { Page } from "@/components/Pagination.vue"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import errorMessages from "@/i18n/errors.json"
import service from "@/services/fieldOfLawService"
import StringsUtil from "@/utils/stringsUtil"

const emit = defineEmits<{
  "node:select": [node: FieldOfLaw]
  "search-results": [searchResults: FieldOfLaw[]]
  "linked-field:select": [node: FieldOfLaw]
  "node:unselect": []
  "do-show-norms": []
}>()

const searchStr = ref("")
const fieldOfLawString = ref("")
const normString = ref("")
const results = ref<FieldOfLaw[]>()
const currentPage = ref<Page<FieldOfLaw>>()
const itemsPerPage = 10
const isLoading = ref(false)

async function submitSearch(page: number) {
  // if (StringsUtil.isEmpty(searchStr.value)) {
  //   return removeSelectedNode()
  // }
  isLoading.value = true

  const response = await service.searchForFieldsOfLaw(
    page,
    itemsPerPage,
    searchStr.value,
    fieldOfLawString.value,
    normString.value,
  )
  if (response.data) {
    currentPage.value = response.data
    results.value = response.data.content

    // only highlight results in tree if no paginated results
    if (currentPage.value.first && currentPage.value.last) {
      emit("search-results", results.value)
    }

    if (results.value?.[0]) {
      emit("node:select", results.value[0] as FieldOfLaw)
    }
    if (searchStr.value.includes("norm:")) {
      emit("do-show-norms")
    }
  } else {
    currentPage.value = undefined
    results.value = undefined
    console.error("Error searching for Nodes")
  }
  isLoading.value = false
}

function removeSelectedNode() {
  emit("node:unselect")
}

watch(
  searchStr,
  async () => {
    if (StringsUtil.isEmpty(searchStr.value)) {
      removeSelectedNode()
    }
  },
  { immediate: true },
)
</script>

<template>
  <div class="flex flex-col gap-16">
    <div class="flex flex-row gap-8">
      <InputField id="fieldOfLawDirectInput" label="Sachgebiet">
        <TextInput
          id="fieldOfLawDirectInput"
          v-model="fieldOfLawString"
          aria-label="Sachgebiet Direkteingabe"
          size="medium"
          @enter-released="submitSearch(0)"
        />
      </InputField>
      <InputField id="fieldOfLawDirectInput" label="Bezeichnung">
        <TextInput
          id="fieldOfLawSearch"
          v-model="searchStr"
          aria-label="Sachgebiete Suche"
          size="medium"
          @enter-released="submitSearch(0)"
        />
      </InputField>
      <InputField id="fieldOfLawNormInput" label="Norm">
        <TextInput
          id="fieldOfLawNorm"
          v-model="normString"
          aria-label="Sachgebiete Norm"
          size="medium"
          @enter-released="submitSearch(0)"
        />
      </InputField>
    </div>

    <div v-if="isLoading" class="m-24 flex items-center justify-center">
      <LoadingSpinner />
    </div>
    <div v-if="currentPage">
      <Pagination
        navigation-position="bottom"
        :page="currentPage"
        @update-page="submitSearch"
      >
        <FieldOfLawListEntry
          v-for="(fieldOfLawNode, idx) in results"
          :key="idx"
          :field-of-law="fieldOfLawNode"
          @linked-field:select="emit('linked-field:select', $event)"
          @node:select="emit('node:select', fieldOfLawNode)"
        />
      </Pagination>
      <div v-if="!currentPage?.content || currentPage?.content?.length == 0">
        {{ errorMessages.SEARCH_RESULTS_NOT_FOUND.title }}
      </div>
    </div>
  </div>
</template>
