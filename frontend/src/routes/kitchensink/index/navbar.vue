<script lang="ts" setup>
import NavbarSide from "@/components/NavbarSide.vue"
import Navbar from "@/components/NavbarTop.vue"
import MenuItem from "@/domain/menuItem"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"

const menuItems: MenuItem[] = [
  {
    label: "Kitchensink",
    route: {
      name: "/kitchensink",
      hash: "#navbarTop",
    },
    children: [
      {
        label: "Navbar Top",
        route: { name: "/kitchensink", hash: "#navbarTop" },
      },
      {
        label: "Navbar Side",
        route: { name: "/kitchensink", hash: "#navbarSide" },
      },
    ],
  },
  {
    label: "Disabled Item",
    route: {
      name: "/caselaw",
    },
    isDisabled: true,
  },
  {
    label: "Last Item",
    route: {
      name: "/kitchensink",
      hash: "#nothing",
    },
    children: [
      { label: "Foo", route: { name: "/kitchensink", hash: "#foo" } },
      { label: "Bar", route: { name: "/kitchensink", hash: "#bar" } },
    ],
  },
]
</script>

<template>
  <KitchensinkPage name="Navbar">
    <KitchensinkStory name="Top">
      <Navbar></Navbar>
    </KitchensinkStory>

    <KitchensinkStory name="Side">
      <NavbarSide :menu-items="menuItems" />
    </KitchensinkStory>
  </KitchensinkPage>
</template>
