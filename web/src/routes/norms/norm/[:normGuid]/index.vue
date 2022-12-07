<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { useLoadedNormStore } from "@/stores/loadedNorm"

const store = useLoadedNormStore()
const { loadedNorm } = storeToRefs(store)
</script>

<template>
  <div v-if="loadedNorm">
    <div class="max-w-screen-md">
      <h1 class="heading-02-regular mb-44">
        {{ loadedNorm.officialLongTitle }}
      </h1>

      <div v-for="article in loadedNorm.articles" :key="article.guid">
        <h2 class="heading-04-regular mb-24">
          {{ article.marker }} {{ article.title }}
        </h2>

        <div
          v-for="paragraph in article.paragraphs"
          :key="paragraph.guid"
          class="mb-24"
        >
          <p>{{ paragraph.marker }} {{ paragraph.text }}</p>
        </div>
      </div>
    </div>
  </div>
</template>
