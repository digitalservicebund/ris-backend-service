<script lang="ts" setup>
import { ref, computed } from "vue"
import { useRoute } from "vue-router"
import FlexContainer from "@/components/FlexContainer.vue"
import FlexItem from "@/components/FlexItem.vue"
import IconBadge from "@/components/IconBadge.vue"
import useSessionStore from "@/stores/sessionStore"
import IconPermIdentity from "~icons/ic/baseline-perm-identity"

const route = useRoute()
const session = useSessionStore()
const fontColor = ref<string>()

const badge = computed(() => {
  const docOffice = session.user?.documentationOffice
    ? session.user.documentationOffice.abbreviation
    : undefined
  if (session.env == "staging") {
    return {
      color: "bg-red-300",
      label: docOffice ? `${docOffice} | Staging` : "",
    }
  } else if (session.env == "uat") {
    return {
      color: "bg-yellow-300",
      label: docOffice ? `${docOffice} | UAT` : "",
    }
  } else {
    return {
      color: "bg-blue-300",
      label: docOffice ?? "",
    }
  }
})
</script>

<template>
  <nav
    class="flex items-center justify-between border-y border-gray-400 px-16 py-16 print:hidden"
  >
    <div class="flex items-center gap-44">
      <div class="flex flex-col">
        <span
          aria-hidden="true"
          class="ris-body1-bold"
          :style="{ color: fontColor }"
        >
          Rechtsinformationen</span
        >

        <span aria-hidden="true" class="leading-none text-gray-900"
          >des Bundes</span
        >
      </div>

      <router-link
        class="ris-label1-regular p-8 hover:bg-yellow-500 hover:underline"
        :class="{
          underline:
            route.path === '/caselaw' ||
            route.path.includes('caselaw/documentunit'),
        }"
        data-testid="search-navbar-button"
        :to="{ name: 'caselaw-search' }"
        >Suche
      </router-link>
      <router-link
        class="ris-label1-regular p-8 hover:bg-yellow-500 hover:underline"
        :class="{
          underline: route.path.includes('inbox'),
        }"
        data-testid="inbox-navbar-button"
        :to="{ name: 'caselaw-inbox' }"
        >Eingang
      </router-link>
      <router-link
        class="ris-label1-regular p-8 hover:bg-yellow-500 hover:underline"
        :class="{ underline: route.path.includes('procedures') }"
        :to="{ name: 'caselaw-procedures' }"
        >Vorgänge
      </router-link>
      <router-link
        class="ris-label1-regular p-8 hover:bg-yellow-500 hover:underline"
        :class="{
          underline: route.path.includes('periodical-evaluation'),
        }"
        :to="{ name: 'caselaw-periodical-evaluation' }"
        >Periodika
      </router-link>
    </div>

    <div v-if="session.user" class="flex gap-10">
      <IconPermIdentity />
      <div>
        <router-link :to="{ name: 'settings' }">
          <FlexContainer class="gap-10">
            <FlexItem>{{ session.user?.name }}</FlexItem>
            <FlexItem>
              <IconBadge
                v-if="session.user.documentationOffice"
                :background-color="badge.color"
                :label="badge.label"
              />
            </FlexItem>
          </FlexContainer>
        </router-link>
      </div>
    </div>
  </nav>
</template>
