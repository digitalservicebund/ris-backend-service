<script lang="ts" setup>
import { useRoute } from "vue-router"
import NavbarSide, { MenuItem } from "@/components/NavbarSide.vue"
import Navbar from "@/components/NavbarTop.vue"

const route = useRoute()
const goBackRoute = { name: "kitchensink" }
const menuItems: MenuItem[] = [
  {
    name: "Top Item",
    route,
    children: [
      {
        name: "Child Item",
        route: { path: "foo" },
      },
      {
        name: "Another Child",
        route: { path: "bar" },
      },
    ],
  },
  {
    name: "Disabled Item",
    route: { path: "baz" },
    isDisabled: true,
  },
  {
    name: "Last Item",
    route: { path: "baz" },
  },
]
</script>

<template>
  <div class="flex flex-col gap-y-20 items-start">
    <h1 class="font-bold text-32">Navbar Top</h1>
    <div class="min-w-full">
      <Navbar></Navbar>
    </div>
    <h1 class="font-bold text-32">Navbar Side</h1>
    <div class="min-h-[200px] pb-4">
      <NavbarSide
        go-back-label="ZURÜCK"
        :go-back-route="goBackRoute"
        :menu-items="menuItems"
      />
    </div>
  </div>
</template>
