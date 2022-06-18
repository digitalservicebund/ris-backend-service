<script lang="ts" setup>
import bmjLogo from "../../assets/BMJ_Logo.png"
import { useDocUnitsStore } from "../../store"

const store = useDocUnitsStore()
</script>

<template>
  <!--<v-app-bar :flat=true :floating=false height="auto">-->
  <v-container fluid>
    <v-row>
      <v-col cols="1">
        <div class="ris_rectangle">Rechts informations system</div>
      </v-col>
      <v-col cols="9"></v-col>
      <v-col cols="2">
        <v-img :src="bmjLogo"></v-img>
      </v-col>
    </v-row>
    <v-divider />
    <v-row>
      <v-col cols="2">
        <span v-if="store.hasSelected()">
          <v-icon class="icon_topnavbar"> west </v-icon>
          <router-link :to="{ name: 'Rechtsprechung' }">ZURÜCK</router-link>
        </span>
      </v-col>
      <v-col cols="2"></v-col>
      <v-col cols="1">
        <router-link :to="{ name: 'Rechtsprechung' }"
          >RECHTSPRECHUNG</router-link
        >
      </v-col>
      <v-col cols="5"></v-col>
      <v-col cols="2">
        <v-icon> perm_identity </v-icon>
        USER NAME
      </v-col>
    </v-row>
    <v-divider />
  </v-container>
</template>

<style lang="scss">
.ris_rectangle {
  font-weight: bold;
  background: $yellow500;
  height: 120px;
  width: 120px;
  padding: 10px 0 0 10px;
  line-height: 120%;
}
.icon_topnavbar {
  color: $blue800;
}
</style>
