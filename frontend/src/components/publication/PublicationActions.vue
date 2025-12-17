<script lang="ts" setup>
import { storeToRefs } from "pinia"
import Button from "primevue/button"
import { useToast } from "primevue/usetoast"
import { computed, Ref, ref } from "vue"
import InfoModal from "@/components/InfoModal.vue"
import PendingProceedingSummary from "@/components/PendingProceedingSummary.vue"
import PopupModal from "@/components/PopupModal.vue"
import PortalPublicationStatusBadge from "@/components/publication/PortalPublicationStatusBadge.vue"
import UatTestPortalInfo from "@/components/publication/UatTestPortalInfo.vue"
import { useFeatureToggle } from "@/composables/useFeatureToggle"
import { Decision } from "@/domain/decision"
import { PortalPublicationStatus } from "@/domain/portalPublicationStatus"
import { ResponseError } from "@/services/httpClient"
import publishDocumentationUnitService from "@/services/publishDocumentationUnitService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import useSessionStore from "@/stores/sessionStore"
import DateUtil from "@/utils/dateUtil"
import IconInfoOutline from "~icons/mdi/information-outline"

const props = defineProps<{
  isPublishable: boolean
  publicationWarnings: string[]
}>()

const store = useDocumentUnitStore()
const { documentUnit: decision } = storeToRefs(store) as {
  documentUnit: Ref<Decision>
}

const isPortalPublicationEnabled = useFeatureToggle("neuris.portal-publication")

const session = useSessionStore()
const linkToPortal = computed(
  () => session.env?.portalUrl + "/case-law/" + decision.value.documentNumber,
)

const docUnitPublicationError = ref<ResponseError | null>(null)

const toastService = useToast()

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
const isPublished = ref(false)
const hasRelatedPendingProceedingsError = ref(false)
const publishDocUnit = async () => {
  showPublicationWarningModal.value = false
  docUnitPublicationError.value = null
  hasRelatedPendingProceedingsError.value = false
  isPublishing.value = true
  const { data, error } = await publishDocumentationUnitService.publishDocument(
    store.documentUnit!.uuid!,
  )
  docUnitPublicationError.value = error ?? null
  await store.loadDocumentUnit(decision.value.documentNumber)
  isPublishing.value = false
  isPublished.value = error == null
  isWithdrawn.value = false

  if (data?.relatedPendingProceedingsPublicationResult === "SUCCESS")
    toastService.add({
      severity: "success",
      life: 5_000,
      summary:
        "Die zugehörigen anhängigen Verfahren wurden als erledigt veröffentlicht.",
    })
  if (data?.relatedPendingProceedingsPublicationResult === "ERROR")
    hasRelatedPendingProceedingsError.value = true
}

const isWithdrawing = ref(false)
const isWithdrawn = ref(false)
const withdrawDocUnit = async () => {
  hasRelatedPendingProceedingsError.value = false
  docUnitPublicationError.value = null
  isWithdrawing.value = true
  const { error } = await publishDocumentationUnitService.withdrawDocument(
    store.documentUnit!.uuid!,
  )
  docUnitPublicationError.value = error ?? null
  await store.loadDocumentUnit(decision.value.documentNumber)
  isWithdrawing.value = false
  isPublished.value = false
  isWithdrawn.value = error == null
}

const lastPublishedAt = computed(() => {
  if (decision.value.managementData.lastPublishedAtDateTime) {
    return DateUtil.formatDateTime(
      decision.value.managementData.lastPublishedAtDateTime,
    )
  }
  return "-"
})

const hasRelatedPendingProceeding = computed(() => {
  return (
    decision.value.contentRelatedIndexing?.relatedPendingProceedings &&
    decision.value.contentRelatedIndexing?.relatedPendingProceedings?.length > 0
  )
})
</script>

<template>
  <div class="flex flex-col gap-24">
    <div class="flex flex-col gap-16">
      <UatTestPortalInfo />
      <h3 class="ris-label1-bold">Aktueller Status Portal</h3>
      <PortalPublicationStatusBadge
        :status="decision.portalPublicationStatus"
      />
      <div v-if="hasRelatedPendingProceeding">
        Mit dieser Entscheidung sind folgende anhängige Verfahren verknüpft:
        <div
          v-for="(pendingProceeding, id) in decision.contentRelatedIndexing
            .relatedPendingProceedings"
          :key="id"
        >
          <PendingProceedingSummary :data="pendingProceeding" />
        </div>
      </div>
      <div
        v-if="
          decision.portalPublicationStatus ===
            PortalPublicationStatus.PUBLISHED ||
          decision.portalPublicationStatus === PortalPublicationStatus.WITHDRAWN
        "
        class="flex flex-row items-center gap-8"
      >
        <IconInfoOutline class="text-grey-900" />
        <a
          class="ris-link1-regular whitespace-nowrap no-underline focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
          :href="linkToPortal"
          rel="noopener noreferrer"
          target="_blank"
        >
          Portalseite der Dokumentationseinheit
        </a>
        <div
          v-if="
            decision.portalPublicationStatus ===
            PortalPublicationStatus.WITHDRAWN
          "
        >
          wurde entfernt
        </div>
      </div>

      <div v-if="isPublished" class="flex flex-row gap-8">
        <IconInfoOutline class="text-grey-900" />
        <p>
          Das Hochladen der Stammdaten und der Informationen im Portal-Tab
          „Details“ dauert ungefähr 5 Minuten.
        </p>
      </div>
      <div
        v-if="
          decision.portalPublicationStatus ===
            PortalPublicationStatus.PUBLISHED ||
          decision.portalPublicationStatus === PortalPublicationStatus.WITHDRAWN
        "
        class="flex flex-row gap-8"
      >
        <IconInfoOutline class="text-grey-900" />
        <p>
          Zuletzt veröffentlicht am:
          {{ lastPublishedAt }}
        </p>
      </div>
    </div>
    <InfoModal
      v-if="docUnitPublicationError"
      aria-label="Fehler bei der Veröffentlichung/Zurückziehung"
      :description="docUnitPublicationError.description"
      :title="docUnitPublicationError.title"
    />
    <InfoModal
      v-if="hasRelatedPendingProceedingsError"
      aria-label="Fehler beim Veröffentlichen zugehöriger anhängiger Verfahren"
      description="Die zugehörigen anhängigen Verfahren konnten nicht vollständig als erledigt veröffentlicht werden. Bitte stellen Sie sicher, dass die anhängigen Verfahren bereits im Portal veröffentlicht sind und alle Pflichtfelder befüllt haben."
      title="Zugehörige anhängige Verfahren nicht veröffentlicht"
    />
    <InfoModal
      v-if="!isPortalPublicationEnabled"
      aria-label="Portal-Veröffentlichung deaktiviert"
      description="Auf Produktion ist die manuelle Portal-Veröffentlichung deaktiviert. Sie können veröffentlichte Dokumentationseinheiten jedoch manuell zurückziehen. Beachten Sie, dass die Dokumentationseinheit durch die jDV-Delta-Migration erneut automatisiert veröffentlicht werden kann."
      title="Portal-Veröffentlichung deaktiviert"
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
        :disabled="
          !props.isPublishable ||
          isWithdrawing ||
          isPublishing ||
          !isPortalPublicationEnabled
        "
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
