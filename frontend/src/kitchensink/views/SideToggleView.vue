<script lang="ts" setup>
import { ref } from "vue"
import SideToggle from "@/components/SideToggle.vue"

const visible = ref(false)
</script>

<template>
  <div class="flex flex-row gap-[3rem] h-[10rem] justify-between w-full">
    <div class="flex h-[10rem]">
      <SideToggle from-side="left" :is-expanded="visible" label="Inhalt"
        ><div class="flex w-full">
          Lorem ipsum dolor sit amet consectetur adipisicing elit. Voluptatem
          iusto autem quasi saepe fuga quis dolores dolor ipsum eligendi dolore
          aliquid, magni quos excepturi amet, odit ipsa, provident eveniet
          accusamus.
        </div>
      </SideToggle>
    </div>
    <div class="flex h-[10rem]">
      <SideToggle from-side="right" :is-expanded="visible" label="Inhalt rechts"
        ><div class="flex w-full">
          Lorem ipsum dolor sit amet consectetur adipisicing elit. Voluptatem
          iusto autem quasi saepe fuga quis dolores dolor ipsum eligendi dolore
          aliquid, magni quos excepturi amet, odit ipsa, provident eveniet
          accusamus.
        </div></SideToggle
      >
    </div>
  </div>
</template>
