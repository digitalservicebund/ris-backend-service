<script setup lang="ts">
import { useRoute } from "vue-router"
import FlexContainer from "@/components/FlexContainer.vue"
import FlexItem from "@/components/FlexItem.vue"
import MenuItem from "@/domain/menuItem"
import StringsUtil from "@/utils/stringsUtil"

interface Props {
  menuItems: MenuItem[]
  isChild?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  isChild: false,
})

const currentRoute = useRoute()

const isChildActive = (currentNavItem: MenuItem) => {
  return currentNavItem.route.hash === currentRoute.hash
}

const isParentActive = (currentNavItem: MenuItem) => {
  if (currentNavItem.route.name == undefined || currentRoute.name == undefined)
    return false

  return currentNavItem.route.name == currentRoute.name
}

const isOnlyParentActive = (currentNavItem: MenuItem) => {
  if (
    !(
      StringsUtil.isEmpty(currentNavItem.route.hash) &&
      StringsUtil.isEmpty(currentRoute.hash)
    )
  ) {
    return (
      isParentActive(currentNavItem) &&
      currentNavItem.route.hash == currentRoute.hash
    )
  }
  return isParentActive(currentNavItem)
}
</script>

<template>
  <FlexContainer
    aria-labelledby="sidebar navigation"
    class="w-[16rem]"
    flex-direction="flex-col"
  >
    <div
      v-for="navItem in props.menuItems"
      :key="navItem.label"
      :class="!props.isChild ? 'border-b-1 border-gray-400' : ''"
    >
      <RouterLink
        v-if="!navItem.isDisabled"
        id="menu-item"
        :aria-label="`${navItem.label}`"
        class="w-full hover:bg-blue-200 hover:underline focus:bg-blue-200 focus:underline"
        :data-testid="navItem.route.name"
        :to="{
          ...navItem.route,
          query: {
            ...currentRoute.query,
          },
        }"
      >
        <div v-if="!props.isChild">
          <FlexItem
            id="parent-menu-item"
            class="ris-label2-bold flex min-h-64 justify-center px-24 py-12"
            :class="isOnlyParentActive(navItem) ? 'bg-blue-200' : ''"
            :data-testid="navItem.label"
          >
            <div :class="isParentActive(navItem) ? 'underline' : ''">
              {{ navItem.label }}
            </div>
          </FlexItem>
        </div>
        <div v-else>
          <FlexItem
            v-if="isParentActive(navItem)"
            id="child-menu-item"
            class="ris-label2-regular block py-[1rem] pl-[2rem]"
            :class="isChildActive(navItem) ? 'bg-blue-200 underline' : ''"
            :data-testid="navItem.label"
          >
            {{ navItem.label }}
          </FlexItem>
        </div>
      </RouterLink>
      <NavbarSide
        v-if="navItem.children"
        is-child
        :menu-items="navItem.children"
      />
    </div>
  </FlexContainer>
</template>
