<script lang="ts" setup></script>

<template>
  <v-form>
    <v-container>
      <v-row><h3>Stammdaten</h3></v-row>
      <v-row>
        <v-col cols="12" md="4">
          <v-text-field
            label="Vorgang"
            placeholder="Vorgang"
            variant="outlined"
            aria-labelledby="Vorgang"
          ></v-text-field>
        </v-col>
        <v-col cols="12" md="4">
          <div>
            <v-label for="Entscheidungsdatum">
              <v-input
                id="Entscheidungsdatum"
                type="text"
                placeholder="Entscheidungsdatum"
                name="Entscheidungsdatum"
              />
            </v-label>
          </div>
        </v-col>
      </v-row>
      <v-row>
        <v-col cols="12" md="4">
          <div>
            <v-label for="Gerichtssitz">
              <v-input
                id="Gerichtssitz"
                type="text"
                placeholder="Gerichtssitz"
                name="Gerichtssitz"
              />
            </v-label>
          </div>
        </v-col>
        <v-col cols="12" md="4">
          <div>
            <v-label for="Gerichtstyp">
              <v-input
                id="Gerichtstyp"
                type="text"
                placeholder="Gerichtstyp"
                name="Gerichtstyp"
              />
            </v-label>
          </div>
        </v-col>
      </v-row>
      <v-row>
        <v-col cols="12" md="4">
          <div>
            <v-label for="Aktenzeichen">
              <v-input
                id="Aktenzeichen"
                type="text"
                placeholder="Aktenzeichen"
                name="Aktenzeichen"
              />
            </v-label>
          </div>
        </v-col>
        <v-col cols="12" md="4">
          <div>
            <v-label for="Spruchkörper">
              <v-input
                id="Spruchkörper"
                type="text"
                placeholder="Spruchkörper"
                name="Spruchkörper"
              />
            </v-label>
          </div>
        </v-col>
      </v-row>
      <v-row>
        <v-col cols="12" md="4">
          <div>
            <v-label for="Eingangsart">
              <v-input
                id="Eingangsart"
                type="text"
                placeholder="Eingangsart"
                name="Eingangsart"
              />
            </v-label>
          </div>
        </v-col>
        <v-col cols="12" md="4">
          <div>
            <v-label for="ECLI-Nummer">
              <v-input
                id="ECLI-Nummer"
                type="text"
                placeholder="ECLI-Nummer"
                name="ECLI-Nummer"
              />
            </v-label>
          </div>
        </v-col>
      </v-row>
      <v-row>
        <v-col cols="12" md="4">
          <div>
            <v-label for="Rechtskraft">
              <v-input
                id="Rechtskraft"
                type="text"
                placeholder="Rechtskraft"
                name="Rechtskraft"
              />
            </v-label>
          </div>
        </v-col>
      </v-row>
    </v-container>
  </v-form>
</template>

<style lang="scss"></style>
