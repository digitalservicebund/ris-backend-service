<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { Ref } from "vue"
import CountryOfOriginInput from "@/components/CountryOfOriginInput.vue"
import CountryOfOriginSummary from "@/components/CountryOfOriginSummary.vue"
import EditableList from "@/components/EditableList.vue"
import CountryOfOrigin from "@/domain/countryOfOrigin"
import { Decision } from "@/domain/decision"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

defineProps<{
  label: string
}>()

const store = useDocumentUnitStore()
const { documentUnit: decision } = storeToRefs(store) as {
  documentUnit: Ref<Decision | undefined>
}
</script>

<template>
  <div id="countriesOfOrigin" class="ris-label2-regular mb-16">
    {{ label }}
  </div>
  <div class="flex flex-row">
    <div class="flex-1">
      <EditableList
        v-model="decision!.contentRelatedIndexing.countriesOfOrigin"
        :create-entry="() => new CountryOfOrigin()"
        :edit-component="CountryOfOriginInput"
        :summary-component="CountryOfOriginSummary"
      />
    </div>
  </div>
</template>
