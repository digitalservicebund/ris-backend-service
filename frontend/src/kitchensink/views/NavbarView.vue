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
  <div class="flex flex-col gap-y-20 items-start">
    <h1 class="font-bold text-32">Navbar Top</h1>
    <div class="min-w-full">
      <Navbar></Navbar>
    </div>
    <h1 class="font-bold text-32">Navbar Side</h1>
    <div class="min-h-[200px] pb-4">
      <NavbarSide
        go-back-label="Zurück"
        :go-back-route="goBackRoute"
        :menu-items="menuItems"
      />
    </div>
  </div>
</template>
