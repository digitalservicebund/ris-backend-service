<script lang="ts" setup>
import { ref, onMounted } from "vue"
import { useRoute } from "vue-router"
import Logo from "@/assets/neuRIS-logo.svg"
import FeatureToggleService from "@/services/featureToggleService"
import useSessionStore from "@/stores/sessionStore"
import IconPermIdentity from "~icons/ic/baseline-perm-identity"

const route = useRoute()
const session = useSessionStore()
const fontColor = ref<string>()

onMounted(async () => {
  const featureToggle = (
    await FeatureToggleService.isEnabled("neuris.environment-test")
  ).data
  if (featureToggle) {
    fontColor.value = "green"
  } else {
    fontColor.value = "black"
  }
})
</script>

<template>
  <nav
    class="flex items-center justify-between border-y border-gray-400 px-16 py-24 print:hidden"
  >
    <div class="flex items-center gap-44">
      <div class="flex items-center">
        <img alt="Neuris Logo" :src="Logo" />
        <span class="px-[1rem] text-16 leading-20">
          <span
            aria-hidden="true"
            class="font-bold"
            :style="{ color: fontColor }"
          >
            Rechtsinformationen</span
          >
          <br />
          <span aria-hidden="true">des Bundes</span>
        </span>
      </div>

      <router-link
        class="p-8 hover:bg-yellow-500 hover:underline"
        :class="{
          underline:
            route.path.includes('caselaw') &&
            !route.path.includes('procedures'),
        }"
        :to="{ name: 'caselaw' }"
        >Rechtsprechung
      </router-link>
      <router-link
        class="p-8 hover:bg-yellow-500 hover:underline"
        :class="{ underline: route.path.includes('procedures') }"
        :to="{ name: 'caselaw-procedures' }"
        >Vorgänge
      </router-link>
    </div>

    <div v-if="session.user" class="grid grid-cols-[auto,1fr] gap-10">
      <IconPermIdentity />
      <div>
        <div class="ds-label-01-bold">
          <router-link :to="{ name: 'settings' }">{{
            session.user.name
          }}</router-link>
        </div>
        <div v-if="session.user.documentationOffice" class="ds-label-03-reg">
          {{ session.user.documentationOffice.abbreviation }}
        </div>
      </div>
    </div>
  </nav>
</template>
