<script lang="ts" setup>
import NavbarSide, {
  LevelOneMenuItem,
} from "@/shared/components/NavbarSide.vue"
import Navbar from "@/shared/components/NavbarTop.vue"

const goBackRoute = { name: "kitchensink" }
const menuItems: LevelOneMenuItem[] = [
  {
    label: "Kitchensink",
    route: "/kitchensink",
    children: [
      { label: "Navbar Top", route: "/kitchensink#navbarTop" },
      { label: "Navbar Side", route: "/kitchensink#navbarSide" },
    ],
  },
  {
    label: "Disabled Item",
    route: "/caselaw",
    isDisabled: true,
  },
  {
    label: "Last Item",
    route: "/kitchensink#nothing",
    children: [
      { label: "Foo", route: "/kitchensink#foo" },
      { label: "Bar", route: "/kitchensink#bar" },
    ],
  },
]
</script>

<template>
  <div class="flex flex-col items-start gap-y-20">
    <h1 class="text-32 font-bold">Navbar Top</h1>
    <div class="min-w-full">
      <Navbar></Navbar>
    </div>
    <h1 class="text-32 font-bold">Navbar Side</h1>
    <div class="min-h-[200px] pb-4">
      <NavbarSide
        go-back-label="Zurück"
        :go-back-route="goBackRoute"
        :menu-items="menuItems"
      />
    </div>
  </div>
</template>
