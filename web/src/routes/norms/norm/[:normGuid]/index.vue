<script lang="ts" setup>
import { ref, toRefs, watch } from "vue"
import { useRoute, useRouter } from "vue-router"
import NavbarSide from "@/components/NavbarSide.vue"
import SideToggle from "@/components/SideToggle.vue"
import { useNormMenuItems } from "@/composables/useNormMenuItems"
import { useToggleStateInRouteQuery } from "@/composables/useToggleStateInRouteQuery"
import { Norm } from "@/domain/Norm"
import { getNormByGuid } from "@/services/normsService"

const props = defineProps<{ normGuid: string }>()

const norm = ref<Norm | undefined>()
async function loadNormByGuid() {
  norm.value = (await getNormByGuid(props.normGuid)).data
}
watch(() => props.normGuid, loadNormByGuid, { immediate: true })

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

    <div v-if="norm" class="bg-gray-100 p-64 pt-[3.5rem] w-full">
      <div class="max-w-screen-md">
        <h1 class="heading-02-regular mb-44">
          {{ norm.longTitle }}
        </h1>
        <div v-for="article in norm.articles" :key="article.guid">
          <h2 class="heading-04-regular mb-24">
            {{ article.marker }} {{ article.title }}
          </h2>
          <div
            v-for="paragraph in article.paragraphs"
            :key="paragraph.guid"
            class="mb-24"
          >
            <p>{{ paragraph.marker }} {{ paragraph.text }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
