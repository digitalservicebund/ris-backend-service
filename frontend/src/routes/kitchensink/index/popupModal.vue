<script lang="ts" setup>
import Button from "primevue/button"
import { ref } from "vue"
import PopupModal from "@/components/PopupModal.vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"

const showModal = ref(false)
const toggleModal = () => {
  showModal.value = !showModal.value
}
</script>

<template>
  <KitchensinkPage name="Popup modal">
    <div>
      <Button label="Open Modal" @click="toggleModal"></Button>
      <PopupModal
        v-if="showModal"
        aria-label="Popup Modal"
        cancel-button-type="ghost"
        content-text="This is Popup Modal content"
        header-text="Popup Modal"
        primary-button-text="Bestätigen"
        primary-button-type="secondary"
        secondary-button-text="Alternative"
        secondary-button-type="tertiary"
        @close-modal="toggleModal"
        @primary-action="toggleModal"
        @secondary-action="toggleModal"
      />
    </div>
  </KitchensinkPage>
</template>
