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
          <v-col class="sidebar-headline">
            <router-link
              :class="
                linkStyling(
                  'jurisdiction-documentUnit-:documentNumber-categories'
                )
              "
              :to="{
                name: 'jurisdiction-documentUnit-:documentNumber-categories',
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
                name: 'jurisdiction-documentUnit-:documentNumber-categories',
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
                name: 'jurisdiction-documentUnit-:documentNumber-categories',
                params: { documentNumber: documentNumber },
                query: route.query,
                hash: '#previousDecisions',
              }"
              >Rechtszug</router-link
            >
          </v-col>
        </v-row>
        <v-row>
          <v-col class="sub-rubriken">
            <router-link
              :to="{
                name: 'jurisdiction-documentUnit-:documentNumber-categories',
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
          <v-col class="sidebar-headline">
            <router-link
              :class="
                linkStyling('jurisdiction-documentUnit-:documentNumber-files')
              "
              :to="{
                name: 'jurisdiction-documentUnit-:documentNumber-files',
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
          <v-col class="sidebar-headline"> Bearbeitungsstand </v-col>
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
              class="sidebar-headline public-button"
              :to="{
                name: 'jurisdiction-documentUnit-:documentNumber-publication',
                params: { documentNumber: documentNumber },
              }"
              >Veröffentlichen</router-link
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

<style lang="scss" scoped>
@import "@/styles/variables";

.sidebar-headline {
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

.sidebar-close-icon-background {
  background-color: $blue800;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  float: right;
  transform: translateY(60px);
}

.sidebar-close-icon {
  color: white;
  margin-left: 8px;
  margin-top: 8px;
}

.sidebar-open {
  background-color: $yellow500;
  border-radius: 10px;
  border: 3px solid $blue800;
  width: 100px;
  height: 65px;
  display: flex;
  justify-content: center; // align horizontal
  align-items: center; // align vertical
  margin-left: 6px;
  transform: rotate(-90deg) translateX(-165px);
  transform-origin: left;
}

.sidebar-open-text {
  margin-left: 40px;
}

.sidebar-open-icon-background {
  background-color: $blue800;
  border-radius: 50%;
  min-width: 40px;
  height: 40px;
  transform: rotate(90deg) translateX(3px) translateY(-10px);
}

.sidebar-open-icon {
  color: white;
  margin-left: 9px;
  margin-top: 8px;
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

.public-button {
  text-decoration: underline;
}
</style>
