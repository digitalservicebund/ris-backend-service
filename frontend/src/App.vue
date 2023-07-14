<script lang="ts" setup>
import Navbar from "./shared/components/NavbarTop.vue"
</script>

<template>
  <div class="flex min-h-screen flex-col">
    <Navbar />

    <Suspense>
      <router-view class="grow" role="main" />
    </Suspense>
  </div>
</template>
