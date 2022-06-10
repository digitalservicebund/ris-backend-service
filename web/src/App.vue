<script lang="ts" setup>
import { useDocUnitsStore } from "./store"

const store = useDocUnitsStore()
</script>

<template>
  <v-app>
    <v-app-bar color="white">
      <v-container>
        <v-layout>
          <v-row>
            <v-col>
              <router-link :to="{ name: 'Rechtssprechung' }"
                >ZURÜCK</router-link
              >
            </v-col>
            <v-col>
              <router-link :to="{ name: 'Rechtssprechung' }"
                >RECHTSSPRECHUNG</router-link
              >
            </v-col>
          </v-row>
        </v-layout>
        <v-layout v-if="store.hasSelected()" style="background-color: #eee">
          <v-row>
            <v-col>
              Rechtssprechung: {{ store.getSelected()?.id }}, Aktenzeichen:
              {{ store.getSelected()?.aktenzeichen }}, Entscheidungsdatum:
              {{ store.getSelected()?.entscheidungsdatum }}, Gerichtstyp:
              {{ store.getSelected()?.gerichtstyp }}
            </v-col>
          </v-row>
        </v-layout>
      </v-container>
    </v-app-bar>
    <v-navigation-drawer
      v-if="store.hasSelected()"
      color="grey-lighten-2"
      permanent
    >
      <router-link
        :to="{
          name: 'Rubriken',
          params: { id: store.getSelected()?.id },
        }"
      >
        RUBRIKEN
      </router-link>
      <ul>
        <li>- Stammdaten</li>
        <li>- Langtexte</li>
      </ul>
      <router-link
        :to="{
          name: 'Dokumente',
          params: { id: store.getSelected()?.id },
        }"
        >DOKUMENTE</router-link
      >
      <br />
      BEARBEITUNGSSTAND
      <br />
      <br />
      <router-link to="/docx">docx -> html</router-link>
    </v-navigation-drawer>
    <v-main>
      <router-view></router-view>
    </v-main>
  </v-app>
</template>

<style lang="scss">
body {
  font-size: $font-size-root;
  font-family: $font-main;
}
a {
  color: black;
}
</style>
