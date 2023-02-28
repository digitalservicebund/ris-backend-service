<script lang="ts" setup>
import { ref } from "vue"
import TextButton from "@/components/TextButton.vue"
import TextInput from "@/components/TextInput.vue"

const searchStr = ref("")

function submitSearch() {
  console.log(searchStr.value)
}
</script>

<template>
  <h1 class="heading-03-regular pb-8">Suche</h1>
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
</template>
