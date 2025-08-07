<script lang="ts" setup>
import ForeignLanguageVersion from "@/domain/foreignLanguageVersion"
import IcBaselineTranslate from "~icons/ic/baseline-translate"

const props = defineProps<{
  data: ForeignLanguageVersion
}>()
</script>

<template>
  <div class="flex w-full justify-between">
    <div class="flex flex-row items-center">
      <IcBaselineTranslate class="mr-8" />
      <div class="ris-label1-regular mr-8 max-w-[calc(30vw)] truncate">
        {{ props.data.languageCode?.label }}:
        <a
          v-if="data.link"
          class="ris-link1-bold whitespace-nowrap no-underline focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
          :href="
            /^https?:\/\//i.test(data.link) ? data.link : `https://${data.link}`
          "
          rel="noopener noreferrer"
          target="_blank"
        >
          {{ data.link }}
        </a>
      </div>
    </div>
  </div>
</template>
