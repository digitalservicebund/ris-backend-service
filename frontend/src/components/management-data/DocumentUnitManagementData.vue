<script lang="ts" setup>
import { storeToRefs } from "pinia"
import Button from "primevue/button"
import { useToast } from "primevue/usetoast"
import { computed, onBeforeMount, Ref, ref, shallowRef, watchEffect } from "vue"
import { useRouter } from "vue-router"
import ComboboxInput from "@/components/ComboboxInput.vue"
import DocumentUnitDeleteButton from "@/components/DocumentUnitDeleteButton.vue"
import InfoModal from "@/components/InfoModal.vue"
import { ComboboxItem } from "@/components/input/types"
import InputErrorMessages from "@/components/InputErrorMessages.vue"
import DocumentUnitHistoryLog from "@/components/management-data/DocumentUnitHistoryLog.vue"
import DuplicateRelationListItem from "@/components/management-data/DuplicateRelationListItem.vue"
import ManagementDataMetadata from "@/components/management-data/ManagementDataMetadata.vue"
import TitleElement from "@/components/TitleElement.vue"
import DocumentationOffice from "@/domain/documentationOffice"
import { DocumentationUnitHistoryLog } from "@/domain/documentationUnitHistoryLog"
import DocumentUnit from "@/domain/documentUnit"
import ComboboxItemService from "@/services/comboboxItemService"
import DocumentUnitHistoryLogService from "@/services/documentUnitHistoryLogService"
import DocumentUnitService from "@/services/documentUnitService"
import { ResponseError } from "@/services/httpClient"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import IconCheck from "~icons/ic/baseline-check"

const { documentUnit } = storeToRefs(useDocumentUnitStore())
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
const documentationOffice = ref<DocumentationOffice>()
const hasNoSelection = ref(false)

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

const documentationOfficeInput = computed({
  get: () =>
    documentationOffice.value?.abbreviation
      ? {
          label: documentationOffice.value.abbreviation,
          value: documentationOffice.value,
        }
      : undefined,
  set: (newValue) => {
    if (newValue) {
      hasNoSelection.value = false
    }
    documentationOffice.value = { ...newValue } as DocumentationOffice
  },
})
const assignDocumentationOffice = async () => {
  if (documentationOfficeInput.value && documentationOffice.value?.id) {
    hasNoSelection.value = false
    const response = await DocumentUnitService.assignDocumentationOffice(
      documentUnit.value!.uuid,
      documentationOffice.value.id,
    )
    if (response.error) {
      assignDocOfficeResponseError.value = response.error
    } else {
      assignDocOfficeResponseError.value = undefined
      await router.push({ path: "/" })
      toast.add({
        severity: "success",
        summary: "Zuweisen erfolgreich",
        detail: `Die Dokumentationseinheit ${documentUnit.value!.documentNumber} ist jetzt in der Zuständigkeit der Dokumentationsstelle ${documentationOffice.value?.abbreviation}.`,
        life: 5_000,
      })
    }
  } else {
    hasNoSelection.value = true
  }
}
/**
 * @summary Provides a dynamically filtered list of documentation offices for a combobox.
 * @description
 * Fetches documentation offices and then applies a client-side exclusion
 * based on the current `documentUnit`. It uses `watchEffect` to ensure the returned
 * `data` property reactively updates if the fetched list or exclusion criteria change.
 */
const getDocumentationOffices = (filter: Ref<string | undefined>) => {
  const rawFetchResult = ComboboxItemService.getDocumentationOffices(filter)
  const filteredDataRef = shallowRef<ComboboxItem[] | null>(null)

  watchEffect(() => {
    const comboboxItems: ComboboxItem[] | null = rawFetchResult.data.value
    if (comboboxItems) {
      filteredDataRef.value = comboboxItems.filter(
        (item) =>
          item.label !==
          documentUnit.value?.coreData.documentationOffice?.abbreviation,
      )
    } else {
      filteredDataRef.value = null
    }
  })
  return {
    ...rawFetchResult,
    data: filteredDataRef,
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
      <dl>
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
      <dl>
        <div class="flex gap-24">
          <dt
            class="ris-body1-bold min-w-160"
            data-testid="assign-documentation-office-title"
          >
            Zuweisen
          </dt>
          <dd class="ris-body2-regular flex flex-wrap items-start gap-8">
            <div class="min-w-2xs">
              <ComboboxInput
                id="documentationOfficeInput"
                v-model="documentationOfficeInput"
                aria-label="Dokumentationsstelle auswählen"
                data-testid="documentation-office-combobox"
                :has-error="hasNoSelection"
                :item-service="getDocumentationOffices"
                placeholder="Dokumentationsstelle auswählen"
              />
              <InputErrorMessages
                v-if="hasNoSelection"
                class="ris-body3-regular"
                error-message="Wählen Sie eine Dokumentationsstelle aus"
              />
            </div>
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
