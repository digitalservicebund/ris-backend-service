<script lang="ts" setup>
import { type Component, computed } from "vue"
import CategoryWrapper from "@/components/CategoryWrapper.vue"
import ParticipatingJudges from "@/components/ParticipatingJudges.vue"
import TextEditorCategory from "@/components/texts/TextEditorCategory.vue"
import { useInternalUser } from "@/composables/useInternalUser"
import { useValidBorderNumberLinks } from "@/composables/useValidBorderNumberLinks"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import TextEditorUtil from "@/utils/textEditorUtil"

defineProps<{
  registerTextEditorRef: (key: string, el: Component | null) => void
}>()

const store = useDocumentUnitStore()

const isInternalUser = useInternalUser()

const otherLongText = computed({
  get: () =>
    store.documentUnit?.longTexts.otherLongText
      ? useValidBorderNumberLinks(
          store.documentUnit?.longTexts.otherLongText,
          store.documentUnit.managementData.borderNumbers,
        )
      : undefined,
  set: (newValue) => {
    store.documentUnit!.longTexts.otherLongText =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const dissentingOpinion = computed({
  get: () =>
    store.documentUnit?.longTexts.dissentingOpinion
      ? useValidBorderNumberLinks(
          store.documentUnit?.longTexts.dissentingOpinion,
          store.documentUnit.managementData.borderNumbers,
        )
      : undefined,
  set: (newValue) => {
    store.documentUnit!.longTexts.dissentingOpinion =
      TextEditorUtil.getEditorContentIfPresent(newValue)
  },
})

const outline = computed({
  get: () =>
    store.documentUnit?.longTexts.outline
      ? useValidBorderNumberLinks(
          store.documentUnit?.longTexts.outline,
          store.documentUnit.managementData.borderNumbers,
        )
      : undefined,
  set: (newValue) => {
    store.documentUnit!.longTexts.outline =
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
        :should-show-button="
          !store.documentUnit?.longTexts?.otherLongText?.length
        "
      />

      <TextEditorCategory
        id="dissentingOpinion"
        v-bind="{ registerTextEditorRef }"
        v-model="dissentingOpinion"
        data-testid="dissentingOpinion"
        :editable="isInternalUser"
        label="Abweichende Meinung"
        :should-show-button="
          !store.documentUnit?.longTexts?.dissentingOpinion?.length
        "
      />

      <CategoryWrapper
        label="Mitwirkende Richter"
        :should-show-button="
          !store.documentUnit?.longTexts?.participatingJudges?.length
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
          :should-show-button="!store.documentUnit?.longTexts?.outline?.length"
        />
      </div>
    </div>
  </div>
</template>
