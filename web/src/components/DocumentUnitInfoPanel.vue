<script lang="ts" setup>
import DocumentUnit from "../domain/documentUnit"

defineProps<{ documentUnit: DocumentUnit }>()
</script>

<template>
  <v-container fluid class="panel-default">
    <v-row class="doc-info-row">
      <v-col class="vert-centered panel-id" sm="6" md="3">
        {{ documentUnit.documentnumber || " - " }}
      </v-col>
      <v-col class="vert-centered" sm="6" md="3">
        Aktenzeichen
        <span class="panel-default__values">
          {{ documentUnit.fileNumber || " - " }}</span
        >
      </v-col>
      <v-col class="vert-centered" sm="6" md="3">
        Entscheidungsdatum
        <span class="panel-default__values">
          {{ documentUnit.decisionDate || " - " }}
        </span>
      </v-col>
      <v-col class="vert-centered" sm="6" md="3">
        Gerichtstyp
        <span class="panel-default__values">{{
          documentUnit.courtType || " - "
        }}</span>
      </v-col>
    </v-row>
  </v-container>
</template>

<style lang="scss" scoped>
@import "@/styles/variables";

.panel-default {
  background-color: $navbar-hover-gray;
  border-bottom: $gray400 solid 1px;

  &__values {
    padding-left: 6px;
    font-weight: bold;
  }
}

.panel-id {
  color: $black;
  font-size: x-large;
}

.vert-centered {
  display: flex;
  align-items: center;
}
</style>
