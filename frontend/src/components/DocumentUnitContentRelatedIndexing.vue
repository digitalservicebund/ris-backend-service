<script setup lang="ts">
import { computed } from "vue"
import ActiveCitations from "@/components/ActiveCitations.vue"
import FieldOfLawMain from "@/components/FieldOfLawMain.vue"
import KeyWords from "@/components/KeyWords.vue"
import Norms from "@/components/NormReferences.vue"
import { ContentRelatedIndexing } from "@/domain/documentUnit"

const props = defineProps<{
  modelValue: ContentRelatedIndexing
}>()

const emit = defineEmits<{
  "update:modelValue": [value?: ContentRelatedIndexing]
}>()

const contentRelatedIndexing = computed({
  get: () => {
    return props.modelValue
  },
  set: (value) => {
    if (value) emit("update:modelValue", value)
  },
})
</script>

<template>
  <div class="mb-32 flex flex-col gap-32">
    <h2 class="ds-heading-02-reg mb-[1rem]">Inhaltliche Erschließung</h2>
    <KeyWords v-model="contentRelatedIndexing.keywords" />
    <FieldOfLawMain v-model="contentRelatedIndexing.fieldsOfLaw" />
    <Norms v-model="contentRelatedIndexing.norms" />
    <ActiveCitations v-model="contentRelatedIndexing.activeCitations" />
  </div>
</template>
