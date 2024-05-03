<script setup lang="ts">
import PreviewLeftCell from "@/components/preview/PreviewLeftCell.vue"
import PreviewRightCell from "@/components/preview/PreviewRightCell.vue"
import TableView from "@/components/TableView.vue"
import EnsuingDecision from "@/domain/ensuingDecision"
import PreviousDecision from "@/domain/previousDecision"

defineProps<{
  previousDecisions?: PreviousDecision[]
  ensuingDecisions?: EnsuingDecision[]
}>()
</script>

<template>
  <TableView class="table w-full table-fixed">
    <tr
      v-if="previousDecisions && previousDecisions?.length > 0"
      class="w-full"
    >
      <PreviewLeftCell>Vorinstanz</PreviewLeftCell>
      <PreviewRightCell>
        <div
          v-for="(previousDecision, index) in previousDecisions"
          :key="index"
        >
          {{ previousDecision.renderDecision }}
        </div>
      </PreviewRightCell>
    </tr>
    <tr v-if="ensuingDecisions && ensuingDecisions?.length > 0" class="w-full">
      <PreviewLeftCell>Nachgehende Entscheidungen</PreviewLeftCell>
      <PreviewRightCell>
        <div v-for="(ensuingDecision, index) in ensuingDecisions" :key="index">
          {{ ensuingDecision.renderDecision }}
        </div>
      </PreviewRightCell>
    </tr>
  </TableView>
</template>
