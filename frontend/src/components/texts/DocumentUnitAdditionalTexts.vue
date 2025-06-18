<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { type Component, computed, Ref } from "vue"
import CategoryWrapper from "@/components/CategoryWrapper.vue"
import ParticipatingJudges from "@/components/ParticipatingJudges.vue"
import TextEditorCategory from "@/components/texts/TextEditorCategory.vue"
import { useInternalUser } from "@/composables/useInternalUser"
import { useValidBorderNumberLinks } from "@/composables/useValidBorderNumberLinks"
import { Decision } from "@/domain/decision"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import TextEditorUtil from "@/utils/textEditorUtil"

defineProps<{
  registerTextEditorRef: (key: string, el: Component | null) => void
}>()

const store = useDocumentUnitStore()
const { documentUnit } = storeToRefs(store) as {
  documentUnit: Ref<Decision | undefined>
}

const isInternalUser = useInternalUser()

const otherLongText = computed({
  get: () =>
    documentUnit.value?.longTexts.otherLongText
      ? useValidBorderNumberLinks(
          documentUnit.value?.longTexts.otherLongText,
          documentUnit.value.managementData.borderNumbers,
        )
      : undefined,
  set: (newValue) => {
    documentUnit.value!.longTexts.otherLongText =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const dissentingOpinion = computed({
  get: () =>
    documentUnit.value?.longTexts.dissentingOpinion
      ? useValidBorderNumberLinks(
          documentUnit.value?.longTexts.dissentingOpinion,
          documentUnit.value.managementData.borderNumbers,
        )
      : undefined,
  set: (newValue) => {
    documentUnit.value!.longTexts.dissentingOpinion =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const outline = computed({
  get: () =>
    documentUnit.value?.longTexts.outline
      ? useValidBorderNumberLinks(
          documentUnit.value?.longTexts.outline,
          documentUnit.value.managementData.borderNumbers,
        )
      : undefined,
  set: (newValue) => {
    documentUnit.value!.longTexts.outline =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})
</script>

<template>
  <div aria-label="Weitere Langtexte">
    <h2 class="ris-label1-bold mb-16">Weitere Langtexte</h2>
    <div class="flex flex-col gap-24">
      <TextEditorCategory
        id="otherLongText"
        v-bind="{ registerTextEditorRef }"
        v-model="otherLongText"
        data-testid="otherLongText"
        :editable="isInternalUser"
        label="Sonstiger Langtext"
        :should-show-button="!documentUnit?.longTexts?.otherLongText?.length"
      />

      <TextEditorCategory
        id="dissentingOpinion"
        v-bind="{ registerTextEditorRef }"
        v-model="dissentingOpinion"
        data-testid="dissentingOpinion"
        :editable="isInternalUser"
        label="Abweichende Meinung"
        :should-show-button="
          !documentUnit?.longTexts?.dissentingOpinion?.length
        "
      />

      <CategoryWrapper
        label="Mitwirkende Richter"
        :should-show-button="
          !documentUnit?.longTexts?.participatingJudges?.length
        "
      >
        <ParticipatingJudges label="Mitwirkende Richter" />
      </CategoryWrapper>

      <div class="gap-0">
        <TextEditorCategory
          v-bind="{ registerTextEditorRef }"
          id="outline"
          v-model="outline"
          data-testid="outline"
          :editable="isInternalUser"
          label="Gliederung"
          :should-show-button="!documentUnit?.longTexts?.outline?.length"
        />
      </div>
    </div>
  </div>
</template>
