<script lang="ts" setup>
import { useRouter } from "vue-router"

defineProps<{ id: string }>()
const router = useRouter()

const linkStyling = (componentName: string) => {
  return router.currentRoute.value.name === componentName
    ? "side-navbar-active-link"
    : ""
}
</script>

<template>
  <v-container fluid>
    <v-row>
      <v-col class="back-button">
        <span>
          <v-icon class="back-button__icon" size="22px"> arrow_back </v-icon>
          <router-link class="back-button" :to="{ name: 'jurisdiction' }"
            >ZURÜCK</router-link
          >
        </span>
      </v-col>
    </v-row>
    <v-divider />
    <v-row><v-col></v-col></v-row>
    <v-row>
      <v-col class="sidebar_headline">
        <router-link
          :class="linkStyling('jurisdiction-docUnit-:id-categories')"
          :to="{
            name: 'jurisdiction-docUnit-:id-categories',
            params: { id: id },
          }"
        >
          Rubriken
        </router-link>
      </v-col>
    </v-row>
    <v-row>
      <v-col class="sub-rubriken">
        <router-link
          :to="{
            name: 'jurisdiction-docUnit-:id-categories',
            params: { id: id },
            hash: '#coreData',
          }"
          >Stammdaten</router-link
        >
      </v-col>
    </v-row>
    <v-row>
      <v-col class="sub-rubriken">
        <router-link
          :to="{
            name: 'jurisdiction-docUnit-:id-categories',
            params: { id: id },
            hash: '#texts',
          }"
          >Kurz- & Langtexte</router-link
        >
      </v-col>
    </v-row>
    <v-row>
      <v-col>
        <v-divider />
      </v-col>
    </v-row>
    <v-row>
      <v-col class="sidebar_headline"> Rechtszug </v-col>
    </v-row>
    <v-row>
      <v-col>
        <v-divider />
      </v-col>
    </v-row>
    <v-row>
      <v-col class="sidebar_headline">
        <router-link
          :class="linkStyling('jurisdiction-docUnit-:id-files')"
          :to="{
            name: 'jurisdiction-docUnit-:id-files',
            params: { id: id },
          }"
          >Dokumente</router-link
        >
      </v-col>
    </v-row>
    <v-row>
      <v-col>
        <v-divider />
      </v-col>
    </v-row>
    <v-row>
      <v-col class="sidebar_headline"> Bearbeitungsstand </v-col>
    </v-row>
    <v-row>
      <v-col>
        <v-divider />
      </v-col>
    </v-row>
    <v-row>
      <v-col> </v-col>
    </v-row>
    <v-row>
      <v-col>
        <router-link
          style="color: gray"
          :to="{
            name: 'docUnitDocx',
          }"
          >docx --> html</router-link
        >
      </v-col>
    </v-row>
  </v-container>
</template>

<style lang="scss">
.sidebar_headline {
  font-weight: bold;
  &:hover {
    background-color: $navbar-hover-gray;
    text-decoration: underline;
  }
}
.sub-rubriken {
  margin-left: 20px;
  &:hover {
    background-color: $navbar-hover-gray;
    text-decoration: underline;
  }
}
.side-navbar-active-link {
  text-decoration: underline;
}
.back-button {
  color: $blue800;
  padding-bottom: 49px;
  font-size: small;
  &__icon {
    margin-bottom: 4px;
    margin-right: 8px;
  }
}
</style>
