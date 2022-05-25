<template>
  <v-app>
    <v-app-bar color="blue800"></v-app-bar>
    <v-navigation-drawer color="grey-lighten-2" permanent>
      <router-link to="/">Home</router-link>
      <router-link to="/stammdaten">Stammdaten</router-link>
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
