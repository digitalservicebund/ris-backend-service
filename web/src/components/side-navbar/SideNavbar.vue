<script lang="ts" setup>
import { useRouter } from "vue-router"
import { useDocUnitsStore } from "../../store"

const store = useDocUnitsStore()
const router = useRouter()

const linkStyling = (componentName: string) => {
  return router.currentRoute.value.name === componentName
    ? "side-navbar-active-link"
    : ""
}
</script>

<template>
  <v-container v-if="store.hasSelected()" fluid>
    <v-row>
      <v-col class="back-button">
        <span v-if="store.hasSelected()">
          <v-icon class="back-button__icon" size="22px"> arrow_back </v-icon>
          <router-link class="back-button" :to="{ name: 'Rechtsprechung' }"
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
          :class="linkStyling('Rubriken')"
          :to="{
            name: 'Rubriken',
            params: { id: store.getSelected()?.id },
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
            name: 'Rubriken',
            params: { id: store.getSelected()?.id },
            hash: '#stammdaten',
          }"
          >Stammdaten</router-link
        >
      </v-col>
    </v-row>
    <v-row>
      <v-col class="sub-rubriken">
        <router-link
          :to="{
            name: 'Rubriken',
            params: { id: store.getSelected()?.id },
            hash: '#kurzUndLangtexte',
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
          :class="linkStyling('Dokumente')"
          :to="{
            name: 'Dokumente',
            params: { id: store.getSelected()?.id },
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
        <router-link style="color: gray" to="/docx">docx --> html</router-link>
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
