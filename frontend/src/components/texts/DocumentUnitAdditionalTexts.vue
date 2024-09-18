<script lang="ts" setup>
import { computed, ref } from "vue"
import CategoryWrapper from "@/components/CategoryWrapper.vue"
import DocumentUnitTextField from "@/components/texts/DocumentUnitTextField.vue"
import { useInternalUser } from "@/composables/useInternalUser"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import TextEditorUtil from "@/utils/textEditorUtil"

const store = useDocumentUnitStore()

const isInternalUser = useInternalUser()

const hasOtherLongtext = ref<boolean>(
  store.documentUnit?.longTexts?.otherLongText
    ? store.documentUnit?.longTexts?.otherLongText?.length > 0
    : false,
)

const hasDissentingOpinion = ref<boolean>(
  store.documentUnit?.longTexts?.dissentingOpinion
    ? store.documentUnit?.longTexts?.dissentingOpinion?.length > 0
    : false,
)

const hasOutline = ref<boolean>(
  store.documentUnit?.longTexts?.outline
    ? store.documentUnit?.longTexts?.outline?.length > 0
    : false,
)

const otherLongText = computed({
  get: () => store.documentUnit?.longTexts.otherLongText,
  set: (newValue) => {
    store.documentUnit!.longTexts.otherLongText =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const dissentingOpinion = computed({
  get: () => store.documentUnit?.longTexts.dissentingOpinion,
  set: (newValue) => {
    store.documentUnit!.longTexts.dissentingOpinion =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const outline = computed({
  get: () => store.documentUnit?.longTexts.outline,
  set: (newValue) => {
    store.documentUnit!.longTexts.outline =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})
</script>

<template>
  <h2 class="ds-label-01-bold mb-16 mt-24">Weitere Langtexte</h2>
  <div class="flex flex-col gap-24">
    <CategoryWrapper
      label="Sonstiger Langtext"
      :should-show-button="!hasOtherLongtext"
    >
      <DocumentUnitTextField
        id="otherLongText"
        v-model="otherLongText"
        :editable="isInternalUser"
        label="Sonstiger Langtext"
      />
    </CategoryWrapper>

    <CategoryWrapper
      label="Abweichende Meinung"
      :should-show-button="!hasDissentingOpinion"
    >
      <DocumentUnitTextField
        id="dissentingOpinion"
        v-model="dissentingOpinion"
        :editable="isInternalUser"
        label="Abweichende Meinung"
      />
    </CategoryWrapper>

    <CategoryWrapper label="Gliederung" :should-show-button="!hasOutline">
      <DocumentUnitTextField
        id="outline"
        v-model="outline"
        :editable="isInternalUser"
        label="Gliederung"
      />
    </CategoryWrapper>
  </div>
</template>
