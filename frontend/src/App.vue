<script lang="ts" setup>
import Navbar from "./components/NavbarTop.vue"
</script>

<template>
  <div class="flex min-h-screen flex-col">
    <Navbar />

    <Suspense>
      <main>
        <router-view class="grow" />
      </main>
    </Suspense>
  </div>
</template>
