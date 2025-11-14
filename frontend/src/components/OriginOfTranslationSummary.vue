<script lang="ts" setup>
import OriginOfTranslationSummaryPreview from "@/components/OriginOfTranslationSummaryPreview.vue"
import OriginOfTranslation from "@/domain/originOfTranslation"
import IcBaselineTranslate from "~icons/ic/baseline-translate"

const props = defineProps<{
  data: OriginOfTranslation
}>()
</script>

<template>
  <div
    class="flex w-full justify-between"
    data-testid="origin-of-translation-summary"
  >
    <div class="flex flex-row items-center">
      <IcBaselineTranslate class="mr-8" />
      <div class="ris-label1-regular mr-8 max-w-[calc(30vw)] truncate">
        <OriginOfTranslationSummaryPreview :data="props.data" />
      </div>
    </div>
  </div>
</template>
