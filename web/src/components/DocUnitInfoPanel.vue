<script lang="ts" setup>
import DocUnit from "../domain/DocUnit"

defineProps<{ docUnit: DocUnit }>()
</script>

<template>
  <v-container fluid class="panel-default">
    <v-row class="doc-info-row">
      <v-col class="vert-centered panel-id" cols="3">
        {{ docUnit.documentnumber || " - " }}
      </v-col>
      <v-col class="vert-centered" cols="3">
        Aktenzeichen
        <span class="panel-default__values">
          {{ docUnit.aktenzeichen || " - " }}</span
        >
      </v-col>
      <v-col class="vert-centered" cols="3">
        Entscheidungsdatum
        <span class="panel-default__values">
          {{ docUnit.entscheidungsdatum || " - " }}
        </span>
      </v-col>
      <v-col class="vert-centered" cols="3">
        Gerichtstyp
        <span class="panel-default__values">{{
          docUnit.gerichtstyp || " - "
        }}</span>
      </v-col>
    </v-row>
  </v-container>
</template>

<style lang="scss">
.panel-default {
  height: 65px; // is a fix value ok?
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
