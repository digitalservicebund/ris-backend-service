<script lang="ts" setup>
import { computed } from "vue"
import { useRouter } from "vue-router"
import NavbarSide, {
  LevelOneMenuItem,
} from "@/shared/components/NavbarSide.vue"

const router = useRouter()

const navigation = computed<LevelOneMenuItem[]>(() =>
  router
    .getRoutes()
    .filter(({ name }) => name?.toString().startsWith("kitchensink-index-"))
    .map<LevelOneMenuItem>((route) => {
      const label = route.name
        ?.toString()
        .replace("kitchensink-index-", "")
        .replace(/(^\w|[A-Z])/g, (match) => ` ${match.toUpperCase()}`)
        .trim()

      return {
        label: label ?? "Unnamed component",
        route: { name: route.name?.toString() as string },
      }
    })
    .sort((a, b) => (a.label < b.label ? -1 : 1)),
)
</script>

<template>
  <div class="flex bg-gray-100">
    <NavbarSide
      class="flex-none border-r border-gray-400 bg-white"
      go-back-label="Zur Anwendung"
      :go-back-route="{ name: 'index' }"
      :menu-items="navigation"
    />

    <main class="w-full flex-1 p-48">
      <RouterView />
    </main>
  </div>
</template>
