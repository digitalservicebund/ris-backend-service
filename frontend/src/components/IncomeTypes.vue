<script lang="ts" setup>
import { storeToRefs } from "pinia"
import EditableList from "@/components/EditableList.vue"
import IncomeTypeInput from "@/components/IncomeTypeInput.vue"
import IncomeTypeSummary from "@/components/IncomeTypeSummary.vue"
import IncomeType from "@/domain/incomeType"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

defineProps<{
  label: string
}>()

const store = useDocumentUnitStore()
const { documentUnit: decision } = storeToRefs(store)
</script>

<template>
  <div id="income-type" class="ris-label2-regular mb-16">
    {{ label }}
  </div>
  <div aria-label="Einkunftsart" data-testid="Einkunftsart">
    <div class="flex flex-row">
      <div class="flex-1">
        <EditableList
          v-model="decision!.contentRelatedIndexing.incomeTypes"
          :create-entry="() => new IncomeType()"
          :edit-component="IncomeTypeInput"
          :summary-component="IncomeTypeSummary"
        />
      </div>
    </div>
  </div>
</template>
