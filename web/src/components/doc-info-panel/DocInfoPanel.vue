<script lang="ts" setup>
import { useDocUnitsStore } from "../../store"
import { DocUnit } from "../../types/DocUnit"

const store = useDocUnitsStore()

const getValueOrFallback = (field: keyof DocUnit) => {
  return store.getSelectedSafe()[field] ? store.getSelectedSafe()[field] : " - "
}
</script>

<template>
  <v-container v-if="store.hasSelected()" fluid class="panel-default">
    <v-row class="doc-info-row">
      <v-col class="vert-centered panel-id" cols="3">
        {{ getValueOrFallback("id") }}
      </v-col>
      <v-col class="vert-centered" cols="3">
        Aktenzeichen
        <span class="panel-default__values">{{
          getValueOrFallback("aktenzeichen")
        }}</span>
      </v-col>
      <v-col class="vert-centered" cols="3">
        Entscheidungsdatum
        <span class="panel-default__values">{{
          getValueOrFallback("entscheidungsdatum")
        }}</span>
      </v-col>
      <v-col class="vert-centered" cols="3">
        Gerichtstyp
        <span class="panel-default__values">{{
          getValueOrFallback("gerichtstyp")
        }}</span>
      </v-col>
    </v-row>
  </v-container>
</template>

<style lang="scss">
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
  font-size: xx-large;
}
.vert-centered {
  display: flex;
  align-items: center;
}
</style>
