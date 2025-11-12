<script setup lang="ts">
import BorderNumberLinkView from "@/components/BorderNumberLinkView.vue"
import OriginOfTranslation, {
  TranslationTypeLabels,
} from "@/domain/originOfTranslation"

const props = defineProps<{
  data: OriginOfTranslation
}>()
</script>
<template data-testid="origin-of-translation-summary-preview">
  {{ props.data.languageCode?.label
  }}<span v-for="translator in props.data.translators" :key="translator"
    >, {{ translator }}</span
  ><span
    v-for="(borderNumber, index) in props.data.borderNumbers"
    :key="borderNumber"
    >{{ index === 0 ? ": " : ", " }}
    <BorderNumberLinkView :border-number="borderNumber"
  /></span>
  <span v-for="url in props.data.urls" :key="url"
    >,
    <a
      class="ris-link1-bold whitespace-nowrap no-underline focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
      :href="/^https?:\/\//i.test(url) ? url : `https://${url}`"
      rel="noopener noreferrer"
      target="_blank"
      >{{ url }}</a
    ></span
  ><template v-if="props.data.translationType"
    >&nbsp;({{ TranslationTypeLabels[props.data.translationType] }})</template
  >
</template>
