<script lang="ts" setup>
import { ref, Ref } from "vue"
import { useRoute } from "vue-router"
import NavbarSide from "@/components/NavbarSide.vue"
import SideToggle from "@/components/SideToggle.vue"
import { useCaseLawMenuItems } from "@/composables/useCaseLawMenuItems"
import useQuery from "@/composables/useQueryFromRoute"

const props = defineProps<{
  documentNumber: string
}>()

const route = useRoute()

const toggleNavigationPanel = () => {
  showNavigationPanelRef.value = !showNavigationPanelRef.value
  pushQueryToRoute({
    showNavigationPanel: showNavigationPanelRef.value.toString(),
  })
}

const showNavigationPanelRef: Ref<boolean> = ref(
  route.query.showNavigationPanel !== "false",
)

const { pushQueryToRoute } = useQuery<"showNavigationPanel">()

const menuItems = useCaseLawMenuItems(props.documentNumber, route.query)
</script>

<template>
  <div
    class="sticky top-0 z-50 flex flex-col border-r-1 border-solid border-gray-400 bg-white"
  >
    <SideToggle
      class="sticky top-0 z-20"
      :is-expanded="showNavigationPanelRef"
      label="Navigation"
      size="small"
      tabindex="0"
      @keydown.enter="toggleNavigationPanel"
      @update:is-expanded="toggleNavigationPanel"
    >
      <NavbarSide :is-child="false" :menu-items="menuItems" :route="route" />
    </SideToggle>
  </div>
  <router-view />
  <!--  side panel-->
</template>
