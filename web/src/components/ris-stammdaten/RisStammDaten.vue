<script lang="ts" setup></script>

<template>
  <v-form>
    <v-container>
      <v-row><h3>Stammdaten</h3></v-row>
      <v-row>
        <v-col cols="12" md="4">
          <div>
            <label for="Vorgang">
              <input
                id="Vorgang"
                type="text"
                placeholder="Vorgang"
                name="Vorgang"
              />
            </label>
          </div>
        </v-col>
        <v-col cols="12" md="4">
          <div>
            <label for="Entscheidungsdatum">
              <input
                id="Entscheidungsdatum"
                type="text"
                placeholder="Entscheidungsdatum"
                name="Entscheidungsdatum"
              />
            </label>
          </div>
        </v-col>
      </v-row>
      <v-row>
        <v-col cols="12" md="4">
          <div>
            <label for="Gerichtssitz">
              <input
                id="Gerichtssitz"
                type="text"
                placeholder="Gerichtssitz"
                name="Gerichtssitz"
              />
            </label>
          </div>
        </v-col>
        <v-col cols="12" md="4">
          <div>
            <label for="Gerichtstyp">
              <input
                id="Gerichtstyp"
                type="text"
                placeholder="Gerichtstyp"
                name="Gerichtstyp"
              />
            </label>
          </div>
        </v-col>
      </v-row>
      <v-row>
        <v-col cols="12" md="4">
          <div>
            <label for="Aktenzeichen">
              <input
                id="Aktenzeichen"
                type="text"
                placeholder="Aktenzeichen"
                name="Aktenzeichen"
              />
            </label>
          </div>
        </v-col>
        <v-col cols="12" md="4">
          <div>
            <label for="Spruchkörper">
              <input
                id="Spruchkörper"
                type="text"
                placeholder="Spruchkörper"
                name="Spruchkörper"
              />
            </label>
          </div>
        </v-col>
      </v-row>
      <v-row>
        <v-col cols="12" md="4">
          <div>
            <label for="Eingangsart">
              <input
                id="Eingangsart"
                type="text"
                placeholder="Eingangsart"
                name="Eingangsart"
              />
            </label>
          </div>
        </v-col>
        <v-col cols="12" md="4">
          <div>
            <label for="ECLI-Nummer">
              <input
                id="ECLI-Nummer"
                type="text"
                placeholder="ECLI-Nummer"
                name="ECLI-Nummer"
              />
            </label>
          </div>
        </v-col>
      </v-row>
      <v-row>
        <v-col cols="12" md="4">
          <div>
            <label for="Rechtskraft">
              <input
                id="Rechtskraft"
                type="text"
                placeholder="Rechtskraft"
                name="Rechtskraft"
              />
            </label>
          </div>
        </v-col>
      </v-row>
    </v-container>
  </v-form>
</template>

<style lang="scss"></style>
