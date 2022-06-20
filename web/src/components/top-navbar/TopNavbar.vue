<script lang="ts" setup>
import bmjLogo from "../../assets/BMJ_Logo.png"
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
      <v-col cols="2"> </v-col>
      <v-col cols="2"></v-col>
      <v-col cols="2" class="topbar_headline">
        <router-link :to="{ name: 'Rechtsprechung' }"
          >Rechtsprechung</router-link
        >
      </v-col>
      <v-col cols="4"></v-col>
      <v-col cols="2">
        <v-icon> perm_identity </v-icon>
        User Name
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
.topbar_headline {
  font-weight: bold;
  text-decoration: none;
  &:hover {
    text-decoration: underline;
  }
}
</style>
