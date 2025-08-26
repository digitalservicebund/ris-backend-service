<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { computed, onBeforeMount, Ref, ref } from "vue"
import CodeSnippet from "@/components/CodeSnippet.vue"
import ExpandableContent from "@/components/ExpandableContent.vue"
import InfoModal from "@/components/InfoModal.vue"
import { LdmlPreview } from "@/components/input/types"
import PublicationActions from "@/components/publication/PublicationActions.vue"
import TitleElement from "@/components/TitleElement.vue"
import { useFeatureToggle } from "@/composables/useFeatureToggle"
import PendingProceeding from "@/domain/pendingProceeding"
import publishDocumentationUnitService from "@/services/publishDocumentationUnitService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const isPortalPublicationEnabled = useFeatureToggle("neuris.portal-publication")
const isPublishable = computed(() => isPortalPublicationEnabled.value)

const store = useDocumentUnitStore()
const { documentUnit: pendingProceeding } = storeToRefs(store) as {
  documentUnit: Ref<PendingProceeding | undefined>
}

const preview = ref<LdmlPreview>()
const previewError = ref()
const fetchPreview = async () => {
  const previewResponse = await publishDocumentationUnitService.getPreview(
    pendingProceeding.value!.uuid,
  )
  if (previewResponse.error) {
    previewError.value = previewResponse.error
  } else if (previewResponse.data?.ldml) {
    preview.value = previewResponse.data
  }
}

onBeforeMount(async () => {
  // Save doc unit in case there are any unsaved local changes before fetching ldml preview
  await store.updateDocumentUnit()
  await fetchPreview()
})
</script>

<template>
  <div class="flex w-full flex-1 grow flex-col gap-32 p-24">
    <div class="flex w-full flex-col gap-24 bg-white p-24">
      <TitleElement>Prüfen</TitleElement>
      <div
        v-if="preview?.success && !!preview.ldml"
        class="border-b-1 border-b-gray-400"
      ></div>
      <ExpandableContent
        v-if="preview?.success && !!preview.ldml"
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
        v-if="previewError"
        aria-label="Fehler beim Laden der LDML-Vorschau"
        :description="previewError.description"
        :title="previewError.title"
      />
    </div>
    <div class="flex w-full flex-col gap-24 bg-white p-24">
      <TitleElement>Veröffentlichen und Zurückziehen</TitleElement>
      <PublicationActions
        :is-publishable="isPublishable"
        :publication-warnings="[]"
      />
    </div>
  </div>
</template>
