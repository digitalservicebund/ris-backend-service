<script setup lang="ts">
import Button from "primevue/button"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"

/**
 * Helper to format tooltip with shortcut on a new line
 */
const getTooltip = (label: string, shortcut?: string) => {
  return shortcut ? `${label}\n(${shortcut})` : label
}
</script>

<template>
  <KitchensinkPage name="Tooltip">
    <div class="flex gap-20">
      <Button v-tooltip.bottom="'I\'m a tooltip'" label="Hover me"></Button>

      <Button
        v-tooltip.bottom="
          getTooltip('I\'m a tooltip with shortcut', 'Key1 + Key2')
        "
        label="Hover me, I have a shortcut"
      ></Button>
    </div>
  </KitchensinkPage>
</template>
