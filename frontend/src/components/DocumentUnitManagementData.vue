<script lang="ts" setup>
import { storeToRefs } from "pinia"
import DocumentUnitDeleteButton from "@/components/DocumentUnitDeleteButton.vue"
import DuplicateRelationListItem from "@/components/DuplicateRelationListItem.vue"
import TitleElement from "@/components/TitleElement.vue"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import IconCheck from "~icons/ic/baseline-check"

const { documentUnit } = storeToRefs(useDocumentUnitStore())
</script>

<template>
  <div class="w-full grow p-24">
    <div class="flex flex-col gap-24 bg-white p-24">
      <TitleElement>Verwaltungsdaten</TitleElement>
      <dl class="my-16">
        <div class="flex gap-24 px-0">
          <dt class="ris-label2-bold shrink-0 grow-0 basis-160">
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
