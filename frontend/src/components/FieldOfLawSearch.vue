<script lang="ts" setup>
import { ref } from "vue"
import TextButton from "@/components/TextButton.vue"
import TextInput from "@/components/TextInput.vue"
import TokenizeText from "@/components/TokenizeText.vue"
import { FieldOfLawNode } from "@/domain/fieldOfLaw"
import FieldOfLawService from "@/services/fieldOfLawService"

const searchStr = ref("")
const results = ref<FieldOfLawNode[]>([])

function submitSearch() {
  FieldOfLawService.searchForFieldsOfLaw(searchStr.value).then((response) => {
    if (!response.data) return
    results.value = response.data
  })
}

function handlePagination(backwards: boolean) {
  console.log(backwards)
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
    <div v-for="(node, idx) in results" :key="idx" class="flex flex-row">
      <div class="identifier">
        {{ node.identifier }}
      </div>
      <div class="font-size-14px pl-6 pt-2 text-blue-800">
        <TokenizeText :keywords="node.linkedFields ?? []" :text="node.text" />
      </div>
    </div>
    <div class="flex flex-row justify-center">
      <div
        class="link pr-6"
        @click="handlePagination(true)"
        @keyup.enter="handlePagination(true)"
      >
        zurück
      </div>
      <div
        class="link pl-6"
        @click="handlePagination(false)"
        @keyup.enter="handlePagination(false)"
      >
        vor
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.link {
  cursor: pointer;
  text-decoration: underline;
}

.identifier {
  font-size: 16px;
  white-space: nowrap;
}

.font-size-14px {
  font-size: 14px;
}
</style>
