<script lang="ts" setup>
import { useDocUnitsStore } from "../../store"

const store = useDocUnitsStore()
</script>

<template>
  <v-container v-if="store.hasSelected()">
    <v-row>
      <v-col>
        Rechtsprechung: {{ store.getSelected()?.id }}, Aktenzeichen:
        {{ store.getSelected()?.aktenzeichen }}, Entscheidungsdatum:
        {{ store.getSelected()?.entscheidungsdatum }}, Gerichtstyp:
        {{ store.getSelected()?.gerichtstyp }}
      </v-col>
    </v-row>
  </v-container>
</template>

<style lang="scss"></style>
