<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { Ref } from "vue"
import EditableList from "@/components/EditableList.vue"
import ForeignLanguageVersionInput from "@/components/ForeignLanguageVersionInput.vue"
import ForeignLanguageVersionSummary from "@/components/ForeignLanguageVersionSummary.vue"
import { Decision } from "@/domain/decision"
import ForeignLanguageVersion from "@/domain/foreignLanguageVersion"
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
  <div id="foreignLanguageVersions" class="ris-label2-regular mb-16">
    {{ label }}
  </div>
  <div class="flex flex-row">
    <div class="flex-1">
      <EditableList
        v-model="decision!.contentRelatedIndexing.foreignLanguageVersions"
        :create-entry="() => new ForeignLanguageVersion()"
        :edit-component="ForeignLanguageVersionInput"
        :summary-component="ForeignLanguageVersionSummary"
      />
    </div>
  </div>
</template>
