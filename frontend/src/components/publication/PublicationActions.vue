<script lang="ts" setup>
import { storeToRefs } from "pinia"
import Button from "primevue/button"
import { Ref, ref } from "vue"
import InfoModal from "@/components/InfoModal.vue"
import PortalPublicationStatusBadge from "@/components/publication/PortalPublicationStatusBadge.vue"
import { Decision } from "@/domain/decision"
import { PortalPublicationStatus } from "@/domain/portalPublicationStatus"
import { ResponseError } from "@/services/httpClient"
import publishDocumentationUnitService from "@/services/publishDocumentationUnitService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const props = defineProps<{ isPublishable: boolean }>()

const store = useDocumentUnitStore()
const { documentUnit: decision } = storeToRefs(store) as {
  documentUnit: Ref<Decision>
}

const docUnitPublicationError = ref<ResponseError | null>(null)

const isPublishing = ref(false)
const publishDocUnit = async () => {
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
  await new Promise((resolve) => setTimeout(resolve, 1000)) // Simulate API call
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
      <Button
        aria-label="Veröffentlichen"
        :disabled="!props.isPublishable || isWithdrawing || isPublishing"
        label="Veröffentlichen"
        :loading="isPublishing"
        size="small"
        @click="publishDocUnit"
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
