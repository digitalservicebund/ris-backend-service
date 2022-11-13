<script lang="ts" setup>
import { ref, watch } from "vue"
import { Norm } from "@/domain/Norm"
import { getNormByGuid } from "@/services/normsService"

const props = defineProps<{ normGuid: string }>()
const norm = ref<Norm | undefined>()

async function loadNormByGuid() {
  norm.value = (await getNormByGuid(props.normGuid)).data
}

watch(() => props.normGuid, loadNormByGuid, { immediate: true })
</script>

<template>
  <div v-if="norm">
    <div class="max-w-screen-md">
      <h1 class="heading-02-regular mb-44">
        {{ norm.longTitle }}
      </h1>

      <div v-for="article in norm.articles" :key="article.guid">
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
