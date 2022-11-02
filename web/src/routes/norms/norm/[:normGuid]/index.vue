<script lang="ts" setup>
import { ref, watch } from "vue"
import Norm from "@/domain/Norm"
import { getNormByGuid } from "@/services/normsService"

const props = defineProps<{ normGuid: string }>()
const norm = ref<Norm | undefined>()
async function loadNormByGuid() {
  norm.value = (await getNormByGuid(props.normGuid)).data
}
watch(() => props.normGuid, loadNormByGuid, { immediate: true })
</script>
<template>
  <div class="flex flex-col gap-16 p-16">
    <div v-if="norm" class="p-40 w-[50rem]">
      <h1 class="heading-02-regular mb-[2.75rem]">
        {{ norm.longTitle }}
      </h1>
      <div v-for="article in norm.articles" :key="article.guid">
        <h2 class="heading-04-regular mb-[1rem]">
          {{ article.marker }}{{ article.title }}
        </h2>
        <div v-for="paragraph in article.paragraphs" :key="paragraph.guid">
          <p>{{ paragraph.marker }} {{ paragraph.text }}</p>
        </div>
      </div>
    </div>
  </div>
</template>
