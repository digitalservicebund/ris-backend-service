<script lang="ts" setup></script>

<template>
  <v-form>
    <v-container>
      <v-row><h3>Stammdaten</h3></v-row>
      <v-row>
        <v-col cols="12" md="4">
          <v-text-field label="Vorgang"></v-text-field>
        </v-col>
        <v-col cols="12" md="4">
          <v-text-field label="Entscheidungsdatum"></v-text-field>
        </v-col>
      </v-row>
      <v-row>
        <v-col cols="12" md="4">
          <v-text-field label="Gerichtssitz"></v-text-field>
        </v-col>
        <v-col cols="12" md="4">
          <v-text-field label="Gerichtstyp"></v-text-field>
        </v-col>
      </v-row>
      <v-row>
        <v-col cols="12" md="4">
          <v-text-field label="Aktenzeichen"></v-text-field>
        </v-col>
        <v-col cols="12" md="4">
          <v-text-field label="Spruchkörper"></v-text-field>
        </v-col>
      </v-row>
      <v-row>
        <v-col cols="12" md="4">
          <v-text-field label="Eingangsart"></v-text-field>
        </v-col>
        <v-col cols="12" md="4">
          <v-text-field label="ECLI-Nummer"></v-text-field>
        </v-col>
      </v-row>
      <v-row>
        <v-col cols="12" md="4">
          <v-text-field label="Rechtskraft"></v-text-field>
        </v-col>
      </v-row>
    </v-container>
  </v-form>
</template>

<style lang="scss"></style>
