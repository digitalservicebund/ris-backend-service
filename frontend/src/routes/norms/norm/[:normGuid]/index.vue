<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { Article } from "@/domain/Norm"
import { useLoadedNormStore } from "@/stores/loadedNorm"

const store = useLoadedNormStore()
const { loadedNorm } = storeToRefs(store)

if (loadedNorm.value !== undefined) {
  const preamble: Article[] = loadedNorm.value.articles.filter(
    (article) => article.marker === "Eingangsformel"
  )

  const close: Article[] = loadedNorm.value.articles.filter(
    (article) => article.marker === "Schlussformel"
  )

  const articlesWithoutPreambleAndClose: Article[] =
    loadedNorm.value.articles.filter(
      (article) => !["Eingangsformel", "Schlussformel"].includes(article.marker)
    )

  articlesWithoutPreambleAndClose.sort((a, b) => {
    if (a.marker.includes("Art")) {
      return Number(a.marker.substring(4)) < Number(b.marker.substring(4))
        ? -1
        : 1
    } else {
      return Number(a.marker.substring(2)) < Number(b.marker.substring(2))
        ? -1
        : 1
    }
  })

  loadedNorm.value.articles = preamble
    .concat(articlesWithoutPreambleAndClose)
    .concat(close)

  loadedNorm.value.articles.forEach((article) => {
    if (article.paragraphs.filter((f) => f.marker == null).length == 0) {
      article.paragraphs.sort((a, b) =>
        Number(a.marker.substring(1, a.marker.length - 1)) <
        Number(b.marker.substring(1, b.marker.length - 1))
          ? -1
          : 1
      )
    }
  })
}
</script>

<template>
  <div v-if="loadedNorm">
    <div class="max-w-screen-md">
      <h1 class="heading-02-bold mb-44 text-center">
        {{ loadedNorm.officialLongTitle }}
      </h1>
      <div v-for="article in loadedNorm.articles" :key="article.guid">
        <h2
          class="mt-40 text-center"
          :class="[
            ['Eingangsformel', 'Schlussformel'].includes(article.marker)
              ? 'label-01-bold mb-16'
              : 'label-01-regular',
          ]"
        >
          {{ article.marker }}
        </h2>
        <h2
          v-if="article.title != null"
          class="label-01-bold mb-16 text-center"
        >
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
