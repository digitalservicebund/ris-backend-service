<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { Ref } from "vue"
import EditableList from "@/components/EditableList.vue"
import OriginOfTranslationInput from "@/components/OriginOfTranslationInput.vue"
import OriginOfTranslationSummary from "@/components/OriginOfTranslationSummary.vue"
import { Decision } from "@/domain/decision"
import OriginOfTranslation from "@/domain/originOfTranslation"
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
  <div id="originOfTranslations" class="ris-label2-regular mb-16">
    {{ label }}
  </div>
  <div class="flex flex-row">
    <div class="flex-1">
      <EditableList
        v-model="decision!.contentRelatedIndexing.originOfTranslations"
        :create-entry="() => new OriginOfTranslation()"
        :edit-component="OriginOfTranslationInput"
        :summary-component="OriginOfTranslationSummary"
      />
    </div>
  </div>
</template>
