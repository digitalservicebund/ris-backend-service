<script setup lang="ts">
import PreviewLeftCell from "@/components/preview/PreviewLeftCell.vue"
import PreviewRightCell from "@/components/preview/PreviewRightCell.vue"
import TableView from "@/components/TableView.vue"
import { ContentRelatedIndexing } from "@/domain/documentUnit"

defineProps<{
  contentRelatedIndexing: ContentRelatedIndexing
}>()
</script>

<template>
  <div class="mx-16 my-16 h-2 w-5/6 bg-blue-600" />
  <TableView class="table w-full table-fixed">
    <tr
      v-if="
        contentRelatedIndexing.keywords &&
        contentRelatedIndexing.keywords?.length > 0
      "
    >
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
    <tr
      v-if="
        contentRelatedIndexing.fieldsOfLaw &&
        contentRelatedIndexing.fieldsOfLaw?.length > 0
      "
    >
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

    <tr
      v-if="
        contentRelatedIndexing.norms && contentRelatedIndexing.norms?.length > 0
      "
    >
      <PreviewLeftCell> Normen </PreviewLeftCell>
      <PreviewRightCell>
        <div v-for="(norm, index) in contentRelatedIndexing.norms" :key="index">
          <div v-for="(singleNorm, i) in norm.singleNorms" :key="i">
            {{ norm.renderDecision }} - {{ singleNorm.renderDecision }}
          </div>
        </div>
      </PreviewRightCell>
    </tr>

    <tr
      v-if="
        contentRelatedIndexing.activeCitations &&
        contentRelatedIndexing.activeCitations?.length > 0
      "
    >
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
