<script setup lang="ts">
import { computed } from "vue"
import PreviewLeftCell from "@/components/preview/PreviewLeftCell.vue"
import PreviewRightCell from "@/components/preview/PreviewRightCell.vue"
import TableView from "@/components/TableView.vue"
import { ContentRelatedIndexing } from "@/domain/documentUnit"

const props = defineProps<{
  contentRelatedIndexing: ContentRelatedIndexing
}>()

const hasKeywords = computed(() => {
  return (
    props.contentRelatedIndexing.keywords &&
    props.contentRelatedIndexing.keywords?.length > 0
  )
})

const hasFieldsOfLaw = computed(() => {
  return (
    props.contentRelatedIndexing.fieldsOfLaw &&
    props.contentRelatedIndexing.fieldsOfLaw?.length > 0
  )
})

const hasNorms = computed(() => {
  return (
    props.contentRelatedIndexing.norms &&
    props.contentRelatedIndexing.norms?.length > 0
  )
})

const hasActiveCitations = computed(() => {
  return (
    props.contentRelatedIndexing.activeCitations &&
    props.contentRelatedIndexing.activeCitations?.length > 0
  )
})
</script>

<template>
  <TableView class="table w-full table-fixed">
    <tr v-if="hasKeywords">
      <PreviewLeftCell> Schlagwörter </PreviewLeftCell>
      <PreviewRightCell>
        <div
          v-for="(keyword, index) in contentRelatedIndexing.keywords"
          :key="index"
        >
          {{ keyword }}
        </div>
      </PreviewRightCell>
    </tr>
    <tr v-if="hasFieldsOfLaw">
      <PreviewLeftCell> Sachgebiete </PreviewLeftCell>
      <PreviewRightCell>
        <div
          v-for="(fieldOfLaw, index) in contentRelatedIndexing.fieldsOfLaw"
          :key="index"
        >
          {{ fieldOfLaw.identifier }}, {{ fieldOfLaw.text }}
        </div>
      </PreviewRightCell>
    </tr>

    <tr v-if="hasNorms">
      <PreviewLeftCell> Normen </PreviewLeftCell>
      <PreviewRightCell>
        <div v-for="(norm, index) in contentRelatedIndexing.norms" :key="index">
          <div v-for="(singleNorm, i) in norm.singleNorms" :key="i">
            {{ norm.renderDecision }} - {{ singleNorm.renderDecision }}
          </div>
        </div>
      </PreviewRightCell>
    </tr>

    <tr v-if="hasActiveCitations">
      <PreviewLeftCell> Aktivzitierung </PreviewLeftCell>
      <PreviewRightCell>
        <div
          v-for="(
            activeCitation, index
          ) in contentRelatedIndexing.activeCitations"
          :key="index"
        >
          {{ activeCitation.renderDecision }}
        </div>
      </PreviewRightCell>
    </tr>
  </TableView>
</template>
