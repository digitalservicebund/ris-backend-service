<script lang="ts" setup>
import { computed } from "vue"
import type { RouteLocationRaw } from "vue-router"

interface Props {
  menuItems: LevelOneMenuItem[]
  goBackLabel: string
  goBackRoute: RouteLocationRaw
}

const props = defineProps<Props>()

const enhencedMenuItems = computed(() =>
  props.menuItems.map((levelOneItem) => ({
    ...levelOneItem,
    classes: getClassesForLevelOne(levelOneItem),
    children: levelOneItem.children?.map((levelTwoItem) => ({
      ...levelTwoItem,
      classes: getClassesForLevelTwo(levelTwoItem, levelOneItem),
    })),
  }))
)

function getClassesForLevelOne(
  levelOneItem: LevelOneMenuItem
): Record<string, boolean> {
  const { isDisabled, children } = levelOneItem
  const hasChildren = children !== undefined && children.length > 0
  return {
    disabled: isDisabled ?? false,
    "mb-0": hasChildren,
  }
}

function getClassesForLevelTwo(
  levelTwoItem: LevelTwoMenuItem,
  levelOneItem: LevelOneMenuItem
): Record<string, boolean> {
  const { isDisabled } = levelTwoItem
  const siblings = levelOneItem.children ?? []
  const lastSibling = siblings[siblings.length - 1]
  const isLastSibling = siblings.length > 0 && levelTwoItem == lastSibling

  return {
    disabled: isDisabled ?? false,
    "mb-24": isLastSibling,
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

    <div
      v-for="levelOneItem in enhencedMenuItems"
      :key="levelOneItem.label"
      class="border-gray-400 border-t-2"
    >
      <router-link
        active-class="bg-blue-200"
        class="block focus:bg-blue-200 focus:underline hover:bg-blue-200 hover:underline label-02-bold my-10 px-8 py-12"
        :class="levelOneItem.classes"
        exact-path
        :to="levelOneItem.route"
      >
        {{ levelOneItem.label }}
      </router-link>

      <router-link
        v-for="levelTwoItem in levelOneItem.children"
        :key="levelTwoItem.label"
        class="block focus:bg-blue-200 focus:underline hover:bg-blue-200 hover:underline label-02-reg p-10 pl-16"
        :class="levelTwoItem.classes"
        :to="levelTwoItem.route"
      >
        {{ levelTwoItem.label }}
      </router-link>
    </div>
  </div>
</template>
