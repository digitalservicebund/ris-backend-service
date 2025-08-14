<script lang="ts" setup>
import { computed, ref } from "vue"
import CodeSnippet from "@/components/CodeSnippet.vue"
import ExpandableContent from "@/components/ExpandableContent.vue"
import DecisionPlausibilityCheck from "@/components/publication/DecisionPlausibilityCheck.vue"
import PublicationActions from "@/components/publication/PublicationActions.vue"
import TitleElement from "@/components/TitleElement.vue"
import { useFeatureToggle } from "@/composables/useFeatureToggle"

const isPortalPublicationEnabled = useFeatureToggle("neuris.portal-publication")
const isPlausibilityCheckValid = ref(false)
const isPublishable = computed(
  () => isPlausibilityCheckValid.value && isPortalPublicationEnabled.value,
)
</script>

<template>
  <div class="w-full flex-1 grow p-24">
    <div class="flex w-full flex-col gap-24 bg-white p-24">
      <TitleElement>Veröffentlichen</TitleElement>
      <DecisionPlausibilityCheck
        @update-plausibility-check="
          (isValid) => (isPlausibilityCheckValid = isValid)
        "
      />
      <div class="border-b-1 border-b-gray-400"></div>
      <ExpandableContent
        v-if="isPlausibilityCheckValid"
        as-column
        class="border-b-1 border-gray-400 pb-24"
        header="XML Vorschau"
        header-class="ris-body1-bold"
        :is-expanded="false"
        title="XML Vorschau"
      >
        <CodeSnippet title="" :xml="'<?xml>' + '\nNoch nicht implementiert'" />
      </ExpandableContent>
      <PublicationActions :is-publishable="isPublishable" />
    </div>
  </div>
</template>
