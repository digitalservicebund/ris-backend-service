<script lang="ts" setup>
import SideToggle from "@/components/SideToggle.vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
</script>

<template>
  <KitchensinkPage name="Side toggle">
    <div class="flex h-[10rem] justify-between gap-[3rem]">
      <SideToggle from-side="left" label="Inhalt">
        <div class="flex w-full p-24">
          Lorem ipsum dolor sit amet consectetur adipisicing elit. Voluptatem
          iusto autem quasi saepe fuga quis dolores dolor ipsum eligendi dolore
          aliquid, magni quos excepturi amet, odit ipsa, provident eveniet
          accusamus.
        </div>
      </SideToggle>
      <SideToggle from-side="right" label="Inhalt rechts">
        <div class="flex w-full p-24">
          Lorem ipsum dolor sit amet consectetur adipisicing elit. Voluptatem
          iusto autem quasi saepe fuga quis dolores dolor ipsum eligendi dolore
          aliquid, magni quos excepturi amet, odit ipsa, provident eveniet
          accusamus.
        </div>
      </SideToggle>
    </div>
  </KitchensinkPage>
</template>
