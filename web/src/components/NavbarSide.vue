<script lang="ts" setup>
import { useRouter, useRoute } from "vue-router"

defineProps<{ documentNumber: string; visible: boolean }>()
defineEmits<{ (e: "toggleNavbar"): void }>()
const router = useRouter()
const route = useRoute()

const linkStyling = (componentName: string) => {
  return router.currentRoute.value.name === componentName
    ? "side-navbar-active-link"
    : ""
}
</script>

<template>
  <v-col :cols="visible ? 2 : 1">
    <span v-if="visible">
      <div
        id="sidebar-close-button"
        class="sidebar-close-icon-background"
        aria-label="Navigation schließen"
        @click="$emit('toggleNavbar')"
        @keydown.m="$emit('toggleNavbar')"
      >
        <v-icon class="sidebar-close-icon"> close </v-icon>
      </div>
      <v-container fluid>
        <v-row>
          <v-col class="back-button">
            <span>
              <v-icon class="back-button__icon" size="22px">
                arrow_back
              </v-icon>
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
              :class="
                linkStyling('jurisdiction-docUnit-:documentNumber-categories')
              "
              :to="{
                name: 'jurisdiction-docUnit-:documentNumber-categories',
                params: { documentNumber: documentNumber },
                query: route.query,
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
                name: 'jurisdiction-docUnit-:documentNumber-categories',
                params: { documentNumber: documentNumber },
                query: route.query,
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
                name: 'jurisdiction-docUnit-:documentNumber-categories',
                params: { documentNumber: documentNumber },
                query: route.query,
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
              :class="linkStyling('jurisdiction-docUnit-:documentNumber-files')"
              :to="{
                name: 'jurisdiction-docUnit-:documentNumber-files',
                params: { documentNumber: documentNumber },
                query: route.query,
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
    </span>
    <div
      v-else
      id="sidebar-open-button"
      class="sidebar-open"
      aria-label="Navigation öffnen"
      @click="$emit('toggleNavbar')"
      @keydown.c="$emit('toggleNavbar')"
    >
      <div class="sidebar-open-text">Menü</div>
      <div class="sidebar-open-icon-background">
        <v-icon class="sidebar-open-icon"> arrow_forward_ios </v-icon>
      </div>
    </div>
  </v-col>
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
