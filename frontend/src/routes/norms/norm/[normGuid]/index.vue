<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { useLoadedNormStore } from "@/stores/loadedNorm"

const store = useLoadedNormStore()
const { loadedNorm } = storeToRefs(store)
</script>

<template>
  <div v-if="loadedNorm">
    <div class="max-w-screen-md">
      <h1 class="ds-heading-02-reg mb-44 text-center font-bold">
        {{
          loadedNorm.metadataSections?.NORM?.[0]?.OFFICIAL_LONG_TITLE?.[0] ?? ""
        }}
      </h1>
      <div v-for="section in loadedNorm.sections" :key="section.guid">
        <h2
          class="mt-40 text-center"
          :class="[
            ['Eingangsformel', 'Schlussformel'].includes(section.designation)
              ? 'ds-label-01-bold mb-16'
              : 'ds-label-01-reg',
          ]"
        >
          {{ section.designation }}
        </h2>
        <h2
          v-if="section.header != null"
          class="ds-label-01-bold mb-16 text-center"
        >
          {{ section.header }}
        </h2>

        <div
          v-for="paragraph in section.paragraphs"
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
