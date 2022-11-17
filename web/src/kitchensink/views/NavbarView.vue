<script lang="ts" setup>
import NavbarSide, { LevelOneMenuItem } from "@/components/NavbarSide.vue"
import Navbar from "@/components/NavbarTop.vue"

const goBackRoute = { name: "kitchensink" }
const menuItems: LevelOneMenuItem[] = [
  {
    label: "Top Item",
    route: "/kitchensink",
    children: [
      {
        label: "Child Item",
        route: { path: "/kitchensink", hash: "#navbarTop" },
      },
      {
        label: "Child Item",
        route: { path: "/kitchensink", hash: "#navbarSide" },
      },
    ],
  },
  {
    label: "Disabled Item",
    route: "/caselaw",
    isDisabled: true,
  },
  {
    label: "Last Item",
    route: "/norms",
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
