<script lang="ts" setup>
import { storeToRefs } from "pinia"
import {
  isDocumentSection,
  isArticle,
  DocumentSection,
  Article,
} from "@/domain/norm"
import { useLoadedNormStore } from "@/stores/loadedNorm"

const store = useLoadedNormStore()
const { loadedNorm } = storeToRefs(store)
if (loadedNorm.value !== undefined) {
  const preamble: (Article | DocumentSection) | undefined =
    loadedNorm.value.documentation?.find(
      (section) => section.marker === "Eingangsformel",
    )

  const close: (Article | DocumentSection) | undefined =
    loadedNorm.value.documentation?.find((section) =>
      ["Schlussformel", "Schlußformel"].includes(section.marker ?? ""),
    )

  const articlesWithoutPreambleAndClose:
    | (Article | DocumentSection)[]
    | undefined = loadedNorm.value.documentation?.filter(
    (section) =>
      !["Eingangsformel", "Schlussformel", "Schlußformel"].includes(
        section.marker ?? "",
      ),
  )

  articlesWithoutPreambleAndClose?.sort((a, b) => {
    if (a.marker?.includes("Art")) {
      return Number(a.marker?.substring(4)) < Number(b.marker?.substring(4))
        ? -1
        : 1
    } else {
      return Number(a.marker?.substring(2)) < Number(b.marker?.substring(2))
        ? -1
        : 1
    }
  })

  const sortedArticleArray = []

  if (preamble) {
    sortedArticleArray.push(preamble)
  }
  articlesWithoutPreambleAndClose?.forEach((article) => {
    if (article) {
      sortedArticleArray.push(article)
    }
  })
  if (close) {
    sortedArticleArray.push(close)
  }

  loadedNorm.value.documentation = sortedArticleArray

  const articlesToSort = loadedNorm.value.documentation.filter((section) =>
    isArticle(section),
  )

  articlesToSort.forEach((article) => {
    const articleWithParagraphs = article as Article

    if (
      articleWithParagraphs.paragraphs.filter((f) => f.marker == null).length ==
      0
    ) {
      articleWithParagraphs.paragraphs.sort(
        (a, b) =>
          Number(a?.marker?.substring(1, a.marker?.length - 1) || 0) -
          Number(b?.marker?.substring(1, b.marker?.length - 1) || 0),
      )
    }
  })
}
</script>

<template>
  <div v-if="loadedNorm">
    <div class="max-w-screen-md">
      <h1 class="ds-heading-02-reg mb-44 text-center font-bold">
        {{
          loadedNorm.metadataSections?.NORM?.[0]?.OFFICIAL_LONG_TITLE?.[0] ?? ""
        }}
      </h1>
      <div v-for="doc in loadedNorm.documentation" :key="doc.guid">
        <h2
          class="mt-40 text-center"
          :class="[
            isDocumentSection(doc) &&
            doc.marker &&
            ['Eingangsformel', 'Schlussformel'].includes(doc.marker)
              ? 'ds-label-01-bold mb-16'
              : 'ds-label-01-reg',
          ]"
        >
          {{ doc.marker }}
        </h2>
        <h2 v-if="doc.heading" class="ds-label-01-bold mb-16 text-center">
          {{ doc.heading }}
        </h2>

        <template v-if="isArticle(doc)">
          <div :key="doc.guid" class="mb-24">
            <div
              v-for="paragraph in doc.paragraphs"
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
        </template>
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
