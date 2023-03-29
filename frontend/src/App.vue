<script lang="ts" setup>
import Navbar from "./shared/components/NavbarTop.vue"
</script>

<template>
  <div class="flex flex-col min-h-screen">
    <Navbar />

    <Suspense>
      <router-view class="grow" role="main" />
    </Suspense>
  </div>
</template>
