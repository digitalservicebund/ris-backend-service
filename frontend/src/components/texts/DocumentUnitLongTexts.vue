<script lang="ts" setup>
import { computed, ref } from "vue"
import CategoryWrapper from "@/components/CategoryWrapper.vue"
import DocumentUnitTextField from "@/components/texts/DocumentUnitTextField.vue"
import { useInternalUser } from "@/composables/useInternalUser"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const store = useDocumentUnitStore()

const isInternalUser = useInternalUser()

const hasTenor = ref<boolean>(
  store.documentUnit?.longTexts?.tenor
    ? store.documentUnit?.longTexts?.tenor?.length > 0
    : false,
)

const hasReasons = ref<boolean>(
  store.documentUnit?.longTexts?.reasons
    ? store.documentUnit?.longTexts?.reasons?.length > 0
    : false,
)

const hasCaseFacts = ref<boolean>(
  store.documentUnit?.longTexts?.caseFacts
    ? store.documentUnit?.longTexts?.caseFacts?.length > 0
    : false,
)

const hasDecisionReasons = ref<boolean>(
  store.documentUnit?.longTexts?.decisionReasons
    ? store.documentUnit?.longTexts?.decisionReasons?.length > 0
    : false,
)

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

const tenor = computed({
  get: () => store.documentUnit?.longTexts.tenor,
  set: (newValue) => {
    store.documentUnit!.longTexts.tenor = hasContent(newValue)
      ? newValue
      : undefined
  },
})

const reasons = computed({
  get: () => store.documentUnit?.longTexts.reasons,
  set: (newValue) => {
    store.documentUnit!.longTexts.reasons = hasContent(newValue)
      ? newValue
      : undefined
  },
})

const caseFacts = computed({
  get: () => store.documentUnit?.longTexts.caseFacts,
  set: (newValue) => {
    store.documentUnit!.longTexts.caseFacts = hasContent(newValue)
      ? newValue
      : undefined
  },
})

const decisionReasons = computed({
  get: () => store.documentUnit?.longTexts.decisionReasons,
  set: (newValue) => {
    store.documentUnit!.longTexts.decisionReasons = hasContent(newValue)
      ? newValue
      : undefined
  },
})

const otherLongText = computed({
  get: () => store.documentUnit?.longTexts.otherLongText,
  set: (newValue) => {
    store.documentUnit!.longTexts.otherLongText = hasContent(newValue)
      ? newValue
      : undefined
  },
})

const dissentingOpinion = computed({
  get: () => store.documentUnit?.longTexts.dissentingOpinion,
  set: (newValue) => {
    store.documentUnit!.longTexts.dissentingOpinion = hasContent(newValue)
      ? newValue
      : undefined
  },
})

const outline = computed({
  get: () => store.documentUnit?.longTexts.outline,
  set: (newValue) => {
    store.documentUnit!.longTexts.outline = hasContent(newValue)
      ? newValue
      : undefined
  },
})

function hasContent(text?: string) {
  const divElem = document.createElement("div")
  if (text == undefined) text = ""
  divElem.innerHTML = text
  const hasImgElem = divElem.getElementsByTagName("img").length > 0
  const hasTable = divElem.getElementsByTagName("table").length > 0
  const hasInnerText = divElem.innerText.trimEnd().length > 0
  return hasInnerText || hasImgElem || hasTable
}
</script>

<template>
  <h2 class="ds-heading-03-bold mb-16 mt-24">Langtexte</h2>
  <div class="flex flex-col gap-24">
    <CategoryWrapper label="Tenor" :should-show-button="!hasTenor">
      <DocumentUnitTextField
        id="tenor"
        v-model="tenor"
        :editable="isInternalUser"
        label="Tenor"
      />
    </CategoryWrapper>

    <CategoryWrapper label="Gründe" :should-show-button="!hasReasons">
      <DocumentUnitTextField
        id="reasons"
        v-model="reasons"
        :editable="isInternalUser"
        label="Gründe"
      />
    </CategoryWrapper>

    <CategoryWrapper label="Tatbestand" :should-show-button="!hasCaseFacts">
      <DocumentUnitTextField
        id="caseFacts"
        v-model="caseFacts"
        :editable="isInternalUser"
        label="Tatbestand"
      />
    </CategoryWrapper>

    <CategoryWrapper
      label="Entscheidungsgründe"
      :should-show-button="!hasDecisionReasons"
    >
      <DocumentUnitTextField
        id="decisionReasons"
        v-model="decisionReasons"
        :editable="isInternalUser"
        label="Entscheidungsgründe"
      />
    </CategoryWrapper>

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
