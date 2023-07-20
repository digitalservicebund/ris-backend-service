<script lang="ts" setup>
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"
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
  <KitchensinkPage name="Navbar">
    <KitchensinkStory name="Top">
      <Navbar></Navbar>
    </KitchensinkStory>

    <KitchensinkStory name="Side">
      <NavbarSide
        go-back-label="Zurück"
        :go-back-route="goBackRoute"
        :menu-items="menuItems"
      />
    </KitchensinkStory>
  </KitchensinkPage>
</template>
