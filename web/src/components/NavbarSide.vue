<script lang="ts" setup>
import { useRouter, useRoute } from "vue-router"

defineProps<{ documentNumber: string; visible: boolean }>()
defineEmits<{ (e: "toggleNavbar"): void }>()
const router = useRouter()
const route = useRoute()

const linkStyling = (componentName: string) => ({
  underline: router.currentRoute.value.name === componentName,
})
</script>

<template>
  <div v-if="visible" class="flex flex-col w-240">
    <div
      class="border-gray-400 border-solid border-y flex h-80 items-center justify-between pl-3"
    >
      <router-link
        class="flex gap-2 items-center link-01-bold"
        :to="{ name: 'jurisdiction' }"
      >
        <span class="material-icons">arrow_back</span>
        <span>ZURÜCK</span>
      </router-link>

      <button
        aria-label="Navigation schließen"
        class="-mr-20 bg-blue-800 rounded-full text-white z-10"
        @click="$emit('toggleNavbar')"
      >
        <span class="material-icons p-8">close</span>
      </button>
    </div>

    <div class="flex flex-col">
      <router-link
        class="font-bold hover:bg-blue-200 hover:underline pl-3 py-4"
        :class="
          linkStyling('jurisdiction-documentUnit-:documentNumber-categories')
        "
        :to="{
          name: 'jurisdiction-documentUnit-:documentNumber-categories',
          params: { documentNumber: documentNumber },
          query: route.query,
        }"
      >
        Rubriken
      </router-link>

      <router-link
        class="hover:bg-blue-200 hover:underline pl-5 py-1"
        :to="{
          name: 'jurisdiction-documentUnit-:documentNumber-categories',
          params: { documentNumber: documentNumber },
          query: route.query,
          hash: '#coreData',
        }"
        >Stammdaten</router-link
      >
      <router-link
        class="hover:bg-blue-200 hover:underline pl-5 py-1"
        :to="{
          name: 'jurisdiction-documentUnit-:documentNumber-categories',
          params: { documentNumber: documentNumber },
          query: route.query,
          hash: '#previousDecisions',
        }"
        >Rechtszug</router-link
      >
      <router-link
        class="hover:bg-blue-200 hover:underline pl-5 py-1"
        :to="{
          name: 'jurisdiction-documentUnit-:documentNumber-categories',
          params: { documentNumber: documentNumber },
          query: route.query,
          hash: '#texts',
        }"
        >Kurz- & Langtexte</router-link
      >
      <router-link
        class="border-b border-gray-400 font-bold hover:bg-blue-200 hover:underline pl-3 py-4"
        :class="linkStyling('jurisdiction-documentUnit-:documentNumber-files')"
        :to="{
          name: 'jurisdiction-documentUnit-:documentNumber-files',
          params: { documentNumber: documentNumber },
          query: route.query,
        }"
        >Dokumente</router-link
      >
      <div
        class="border-b border-gray-400 font-bold hover:bg-blue-200 hover:underline pl-3 py-4"
      >
        Bearbeitungsstand
      </div>

      <router-link
        class="font-bold hover:bg-blue-200 hover:underline pl-3 py-4"
        :to="{
          name: 'jurisdiction-documentUnit-:documentNumber-publication',
          params: { documentNumber: documentNumber },
        }"
        >Veröffentlichen</router-link
      >
    </div>
  </div>

  <button
    v-else
    aria-label="Navigation öffnen"
    class="bg-yellow-500 border-3 border-blue-800 border-solid sidebar-open"
    @click="$emit('toggleNavbar')"
  >
    <div class="sidebar-open-text">Menü</div>
    <div class="bg-blue-800 sidebar-open-icon-background">
      <span class="material-icons sidebar-open-icon"> arrow_forward_ios </span>
    </div>
  </button>
</template>

<style lang="scss" scoped>
.side-navbar-active-link {
  text-decoration: underline;
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
</style>
