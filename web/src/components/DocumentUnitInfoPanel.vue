<script lang="ts" setup>
import DocumentUnit from "../domain/documentUnit"

defineProps<{ documentUnit: DocumentUnit }>()
</script>

<template>
  <v-container class="panel-default" fluid>
    <v-row class="doc-info-row">
      <v-col class="panel-id vert-centered" md="3" sm="6">
        {{ documentUnit.documentnumber || " - " }}
      </v-col>
      <v-col class="vert-centered" md="3" sm="6">
        Aktenzeichen
        <span class="panel-default__values">
          {{ documentUnit.fileNumber || " - " }}</span
        >
      </v-col>
      <v-col class="vert-centered" md="3" sm="6">
        Entscheidungsdatum
        <span class="panel-default__values">
          {{ documentUnit.decisionDate || " - " }}
        </span>
      </v-col>
      <v-col class="vert-centered" md="3" sm="6">
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
  border-bottom: $gray400 solid 1px;
  background-color: $navbar-hover-gray;

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
