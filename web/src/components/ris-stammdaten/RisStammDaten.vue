<script lang="ts" setup></script>

<template>
  <v-form>
    <v-container>
      <v-row><h3>Stammdaten</h3></v-row>
      <v-row>
        <v-col cols="12" md="4">
          <v-text-field
            name="Vorgang"
            label="Vorgang"
            aria-labelledby="Vorgang"
          ></v-text-field>
        </v-col>
        <v-col cols="12" md="4">
          <v-text-field
            name="Entscheidungsdatum"
            label="Entscheidungsdatum"
            aria-labelledby="Entscheidungsdatum"
          ></v-text-field>
        </v-col>
      </v-row>
      <v-row>
        <v-col cols="12" md="4">
          <v-text-field
            name="Gerichtssitz"
            label="Gerichtssitz"
            aria-labelledby="Gerichtssitz"
          ></v-text-field>
        </v-col>
        <v-col cols="12" md="4">
          <v-text-field
            name="Gerichtstyp"
            label="Gerichtstyp"
            aria-labelledby="Gerichtstyp"
          ></v-text-field>
        </v-col>
      </v-row>
      <v-row>
        <v-col cols="12" md="4">
          <v-text-field
            name="Aktenzeichen"
            label="Aktenzeichen"
            aria-labelledby="Aktenzeichen"
          ></v-text-field>
        </v-col>
        <v-col cols="12" md="4">
          <v-text-field
            name="Spruchkörper"
            label="Spruchkörper"
            aria-labelledby="Spruchkörper"
          ></v-text-field>
        </v-col>
      </v-row>
      <v-row>
        <v-col cols="12" md="4">
          <v-text-field
            name="Eingangsart"
            label="Eingangsart"
            aria-labelledby="Eingangsart"
          ></v-text-field>
        </v-col>
        <v-col cols="12" md="4">
          <v-text-field
            name="ECLI-Nummer"
            label="ECLI-Nummer"
            aria-labelledby="ECLI-Nummer"
          ></v-text-field>
        </v-col>
      </v-row>
      <v-row>
        <v-col cols="12" md="4">
          <v-text-field
            name="Rechtskraft"
            label="Rechtskraft"
            aria-labelledby="ECLI-Nummer"
          ></v-text-field>
        </v-col>
      </v-row>
    </v-container>
  </v-form>
</template>

<style lang="scss"></style>
