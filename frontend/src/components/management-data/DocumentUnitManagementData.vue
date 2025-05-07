<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { onBeforeMount, ref } from "vue"
import DocumentUnitDeleteButton from "@/components/DocumentUnitDeleteButton.vue"
import DocumentUnitHistoryLog from "@/components/management-data/DocumentUnitHistoryLog.vue"
import DuplicateRelationListItem from "@/components/management-data/DuplicateRelationListItem.vue"
import ManagementDataMetadata from "@/components/management-data/ManagementDataMetadata.vue"
import TitleElement from "@/components/TitleElement.vue"
import { DocumentationUnitHistoryLog } from "@/domain/documentationUnitHistoryLog"
import DocumentUnit from "@/domain/documentUnit"
import DocumentUnitHistoryLogService from "@/services/documentUnitHistoryLogService"
import { ResponseError } from "@/services/httpClient"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import IconCheck from "~icons/ic/baseline-check"

const { documentUnit } = storeToRefs(useDocumentUnitStore())
const { updateDocumentUnit } = useDocumentUnitStore()

onBeforeMount(async () => {
  // Save before navigation
  await updateDocumentUnit()
  // Load history only after latest changes are saved
  await loadHistory()
})

const historyLogs = ref<DocumentationUnitHistoryLog[]>()
const error = ref<ResponseError>()
const isLoading = ref(true)

const loadHistory = async () => {
  isLoading.value = true
  const response = await DocumentUnitHistoryLogService.get(
    documentUnit.value!.uuid!,
  )
  if (response.error) {
    error.value = response.error
  } else if (response.data) {
    historyLogs.value = response.data
  }
  isLoading.value = false
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
        :error="error"
        :loading="isLoading"
      />
      <dl>
        <div class="flex gap-24 px-0">
          <dt class="ris-body1-bold shrink-0 grow-0 basis-160">
            Dublettenverdacht:
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
    </div>
    <div class="flex flex-col gap-24 bg-white p-24">
      <TitleElement
        >Dokumentationseinheit "{{ documentUnit?.documentNumber }}"
        löschen</TitleElement
      >
      <DocumentUnitDeleteButton
        :document-number="documentUnit?.documentNumber!"
        :uuid="documentUnit?.uuid!"
      />
    </div>
  </div>
</template>
