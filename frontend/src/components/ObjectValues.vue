<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { Ref } from "vue"
import EditableList from "@/components/EditableList.vue"
import ObjectValueInput from "@/components/ObjectValueInput.vue"
import ObjectValueSummary from "@/components/ObjectValueSummary.vue"
import { Decision } from "@/domain/decision"
import ObjectValue from "@/domain/objectValue"
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
  <div id="objectValues" class="ris-label2-regular mb-16">
    {{ label }}
  </div>
  <div class="flex flex-row">
    <div class="flex-1">
      <EditableList
        v-model="decision!.contentRelatedIndexing.objectValues"
        :create-entry="() => new ObjectValue()"
        :edit-component="ObjectValueInput"
        :summary-component="ObjectValueSummary"
      />
    </div>
  </div>
</template>
