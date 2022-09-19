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
  <v-container class="topbar-main" fluid role="navigation" tag="nav">
    <v-row>
      <v-col cols="1">
        <span class="material-icons text-blue-800 topbar-logo-circle"
          >circle</span
        >
      </v-col>
      <v-col
        class="border-gray-400 border-r-1 border-solid label-01-bold vertically-centered"
        cols="2"
      >
        Rechtsinformationssystem<br />
        des Bundes [Platzhalter]
      </v-col>
      <v-col class="topbar-link-box" cols="2">
        <router-link
          class="hover:bg-yellow-500 topbar-link"
          :class="linkStyling('jurisdiction')"
          :to="{ name: 'jurisdiction' }"
          >Rechtsprechung</router-link
        >
      </v-col>
      <v-col cols="5"></v-col>
      <v-col class="vertically-centered" cols="2">
        <span class="material-icons topbar-user-icon"> perm_identity </span>
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
}

.topbar-logo-circle {
  float: right;
  font-size: 56px;
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
