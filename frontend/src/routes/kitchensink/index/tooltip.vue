<script setup lang="ts">
import Button from "primevue/button"
import Tooltip from "@/components/Tooltip.vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
</script>

<template>
  <KitchensinkPage name="Tooltip">
    <div class="flex gap-20">
      <Tooltip text="I'm a tooltip">
        <Button label="Hover me"></Button>
      </Tooltip>

      <Tooltip shortcut="Key1 + Key2" text="I'm a tooltip with shortcut">
        <Button label="Hover me, I have a shortcut"></Button>
      </Tooltip>

      <Tooltip
        text="Tooltips shouldn’t really exceed one line, but sometimes it’s unavoidable"
      >
        <Button label="Hover me, I have a long tooltip"></Button>
      </Tooltip>
    </div>
  </KitchensinkPage>
</template>
