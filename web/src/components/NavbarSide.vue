<script lang="ts" setup>
import { useRouter, useRoute } from "vue-router"
import SideToggle from "@/components/SideToggle.vue"

defineProps<{ documentNumber: string; visible: boolean }>()
defineEmits<{ (e: "toggleNavbar"): void }>()
const router = useRouter()
const route = useRoute()

const linkStyling = (componentName: string) => ({
  underline: router.currentRoute.value.name === componentName,
})
</script>

<template>
  <SideToggle
    from-side="left"
    :is-expanded="visible"
    label="Navigation"
    @toggle="$emit('toggleNavbar')"
  >
    <div v-if="visible" class="flex flex-col w-[24rem]">
      <div
        class="border-b border-gray-400 border-solid flex h-80 items-center justify-between pl-3"
      >
        <router-link
          class="flex gap-12 items-center link-01-bold px-[1.3rem] py-[0.44rem] text-blue-800"
          :to="{ name: 'caselaw' }"
        >
          <span class="material-icons">arrow_back</span>
          <span>ZURÜCK</span>
        </router-link>
      </div>

      <div class="flex flex-col">
        <router-link
          class="font-bold hover:bg-blue-200 hover:underline px-[1.3rem] py-[0.44rem]"
          :class="
            linkStyling('caselaw-documentUnit-:documentNumber-categories')
          "
          :to="{
            name: 'caselaw-documentUnit-:documentNumber-categories',
            params: { documentNumber: documentNumber },
            query: route.query,
          }"
        >
          Rubriken
        </router-link>

        <router-link
          class="hover:bg-blue-200 hover:underline px-[2.667rem] py-[0.33rem]"
          :to="{
            name: 'caselaw-documentUnit-:documentNumber-categories',
            params: { documentNumber: documentNumber },
            query: route.query,
            hash: '#coreData',
          }"
          >Stammdaten</router-link
        >
        <router-link
          class="hover:bg-blue-200 hover:underline px-[2.667rem] py-[0.33rem]"
          :to="{
            name: 'caselaw-documentUnit-:documentNumber-categories',
            params: { documentNumber: documentNumber },
            query: route.query,
            hash: '#previousDecisions',
          }"
          >Rechtszug</router-link
        >
        <router-link
          class="hover:bg-blue-200 hover:underline px-[2.667rem] py-[0.33rem]"
          :to="{
            name: 'caselaw-documentUnit-:documentNumber-categories',
            params: { documentNumber: documentNumber },
            query: route.query,
            hash: '#texts',
          }"
          >Kurz- & Langtexte</router-link
        >
        <router-link
          class="border-b border-gray-400 font-bold hover:bg-blue-200 hover:underline px-[1.3rem] py-[0.44rem]"
          :class="linkStyling('caselaw-documentUnit-:documentNumber-files')"
          :to="{
            name: 'caselaw-documentUnit-:documentNumber-files',
            params: { documentNumber: documentNumber },
            query: route.query,
          }"
          >Dokumente</router-link
        >
        <div
          class="border-b border-gray-400 font-bold hover:bg-blue-200 hover:underline px-[1.3rem] py-[0.44rem]"
        >
          Bearbeitungsstand
        </div>

        <router-link
          class="font-bold hover:bg-blue-200 hover:underline px-[1.3rem] py-[0.44rem]"
          :to="{
            name: 'caselaw-documentUnit-:documentNumber-publication',
            params: { documentNumber: documentNumber },
          }"
          >Veröffentlichen</router-link
        >
      </div>
    </div>
  </SideToggle>
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
  border-radius: 10px;
  margin-left: 6px;
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
