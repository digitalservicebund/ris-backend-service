<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { type Component, Ref } from "vue"
import CorrectionInput from "@/components/CorrectionInput.vue"
import CorrectionSummary from "@/components/CorrectionSummary.vue"
import EditableList from "@/components/EditableList.vue"
import Correction from "@/domain/correction"
import { Decision } from "@/domain/decision"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

defineProps<{
  label: string
  registerTextEditorRef: (key: string, el: Component | null) => void
}>()

const store = useDocumentUnitStore()
const { documentUnit: decision } = storeToRefs(store) as {
  documentUnit: Ref<Decision | undefined>
}
</script>

<template>
  <div id="corrections" class="ris-label2-regular mb-16">
    {{ label }}
  </div>
  <div aria-label="Berichtigungen" data-testid="Berichtigungen">
    <div class="flex flex-row">
      <div class="flex-1">
        <EditableList
          v-model="decision!.longTexts.corrections"
          :create-entry="() => new Correction()"
          :summary-component="CorrectionSummary"
        >
          <template
            #edit="{
              onAddEntry,
              onCancelEdit,
              onRemoveEntry,
              modelValueList,
              value,
              'onUpdate:value': onUpdateValue,
            }"
          >
            <CorrectionInput
              :model-value="value"
              :model-value-list="modelValueList"
              :register-text-editor-ref="registerTextEditorRef"
              @add-entry="onAddEntry"
              @cancel-edit="onCancelEdit"
              @remove-entry="onRemoveEntry"
              @update:model-value="onUpdateValue"
            />
          </template>
        </EditableList>
      </div>
    </div>
  </div>
</template>
