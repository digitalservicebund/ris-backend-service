<script lang="ts" setup>
import { computed } from "vue"
import type { RouteLocationRaw, RouteRecordName } from "vue-router"
import { useRoute } from "vue-router"
import IconArrowBack from "~icons/ic/baseline-arrow-back"

interface Props {
  menuItems: LevelOneMenuItem[]
  goBackLabel: string
  goBackRoute: RouteLocationRaw
}

const props = defineProps<Props>()
const activeRoute = useRoute()

const enhancedActiveMenuItems = computed(() =>
  props.menuItems
    .filter((item) => !item.isDisabled)
    .map((levelOneItem) => ({
      ...levelOneItem,
      classes: getClassesForMenuItem(levelOneItem),
      isExpanded: checkIfLevelOneItemIsExpanded(levelOneItem),
      children: levelOneItem.children
        ?.filter((item) => !item.isDisabled)
        .map((levelTwoItem) => ({
          ...levelTwoItem,
          classes: getClassesForMenuItem(levelTwoItem),
          isActive: checkIfMenuItemIsActive(levelTwoItem),
        })),
    })),
)

function getClassesForMenuItem(
  menuItem: LevelOneMenuItem | LevelTwoMenuItem,
): Record<string, boolean> {
  const { isDisabled } = menuItem
  const isActive = checkIfMenuItemIsActive(menuItem)
  const isParentActive =
    "children" in menuItem &&
    checkIfLevelOneItemIsExpanded(menuItem as LevelOneMenuItem)

  return {
    disabled: isDisabled ?? false,
    "bg-blue-200": isActive,
    underline: isActive || isParentActive,
  }
}

function checkIfLevelOneItemIsExpanded(
  levelOneItem: LevelOneMenuItem,
): boolean {
  const activeChilds = levelOneItem.children?.filter((item) => {
    return checkIfMenuItemIsActive(item)
  })
  const anyChildIsActive = (activeChilds?.length ?? 0) > 0
  const isActiveItself = routesAreMatching(levelOneItem.route, activeRoute)
  return anyChildIsActive || isActiveItself
}

function checkIfMenuItemIsActive(
  menuItem: LevelOneMenuItem & LevelTwoMenuItem,
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
  routeTwo: RouteLocationRaw,
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

type ParsedLocation = { path?: string; name?: RouteRecordName; hash?: string }

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
  <aside aria-labelledby="sidebarNavigation" class="w-[16rem]">
    <span id="sidebarNavigation" hidden>SideBar Navigation</span>
    <router-link
      aria-labelledby="toOverviewButton"
      class="ds-link-01-bold flex h-80 items-center gap-12 border-b-1 border-gray-400 px-14 text-blue-800"
      :to="goBackRoute"
    >
      <IconArrowBack class="text-18" />
      <span id="toOverviewButton">{{ goBackLabel }}</span>
    </router-link>

    <div
      v-for="(levelOneItem, levelOneIndex) in enhancedActiveMenuItems"
      :key="levelOneItem.label"
      class="border-b-1 border-gray-400"
    >
      <router-link
        :aria-labelledby="`levelOneSideMenuItem` + levelOneIndex"
        class="ds-label-02-bold block py-[1.25rem] pl-[1rem] hover:bg-blue-200 hover:underline focus:bg-blue-200 focus:underline"
        :class="levelOneItem.classes"
        :to="levelOneItem.route"
      >
        <span :id="`levelOneSideMenuItem` + levelOneIndex">{{
          levelOneItem.label
        }}</span>
      </router-link>

      <div v-show="levelOneItem.isExpanded">
        <router-link
          v-for="(levelTwoItem, levelTwoIndex) in levelOneItem.children"
          :key="levelTwoItem.label"
          :aria-labelledby="`levelTwoSideMenuItem` + levelTwoIndex"
          class="ds-label-02-reg block py-[1rem] pl-[2rem] hover:bg-blue-200 hover:underline focus:bg-blue-200 focus:underline"
          :class="[
            levelTwoItem.classes,
            levelTwoItem.isActive ? 'underline' : '',
          ]"
          :to="levelTwoItem.route"
        >
          <span :id="`levelTwoSideMenuItem` + levelTwoIndex">{{
            levelTwoItem.label
          }}</span>
        </router-link>
      </div>
    </div>
  </aside>
</template>
