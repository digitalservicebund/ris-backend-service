<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { useLoadedNormStore } from "@/stores/loadedNorm"

const store = useLoadedNormStore()
const { loadedNorm } = storeToRefs(store)

if (loadedNorm.value !== undefined) {
  loadedNorm.value.articles.sort((a, b) =>
    Number(a.marker.substring(2)) < Number(b.marker.substring(2)) ? -1 : 1
  )
}
</script>

<template>
  <div v-if="loadedNorm">
    <div class="max-w-screen-md">
      <h1 class="heading-02-bold mb-44 text-center">
        {{ loadedNorm.officialLongTitle }}
      </h1>
      <div v-for="article in loadedNorm.articles" :key="article.guid">
        <h2 class="label-01-regular mt-40 text-center">
          {{ article.marker }}
        </h2>
        <h2 class="label-01-bold mb-16 text-center">
          {{ article.title }}
        </h2>

        <div
          v-for="paragraph in article.paragraphs"
          :key="paragraph.guid"
          class="mb-24"
        >
          <!-- eslint-disable vue/no-v-html -->
          <p
            v-html="
              paragraph.marker === null
                ? paragraph.text
                : paragraph.marker + ' ' + paragraph.text
            "
          />
          <!-- eslint-enable vue/no-v-html -->
        </div>
      </div>
    </div>
  </div>
</template>

<!-- eslint-disable-next-line vue-scoped-css/enforce-style-type-->
<style>
dl {
  padding-left: 2rem;
}

p > dl {
  padding-top: 1rem;
}

dt {
  float: left;
}

dd {
  padding-left: 2rem;
}
</style>
