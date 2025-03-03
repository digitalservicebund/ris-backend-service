<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { ref } from "vue"
import DocumentUnitDeleteButton from "@/components/DocumentUnitDeleteButton.vue"
import DuplicateRelationListItem from "@/components/DuplicateRelationListItem.vue"
import { InfoStatus } from "@/components/enumInfoStatus"
import InfoModal from "@/components/InfoModal.vue"
import TextButton from "@/components/input/TextButton.vue"
import TitleElement from "@/components/TitleElement.vue"
import portalService from "@/services/portalService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import IconCheck from "~icons/ic/baseline-check"

const { documentUnit } = storeToRefs(useDocumentUnitStore())
const errorMessage = ref()
const successMessage = ref()

const deleteFromPortal = async () => {
  const docNumber = documentUnit.value?.documentNumber
  if (!docNumber) return

  const response = await portalService.deleteFromPortal(docNumber)
  if (response.error) {
    errorMessage.value = response.error.title
  } else {
    successMessage.value =
      "Dokumentationseinheit wurde erfolgreich aus dem Portal entfernt"
  }
}
</script>

<template>
  <div class="w-full grow p-24">
    <div class="flex flex-col gap-24 bg-white p-24">
      <TitleElement>Verwaltungsdaten</TitleElement>
      <dl class="my-16">
        <div class="flex gap-24 px-0">
          <dt class="ds-label-02-bold shrink-0 grow-0 basis-160">
            Dublettenverdacht:
          </dt>
          <dd class="ds-body-02-reg flex flex-col gap-32">
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
      <div class="flex flex-row gap-24">
        <DocumentUnitDeleteButton
          :document-number="documentUnit?.documentNumber!"
          :uuid="documentUnit?.uuid!"
        />
        <TextButton
          button-type="destructive"
          label="Dokumentationseinheit aus Portal entfernen"
          size="small"
          @click="deleteFromPortal"
        />
      </div>
      <InfoModal v-if="errorMessage" :title="errorMessage" />
      <InfoModal
        v-if="successMessage"
        :status="InfoStatus.SUCCEED"
        :title="successMessage"
      />
    </div>
  </div>
</template>
