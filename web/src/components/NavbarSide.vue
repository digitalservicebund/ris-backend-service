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
        aria-label="Navigation schließen"
        class="bg-blue-800 sidebar-close-icon-background"
        @click="$emit('toggleNavbar')"
        @keydown.m="$emit('toggleNavbar')"
      >
        <span class="material-icons sidebar-close-icon"> close </span>
      </div>
      <v-container fluid>
        <v-row>
          <v-col class="back-button">
            <span class="flex">
              <span class="back-button__icon material-icons text-blue-800">
                arrow_back
              </span>
              <router-link
                class="back-button__link text-blue-800"
                :to="{ name: 'jurisdiction' }"
                >ZURÜCK</router-link
              >
            </span>
          </v-col>
        </v-row>
        <v-divider />
        <v-row><v-col></v-col></v-row>
        <v-row>
          <v-col class="hover:bg-blue-200 sidebar-headline">
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
          <v-col class="hover:bg-blue-200 sub-rubriken">
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
          <v-col class="hover:bg-blue-200 sub-rubriken">
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
          <v-col class="hover:bg-blue-200 sub-rubriken">
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
          <v-col class="hover:bg-blue-200 sidebar-headline">
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
          <v-col class="hover:bg-blue-200 sidebar-headline">
            Bearbeitungsstand
          </v-col>
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
          <v-col class="hover:bg-blue-200">
            <router-link
              class="public-button sidebar-headline"
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
      aria-label="Navigation öffnen"
      class="bg-yellow-500 border-3 border-blue-800 border-solid sidebar-open"
      @click="$emit('toggleNavbar')"
      @keydown.c="$emit('toggleNavbar')"
    >
      <div class="sidebar-open-text">Menü</div>
      <div class="bg-blue-800 sidebar-open-icon-background">
        <span class="material-icons sidebar-open-icon">
          arrow_forward_ios
        </span>
      </div>
    </div>
  </v-col>
</template>

<style lang="scss" scoped>
.sidebar-headline {
  font-weight: bold;

  &:hover {
    text-decoration: underline;
  }
}

.sub-rubriken {
  margin-left: 20px;

  &:hover {
    text-decoration: underline;
  }
}

.side-navbar-active-link {
  text-decoration: underline;
}

.sidebar-close-icon-background {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  float: right;
  transform: translateY(60px);
}

.sidebar-close-icon {
  margin-top: 8px;
  margin-left: 8px;
  color: white;
}

.sidebar-open {
  display: flex;
  width: 100px;
  height: 65px;
  align-items: center; // align vertical
  justify-content: center; // align horizontal
  margin-left: 6px;
  border-radius: 10px;
  transform: rotate(-90deg) translateX(-165px);
  transform-origin: left;
}

.sidebar-open-text {
  margin-left: 40px;
}

.sidebar-open-icon-background {
  min-width: 40px;
  height: 40px;
  border-radius: 50%;
  transform: rotate(90deg) translateX(3px) translateY(-10px);
}

.sidebar-open-icon {
  margin-top: 8px;
  margin-left: 9px;
  color: white;
}

.back-button {
  padding-bottom: 49px;

  &__icon {
    position: relative;
    margin-right: 8px;
    margin-bottom: 4px;
  }
}

.public-button {
  text-decoration: underline;
}
</style>
