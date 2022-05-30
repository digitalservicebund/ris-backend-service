<script lang="ts" setup>
import RisButton from "../ris-button/RisButton.vue"

const stammDatenList = [
  {
    id: "Vorgang",
    name: "Vorgang",
    label: "Vorgang",
    aria: "Vorgang",
  },
  {
    id: "Entscheidungsdatum",
    name: "Entscheidungsdatum",
    label: "Entscheidungsdatum",
    aria: "Entscheidungsdatum",
  },
  {
    id: "Gerichtssitz",
    name: "Gerichtssitz",
    label: "Gerichtssitz",
    aria: "Gerichtssitz",
  },
  {
    id: "Gerichtstyp",
    name: "Gerichtstyp",
    label: "Gerichtstyp",
    aria: "Gerichtstyp",
  },
  {
    id: "Aktenzeichen",
    name: "Aktenzeichen",
    label: "Aktenzeichen",
    aria: "Aktenzeichen",
  },
  {
    id: "Spruchkörper",
    name: "Spruchkörper",
    label: "Spruchkörper",
    aria: "Spruchkörper",
  },
  {
    id: "Eingangsart",
    name: "Eingangsart",
    label: "Eingangsart",
    aria: "Eingangsart",
  },
  {
    id: "ECLI-Nummer",
    name: "ECLI-Nummer",
    label: "ECLI-Nummer",
    aria: "ECLI-Nummer",
  },
  {
    id: "Rechtskraft",
    name: "Rechtskraft",
    label: "Rechtskraft",
    aria: "Rechtskraft",
  },
]

const safeData = () => {
  alert("Daten gespeichert")
}
</script>

<template>
  <v-form>
    <v-container>
      <v-row><h3>Stammdaten</h3></v-row>
      <v-row>
        <v-col cols="12" md="6">
          <template v-for="(item, index) in stammDatenList">
            <v-text-field
              v-if="index <= 4"
              :key="item.id"
              :name="item.name"
              :label="item.label"
              :aria-labelledby="item.aria"
            ></v-text-field>
          </template>
        </v-col>
        <v-col cols="12" md="6">
          <template v-for="(item, index) in stammDatenList">
            <v-text-field
              v-if="index > 4"
              :key="item.id"
              :name="item.name"
              :label="item.label"
              :aria-labelledby="item.aria"
            ></v-text-field>
          </template>
          <ris-button color="blue800" @click="safeData" />
        </v-col>
      </v-row>
    </v-container>
  </v-form>
</template>

<style lang="scss"></style>
