<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { computed, onMounted, Ref, ref } from "vue"
import CodeSnippet from "@/components/CodeSnippet.vue"
import ExpandableContent from "@/components/ExpandableContent.vue"
import InfoModal from "@/components/InfoModal.vue"
import { LdmlPreview } from "@/components/input/types"
import DecisionPlausibilityCheck from "@/components/publication/DecisionPlausibilityCheck.vue"
import PublicationActions from "@/components/publication/PublicationActions.vue"
import TitleElement from "@/components/TitleElement.vue"
import { useFeatureToggle } from "@/composables/useFeatureToggle"
import { Decision } from "@/domain/decision"
import publishDocumentationUnitService from "@/services/publishDocumentationUnitService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const store = useDocumentUnitStore()
const { documentUnit: decision } = storeToRefs(store) as {
  documentUnit: Ref<Decision | undefined>
}
const isPortalPublicationEnabled = useFeatureToggle("neuris.portal-publication")
const hasPlausibilityCheckPassed = ref(false)
const isPublishable = computed(
  () => hasPlausibilityCheckPassed.value && isPortalPublicationEnabled.value,
)
const preview = ref<LdmlPreview>()
const previewError = ref()
const fetchPreview = async () => {
  if (!hasPlausibilityCheckPassed.value) return

  const previewResponse = await publishDocumentationUnitService.getPreview(
    decision.value!.uuid,
  )
  if (previewResponse.error) {
    previewError.value = previewResponse.error
  } else if (previewResponse.data?.ldml) {
    preview.value = previewResponse.data
  }
}
onMounted(async () => {
  // Save doc unit in case there are any unsaved local changes before fetching ldml preview
  await store.updateDocumentUnit()
  await fetchPreview()
})
</script>

<template>
  <div class="w-full flex-1 grow p-24">
    <div class="flex w-full flex-col gap-24 bg-white p-24">
      <TitleElement>Veröffentlichen</TitleElement>
      <DecisionPlausibilityCheck
        @update-plausibility-check="
          (hasPassed) => (hasPlausibilityCheckPassed = hasPassed)
        "
      />
      <div class="border-b-1 border-b-gray-400"></div>
      <ExpandableContent
        v-if="hasPlausibilityCheckPassed && preview?.success && !!preview.ldml"
        as-column
        class="border-b-1 border-gray-400 pb-24"
        :data-set="preview"
        header="LDML Vorschau"
        header-class="ris-body1-bold"
        :is-expanded="false"
        title="LDML Vorschau"
      >
        <CodeSnippet title="" :xml="preview.ldml" />
      </ExpandableContent>
      <InfoModal
        v-if="hasPlausibilityCheckPassed && previewError"
        aria-label="Fehler beim Laden der LDML-Vorschau"
        :description="previewError.description"
        :title="previewError.title"
      />
      <PublicationActions :is-publishable="isPublishable" />
    </div>
  </div>
</template>
