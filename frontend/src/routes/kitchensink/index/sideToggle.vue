<script lang="ts" setup>
import { ref } from "vue"
import FlexContainer from "@/components/FlexContainer.vue"
import SideToggle, { OpeningDirection } from "@/components/SideToggle.vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"

const toggleShow = ref(true)
</script>

<template>
  <KitchensinkPage name="Side toggle">
    <FlexContainer class="h-[10rem] justify-between gap-[3rem]">
      <SideToggle
        class="float-right"
        :is-expanded="toggleShow"
        label="Inhalt links"
        :opening-direction="OpeningDirection.LEFT"
        @update:is-expanded="toggleShow = !toggleShow.valueOf()"
      >
        <div class="flex w-full p-24">
          Lorem ipsum dolor sit amet consectetur adipisicing elit. Voluptatem
          iusto autem quasi saepe fuga quis dolores dolor ipsum eligendi dolore
          aliquid, magni quos excepturi amet, odit ipsa, provident eveniet
          accusamus.
        </div>
      </SideToggle>
      <SideToggle
        :is-expanded="!toggleShow"
        label="Inhalt rechts"
        :opening-direction="OpeningDirection.RIGHT"
        @update:is-expanded="toggleShow = !toggleShow"
      >
        <div class="flex w-full p-24">
          Lorem ipsum dolor sit amet consectetur adipisicing elit. Voluptatem
          iusto autem quasi saepe fuga quis dolores dolor ipsum eligendi dolore
          aliquid, magni quos excepturi amet, odit ipsa, provident eveniet
          accusamus.
        </div>
      </SideToggle>
    </FlexContainer>
  </KitchensinkPage>
</template>
