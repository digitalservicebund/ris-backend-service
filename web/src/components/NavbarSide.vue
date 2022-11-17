<script lang="ts" setup>
import type { RouteLocationRaw } from "vue-router"

interface Props {
  menuItems: LevelOneMenuItem[]
  goBackLabel: string
  goBackRoute: RouteLocationRaw
}

defineProps<Props>()
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
      v-for="levelOneItem in menuItems"
      :key="levelOneItem.label"
      class="border-gray-400 border-t-2"
    >
      <router-link
        active-class="bg-blue-200"
        class="block focus:bg-blue-200 focus:underline hover:bg-blue-200 hover:underline label-02-bold my-10 px-8 py-12"
        :class="{
          disabled: levelOneItem.isDisabled,
          'mb-0': levelOneItem.children,
        }"
        exact-path
        :to="levelOneItem.route"
      >
        {{ levelOneItem.label }}
      </router-link>

      <router-link
        v-for="(levelTwoItem, index) in levelOneItem.children"
        :key="levelTwoItem.label"
        class="block focus:bg-blue-200 focus:underline hover:bg-blue-200 hover:underline label-02-reg p-10 pl-16"
        :class="{ 'mb-24': index + 1 == levelOneItem.children?.length }"
        :to="levelTwoItem.route"
      >
        {{ levelTwoItem.label }}
      </router-link>
    </div>
  </div>
</template>
