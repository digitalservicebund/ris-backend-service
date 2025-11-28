<script lang="ts" setup>
import { useHead, useSeoMeta } from "@unhead/vue"
import Toast from "primevue/toast"
import Navbar from "./components/NavbarTop.vue"

useHead({
  title: "NeuRIS Rechtsinformationssystem",
})

useSeoMeta({
  charset: "utf-8",
  ogTitle: "NeuRIS Rechtsinformationssystem",
  ogImage: "src/assets/favicon-production.svg",
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
    <Toast position="bottom-right" />
  </div>
</template>

<style>
.ProseMirror ol {
  list-style-type: decimal;
  padding-left: 1.5rem;
}

.ProseMirror ul {
  list-style-type: disc;
  padding-left: 1.5rem;
}

.ProseMirror li {
  margin-bottom: 0.25rem;
}
</style>
