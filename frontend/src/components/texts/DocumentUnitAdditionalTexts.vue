<script lang="ts" setup>
import { computed, ref } from "vue"
import CategoryWrapper from "@/components/CategoryWrapper.vue"
import ParticipatingJudges from "@/components/ParticipatingJudges.vue"
import TextEditorCategory from "@/components/texts/TextEditorCategory.vue"
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

const hasParticipatingJudges = ref<boolean>(
  store.documentUnit?.longTexts?.participatingJudges
    ? store.documentUnit?.longTexts?.participatingJudges?.length > 0
    : false,
)
</script>

<template>
  <div aria-label="Weitere Langtexte">
    <h2 class="ds-label-01-bold mb-16">Weitere Langtexte</h2>
    <div class="flex flex-col gap-24">
      <TextEditorCategory
        id="otherLongText"
        v-model="otherLongText"
        :editable="isInternalUser"
        label="Sonstiger Langtext"
        :should-show-button="!hasOtherLongtext"
      />

      <TextEditorCategory
        id="dissentingOpinion"
        v-model="dissentingOpinion"
        :editable="isInternalUser"
        label="Abweichende Meinung"
        :should-show-button="!hasDissentingOpinion"
      />

      <CategoryWrapper
        label="Mitwirkende Richter"
        :should-show-button="!hasParticipatingJudges"
      >
        <ParticipatingJudges label="Mitwirkende Richter" />
      </CategoryWrapper>

      <div class="gap-0">
        <TextEditorCategory
          id="outline"
          v-model="outline"
          :editable="isInternalUser"
          label="Gliederung"
          :should-show-button="!hasOutline"
        />
      </div>
    </div>
  </div>
</template>
