<script lang="ts" setup>
import CollectiveAgreementInput from "@/components/CollectiveAgreementInput.vue"
import CollectiveAgreementSummary from "@/components/CollectiveAgreementSummary.vue"
import EditableList from "@/components/EditableList.vue"
import { CollectiveAgreement } from "@/domain/collectiveAgreement"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

defineProps<{
  label: string
}>()

const store = useDocumentUnitStore()
</script>

<template>
  <div aria-label="Tarifvertrag">
    <h3 id="collectiveAgreements" class="ris-label2-regular mb-16">
      {{ label }}
    </h3>
    <div class="flex flex-row">
      <div class="flex-1">
        <EditableList
          v-model="
            store.documentUnit!.contentRelatedIndexing.collectiveAgreements
          "
          :create-entry="() => new CollectiveAgreement()"
          :edit-component="CollectiveAgreementInput"
          :summary-component="CollectiveAgreementSummary"
        />
      </div>
    </div>
  </div>
</template>
