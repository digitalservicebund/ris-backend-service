<script lang="ts" setup>
import { storeToRefs } from "pinia"
import Button from "primevue/button"
import { useToast } from "primevue/usetoast"
import { computed, onBeforeMount, Ref, ref } from "vue"
import { useRouter } from "vue-router"
import DocumentationOfficeSelector from "@/components/DocumentationOfficeSelector.vue"
import DocumentUnitDeleteButton from "@/components/DocumentUnitDeleteButton.vue"
import InfoModal from "@/components/InfoModal.vue"
import DocumentUnitHistoryLog from "@/components/management-data/DocumentUnitHistoryLog.vue"
import DuplicateRelationListItem from "@/components/management-data/DuplicateRelationListItem.vue"
import ManagementDataMetadata from "@/components/management-data/ManagementDataMetadata.vue"
import TitleElement from "@/components/TitleElement.vue"
import DocumentationOffice from "@/domain/documentationOffice"
import { DocumentationUnitHistoryLog } from "@/domain/documentationUnitHistoryLog"
import DocumentUnit from "@/domain/documentUnit"
import DocumentUnitHistoryLogService from "@/services/documentUnitHistoryLogService"
import DocumentUnitService from "@/services/documentUnitService"
import { ResponseError } from "@/services/httpClient"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { isPendingProceeding } from "@/utils/typeGuards"
import IconCheck from "~icons/ic/baseline-check"

const { documentUnit } = storeToRefs(useDocumentUnitStore()) as {
  documentUnit: Ref<DocumentUnit | undefined>
}
const { updateDocumentUnit } = useDocumentUnitStore()
const router = useRouter()
const toast = useToast()

onBeforeMount(async () => {
  // Save before navigation
  await updateDocumentUnit()
  // Load history only after latest changes are saved
  await loadHistory()
})

const historyLogs = ref<DocumentationUnitHistoryLog[]>()
const historyLogResponseError = ref<ResponseError>()
const assignDocOfficeResponseError = ref<ResponseError>()

const isLoading = ref(true)
const selectedDocumentationOffice = ref<DocumentationOffice | undefined>()
const hasDocumentationOfficeError = ref(false)

const loadHistory = async () => {
  isLoading.value = true
  const response = await DocumentUnitHistoryLogService.get(
    documentUnit.value!.uuid!,
  )
  if (response.error) {
    historyLogResponseError.value = response.error
  } else if (response.data) {
    historyLogs.value = response.data
  }
  isLoading.value = false
}
const abbreviationsToExclude = computed(() => {
  const currentOfficeAbbr =
    documentUnit.value?.coreData.documentationOffice?.abbreviation
  const exclusions: string[] = []

  if (currentOfficeAbbr) {
    exclusions.push(currentOfficeAbbr)
  }

  return exclusions
})

const assignDocumentationOffice = async () => {
  if (selectedDocumentationOffice.value?.id) {
    hasDocumentationOfficeError.value = false
    const response = await DocumentUnitService.assignDocumentationOffice(
      documentUnit.value!.uuid,
      selectedDocumentationOffice.value.id,
    )
    if (response.error) {
      assignDocOfficeResponseError.value = response.error
    } else {
      assignDocOfficeResponseError.value = undefined
      await router.push({ path: "/" })
      toast.add({
        severity: "success",
        summary: "Zuweisen erfolgreich",
        detail: `Die Dokumentationseinheit ${documentUnit.value!.documentNumber} ist jetzt in der Zuständigkeit der Dokumentationsstelle ${selectedDocumentationOffice.value?.abbreviation}.`,
        life: 5_000,
      })
    }
  } else {
    hasDocumentationOfficeError.value = true
  }
}
</script>

<template>
  <div class="w-full flex-1 grow p-24">
    <div class="flex flex-col gap-24 bg-white p-24">
      <TitleElement>Verwaltungsdaten</TitleElement>
      <ManagementDataMetadata
        v-if="documentUnit"
        :document-unit="documentUnit as DocumentUnit"
      />
      <DocumentUnitHistoryLog
        :data="historyLogs"
        :error="historyLogResponseError"
        :loading="isLoading"
      />
      <dl v-if="!isPendingProceeding(documentUnit)">
        <div class="flex gap-24 px-0 py-16">
          <dt class="ris-body1-bold shrink-0 grow-0 basis-160">
            Dublettenverdacht
          </dt>
          <dd class="ris-body2-regular flex flex-col gap-32">
            <DuplicateRelationListItem
              v-for="duplicateRelation in documentUnit?.managementData
                .duplicateRelations"
              :key="duplicateRelation.documentNumber"
              :duplicate-relation="duplicateRelation"
            />
            <div
              v-if="!documentUnit?.managementData.duplicateRelations.length"
              class="flex flex-row gap-8"
            >
              <IconCheck class="text-green-700" />
              <span>Es besteht kein Dublettenverdacht.</span>
            </div>
          </dd>
        </div>
      </dl>
      <div v-if="assignDocOfficeResponseError">
        <InfoModal
          data-testid="assignDocOfficeErrorModal"
          :description="assignDocOfficeResponseError.description"
          :title="assignDocOfficeResponseError.title"
        />
      </div>
      <dl v-if="!isPendingProceeding(documentUnit)">
        <div class="flex gap-24">
          <dt
            class="ris-body1-bold min-w-160"
            data-testid="assign-documentation-office-title"
          >
            Zuweisen
          </dt>
          <dd class="flex flex-wrap items-start gap-8">
            <DocumentationOfficeSelector
              v-model="selectedDocumentationOffice"
              v-model:has-error="hasDocumentationOfficeError"
              class="min-w-xs"
              :exclude-office-abbreviations="abbreviationsToExclude"
            />
            <Button
              aria-label="Zuweisen"
              label="Zuweisen"
              severity="secondary"
              @click="assignDocumentationOffice"
            />
          </dd>
        </div>
      </dl>
      <dl>
        <div class="flex gap-24 px-0">
          <dt class="ris-body1-bold shrink-0 grow-0 basis-160">Löschen</dt>
          <dd class="ris-body2-regular flex flex-col gap-32">
            <div class="flex flex-row gap-8">
              <DocumentUnitDeleteButton
                :document-number="documentUnit?.documentNumber!"
                :uuid="documentUnit?.uuid!"
              />
            </div>
          </dd>
        </div>
      </dl>
    </div>
  </div>
</template>
