<script lang="ts" setup>
import type { RouteLocationRaw } from "vue-router"

interface Props {
  menuItems: MenuItem[]
  goBackLabel: string
  goBackRoute: RouteLocationRaw
}

defineProps<Props>()
</script>

<script lang="ts">
export interface ChildMenuItem {
  name: string
  route: RouteLocationRaw
  isDisabled?: boolean
}

export interface MenuItem extends ChildMenuItem {
  children?: ChildMenuItem[]
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
      v-for="menuItem in menuItems"
      :key="menuItem.name"
      class="border-gray-400 border-t-2"
    >
      <router-link
        active-class="bg-blue-200"
        class="block hover:bg-blue-200 hover:underline label-02-bold my-10 px-8 py-12"
        :class="{ disabled: menuItem.isDisabled, 'mb-0': menuItem.children }"
        exact-path
        :to="menuItem.route"
      >
        {{ menuItem.name }}
      </router-link>

      <router-link
        v-for="(childMenuItem, index) in menuItem.children"
        :key="childMenuItem.name"
        class="block hover:bg-blue-200 hover:underline label-02-reg p-10 pl-16"
        :class="{ 'mb-24': index + 1 == menuItem.children?.length }"
        :to="childMenuItem.route"
      >
        {{ childMenuItem.name }}
      </router-link>
    </div>
  </div>
</template>
