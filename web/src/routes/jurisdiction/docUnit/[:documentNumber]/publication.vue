<script lang="ts" setup>
import { ref } from "vue"
import DocUnitDetail from "./index.vue"
import PublicationDocument from "@/components/PublicationDocument.vue"
import docUnitService from "@/services/docUnitService"

const props = defineProps<{
  documentNumber: string
}>()
const docUnit = ref(
  await docUnitService.getByDocumentNumber(props.documentNumber)
)
</script>

<template>
  <DocUnitDetail :doc-unit="docUnit">
    <v-container>
      <v-row>
        <v-col><h2>Veröffentlichen</h2></v-col>
      </v-row>
    </v-container>
    <PublicationDocument />
  </DocUnitDetail>
</template>
