<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { Ref } from "vue"
import AbuseFeeInput from "@/components/AbuseFeeInput.vue"
import AbuseFeeSummary from "@/components/AbuseFeeSummary.vue"
import EditableList from "@/components/EditableList.vue"
import AbuseFee from "@/domain/abuseFee"
import { Decision } from "@/domain/decision"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

defineProps<{
  label: string
}>()

const store = useDocumentUnitStore()
const { documentUnit: decision } = storeToRefs(store) as {
  documentUnit: Ref<Decision>
}
</script>

<template>
  <div id="abuseFees" class="ris-label2-regular mb-16">
    {{ label }}
  </div>
  <div class="flex flex-row">
    <div class="flex-1">
      <EditableList
        v-model="decision!.contentRelatedIndexing.abuseFees"
        :create-entry="() => new AbuseFee()"
        :edit-component="AbuseFeeInput"
        :summary-component="AbuseFeeSummary"
      />
    </div>
  </div>
</template>
