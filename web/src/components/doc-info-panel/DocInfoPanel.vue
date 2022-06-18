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
    <v-row>
      <v-col cols="12">
        <v-icon> gavel </v-icon>
        <span>RECHTSPRECHUNG</span>
      </v-col>
    </v-row>
    <v-row>
      <v-col cols="2" class="panel-id">
        {{ getValueOrFallback("id") }}
      </v-col>
      <v-col cols="2">
        Aktenzeichen
        <span class="panel-default__values">{{
          getValueOrFallback("aktenzeichen")
        }}</span>
      </v-col>
      <v-col cols="2">
        Entscheidungsdatum
        <span class="panel-default__values">{{
          getValueOrFallback("entscheidungsdatum")
        }}</span>
      </v-col>
      <v-col cols="2">
        Gerichtstyp
        <span class="panel-default__values">{{
          getValueOrFallback("gerichtstyp")
        }}</span>
      </v-col>
      <v-col cols="4"> </v-col>
    </v-row>
  </v-container>
</template>

<style lang="scss">
.panel-default {
  color: $gray900;

  &__values {
    font-weight: bold;
  }
}
.panel-id {
  color: $black;
  font-size: xx-large;
}
</style>
