<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { toRefs, watchEffect, onUnmounted } from "vue"
import { useRoute, useRouter } from "vue-router"
import NavbarSide from "@/components/NavbarSide.vue"
import SideToggle from "@/components/SideToggle.vue"
import { useNormMenuItems } from "@/composables/useNormMenuItems"
import { useToggleStateInRouteQuery } from "@/composables/useToggleStateInRouteQuery"
import { useLoadedNormStore } from "@/stores/loadedNorm"

const props = defineProps<{ normGuid: string }>()

const route = useRoute()
const router = useRouter()
const { normGuid } = toRefs(props)
const menuItems = useNormMenuItems(normGuid, route)
const goBackRoute = { name: "norms" }
const navigationIsOpen = useToggleStateInRouteQuery(
  "showNavBar",
  route,
  router.replace
)
const store = useLoadedNormStore()
const { loadedNorm } = storeToRefs(store)

watchEffect(() => store.load(props.normGuid))
onUnmounted(() => (loadedNorm.value = undefined))
</script>
<template>
  <div class="flex grow w-screen">
    <SideToggle v-model:is-expanded="navigationIsOpen" label="Navigation">
      <NavbarSide
        go-back-label="Zur Übersicht"
        :go-back-route="goBackRoute"
        :menu-items="menuItems"
      />
    </SideToggle>

    <div class="bg-gray-100 border-gray-400 border-l-1 p-48 w-full">
      <router-view v-if="loadedNorm" />
      <span v-else>Lade Norm...</span>
    </div>
  </div>
</template>
