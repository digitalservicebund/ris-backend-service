<script lang="ts" setup>
import { storeToRefs } from "pinia"
import Button from "primevue/button"
import { computed, Ref, ref } from "vue"
import InfoModal from "@/components/InfoModal.vue"
import PopupModal from "@/components/PopupModal.vue"
import PortalPublicationStatusBadge from "@/components/publication/PortalPublicationStatusBadge.vue"
import { Decision } from "@/domain/decision"
import { PortalPublicationStatus } from "@/domain/portalPublicationStatus"
import { ResponseError } from "@/services/httpClient"
import publishDocumentationUnitService from "@/services/publishDocumentationUnitService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const props = defineProps<{
  isPublishable: boolean
  publicationWarnings: string[]
}>()

const store = useDocumentUnitStore()
const { documentUnit: decision } = storeToRefs(store) as {
  documentUnit: Ref<Decision>
}

const docUnitPublicationError = ref<ResponseError | null>(null)

const showPublicationWarningModal = ref(false)
const warningModalText = computed(
  () =>
    props.publicationWarnings.join("\n") +
    "\n\nWollen Sie das Dokument dennoch übergeben?",
)
const checkWarningsAndPublishDocUnit = async () => {
  if (props.publicationWarnings.length > 0) {
    showPublicationWarningModal.value = true
    return
  }
  await publishDocUnit()
}

const isPublishing = ref(false)
const publishDocUnit = async () => {
  showPublicationWarningModal.value = false
  docUnitPublicationError.value = null
  isPublishing.value = true
  const { error } = await publishDocumentationUnitService.publishDocument(
    store.documentUnit!.uuid!,
  )
  docUnitPublicationError.value = error ?? null
  await store.loadDocumentUnit(decision.value.documentNumber)
  isPublishing.value = false
}

const isWithdrawing = ref(false)
const withdrawDocUnit = async () => {
  docUnitPublicationError.value = null
  isWithdrawing.value = true
  const { error } = await publishDocumentationUnitService.withdrawDocument(
    store.documentUnit!.uuid!,
  )
  docUnitPublicationError.value = error ?? null
  await store.loadDocumentUnit(decision.value.documentNumber)
  isWithdrawing.value = false
}
</script>

<template>
  <div class="flex flex-col gap-24">
    <div class="flex flex-col gap-16">
      <h3 class="ris-label1-bold">Aktueller Status Portal</h3>
      <PortalPublicationStatusBadge
        :status="decision.portalPublicationStatus"
      />
    </div>
    <InfoModal
      v-if="docUnitPublicationError"
      aria-label="Fehler bei der Veröffentlichung/Zurückziehung"
      :description="docUnitPublicationError.description"
      :title="docUnitPublicationError.title"
    />
    <div class="flex flex-row gap-24">
      <PopupModal
        v-if="showPublicationWarningModal"
        aria-label="Bestätigung für Veröffentlichung bei Fehlern"
        :content-text="warningModalText"
        header-text="Prüfung hat Warnungen ergeben"
        primary-button-text="Trotzdem veröffentlichen"
        primary-button-type="primary"
        @close-modal="showPublicationWarningModal = false"
        @primary-action="publishDocUnit"
      />
      <Button
        aria-label="Veröffentlichen"
        :disabled="!props.isPublishable || isWithdrawing || isPublishing"
        label="Veröffentlichen"
        :loading="isPublishing"
        size="small"
        @click="checkWarningsAndPublishDocUnit"
      />
      <Button
        v-if="
          decision.portalPublicationStatus === PortalPublicationStatus.PUBLISHED
        "
        aria-label="Zurückziehen"
        :disabled="isPublishing || isWithdrawing"
        label="Zurückziehen"
        :loading="isWithdrawing"
        severity="secondary"
        size="small"
        @click="withdrawDocUnit"
      />
    </div>
  </div>
</template>
