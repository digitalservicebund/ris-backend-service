<script lang="ts" setup>
import { useRouter } from "vue-router"

const router = useRouter()
const linkStyling = (componentName: string) => {
  return router.currentRoute.value.path.includes(componentName.toLowerCase())
    ? "topbar-link-active"
    : ""
}
</script>

<template>
  <v-container fluid class="topbar-main" role="navigation" tag="nav">
    <v-row>
      <v-col cols="1">
        <v-icon class="topbar-logo-circle" size="56px">circle</v-icon>
      </v-col>
      <v-col class="topbar-header vertically-centered" cols="2">
        Rechtsinformationssystem<br />
        des Bundes [Platzhalter]
      </v-col>
      <v-col cols="2" class="topbar-link-box">
        <router-link
          :class="linkStyling('jurisdiction')"
          class="topbar-link"
          :to="{ name: 'jurisdiction' }"
          >Rechtsprechung</router-link
        >
      </v-col>
      <v-col cols="5"></v-col>
      <v-col cols="2" class="vertically-centered">
        <v-icon class="topbar-user-icon"> perm_identity </v-icon>
        User Name
      </v-col>
    </v-row>
  </v-container>
</template>

<style lang="scss" scoped>
.vertically-centered {
  display: flex;
  align-items: center;
}

.topbar-header {
  font-weight: bold;
  line-height: 120%;
  border-right: $gray400 solid 1px;
}

.topbar-logo-circle {
  color: $blue800;
  float: right;
}

.topbar-link-box {
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

.topbar-link {
  padding: 10px;

  &:hover {
    background-color: $yellow500;
    text-decoration: underline;
  }
}

.topbar-link-active {
  text-decoration: underline !important;
}

.topbar-user-icon {
  margin-right: 8px;
}
</style>
