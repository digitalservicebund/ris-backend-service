<script lang="ts" setup>
import { useSeoMeta } from "@unhead/vue"
import Navbar from "./components/NavbarTop.vue"

useSeoMeta({
  charset: "utf-8",
  ogTitle: "NeuRIS Rechtsinformationssystem",
  ogImage: "/favicon.svg",
  ogLocale: "de",
})
</script>

<template>
  <div class="flex min-h-screen flex-col">
    <Navbar />

    <Suspense>
      <main class="flex grow">
        <router-view class="grow" />
      </main>
    </Suspense>
  </div>
</template>
