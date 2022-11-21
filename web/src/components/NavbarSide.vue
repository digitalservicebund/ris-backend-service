<script lang="ts" setup>
import { computed } from "vue"
import { useRoute } from "vue-router"
import type { RouteLocationRaw } from "vue-router"

interface Props {
  menuItems: LevelOneMenuItem[]
  goBackLabel: string
  goBackRoute: RouteLocationRaw
}

const props = defineProps<Props>()
const activeRoute = useRoute()

const enhencedMenuItems = computed(() =>
  props.menuItems.map((levelOneItem) => ({
    ...levelOneItem,
    classes: getClassesForMenuItem(levelOneItem),
    isExpanded: checkIfLevelOneItemIsExpanded(levelOneItem),
    children: levelOneItem.children?.map((levelTwoItem) => ({
      ...levelTwoItem,
      classes: getClassesForMenuItem(levelTwoItem),
    })),
  }))
)

function getClassesForMenuItem(
  menuItem: LevelOneMenuItem | LevelTwoMenuItem
): Record<string, boolean> {
  const { isDisabled } = menuItem
  const isActive = checkIfMenuItemIsActive(menuItem)
  return {
    disabled: isDisabled ?? false,
    "bg-blue-200": isActive,
  }
}

function checkIfLevelOneItemIsExpanded(
  levelOneItem: LevelOneMenuItem
): boolean {
  const activeChilds = levelOneItem.children?.filter((item) => {
    return checkIfMenuItemIsActive(item)
  })
  const anyChildIsActive = (activeChilds?.length ?? 0) > 0
  const isActiveItself = routesAreMatching(levelOneItem.route, activeRoute)
  return anyChildIsActive || isActiveItself
}

function checkIfMenuItemIsActive(
  menuItem: LevelOneMenuItem & LevelTwoMenuItem
): boolean {
  const activeChilds = menuItem.children?.filter((item) => {
    return checkIfMenuItemIsActive(item)
  })
  const anyChildIsActive = (activeChilds?.length ?? 0) > 0
  const isActiveItself = routesAreMatching(menuItem.route, activeRoute)
  return !anyChildIsActive && isActiveItself
}

/**
 * Compare to routes to check if the first one partially matches the second
 * route. First of all, it checks if the route paths or names are matching. If
 * that is the case and if the first route also defines a hash, the hash gets
 * matched to. If the first one should not have a hash, but the second, this
 * still counts as a (partial) match.
 * Note that due to how routes work, we can make some assumption. For example it
 * can't happen that (for the unlikely case that both are given) the names match,
 * but not the paths.
 * The queries of both routes are not (yet) considered for matching at all.
 */
function routesAreMatching(
  routeOne: RouteLocationRaw,
  routeTwo: RouteLocationRaw
): boolean {
  const parsedRouteOne = parseRouteLocation(routeOne)
  const parsedRouteTwo = parseRouteLocation(routeTwo)

  const canMatchName = parsedRouteOne.name !== undefined
  const nameIsMatching = parsedRouteOne.name == parsedRouteTwo.name

  const canMatchPath = parsedRouteOne.path !== undefined
  const pathIsMatching = parsedRouteOne.path == parsedRouteTwo.path

  const canMatchHash = parsedRouteOne.hash !== undefined
  const hashIsMatching = parsedRouteOne.hash == parsedRouteTwo.hash

  // Should be always true due to the Vue Router types.
  const matchingIsPossible = canMatchPath || canMatchName

  return (
    matchingIsPossible &&
    (!canMatchName || nameIsMatching) &&
    (!canMatchPath || pathIsMatching) &&
    (!canMatchHash || hashIsMatching)
  )
}

type ParsedLocation = { path?: string; name?: string; hash?: string }

function parseRouteLocation(route: RouteLocationRaw): ParsedLocation {
  if (typeof route === "string") {
    const routeAsUrl = new URL(route, "https://fake.necessary")
    return { path: routeAsUrl.pathname, hash: routeAsUrl.hash }
  } else {
    return {
      path: "path" in route ? route.path : undefined,
      name: "name" in route ? route.name : undefined,
      hash: "hash" in route ? route.hash : undefined,
    }
  }
}
</script>

<script lang="ts">
export interface LevelOneMenuItem {
  label: string
  route: RouteLocationRaw
  isDisabled?: boolean
  children?: LevelTwoMenuItem[]
}

export interface LevelTwoMenuItem {
  label: string
  route: RouteLocationRaw
  isDisabled?: boolean
}
</script>

<template>
  <div class="px-14 w-[24rem]">
    <router-link
      class="flex gap-12 h-80 items-center link-01-bold text-blue-800"
      :to="goBackRoute"
    >
      <span class="material-icons">arrow_back</span>
      <span>{{ goBackLabel }}</span>
    </router-link>

    <hr class="border border-gray-400 mb-10" />

    <div v-for="levelOneItem in enhencedMenuItems" :key="levelOneItem.label">
      <router-link
        class="block focus:bg-blue-200 focus:underline hover:bg-blue-200 hover:underline label-02-bold px-8 py-12"
        :class="levelOneItem.classes"
        :to="levelOneItem.route"
      >
        {{ levelOneItem.label }}
      </router-link>

      <div v-show="levelOneItem.isExpanded">
        <router-link
          v-for="levelTwoItem in levelOneItem.children"
          :key="levelTwoItem.label"
          class="block focus:bg-blue-200 focus:underline hover:bg-blue-200 hover:underline label-02-reg p-12 pl-24"
          :class="levelTwoItem.classes"
          :to="levelTwoItem.route"
        >
          {{ levelTwoItem.label }}
        </router-link>
      </div>

      <hr class="border border-gray-400 my-10" />
    </div>
  </div>
</template>
