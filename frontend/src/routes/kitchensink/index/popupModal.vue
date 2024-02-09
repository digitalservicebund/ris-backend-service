<script lang="ts" setup>
import { ref } from "vue"
import TextButton from "@/components/input/TextButton.vue"
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
      <TextButton
        button-type="primary"
        label="Open Modal"
        @click="toggleModal"
      />
      <PopupModal
        v-if="showModal"
        aria-label="Popup Modal"
        cancel-button-type="ghost"
        confirm-button-type="secondary"
        confirm-text="Bestätigen"
        content-text="This is Popup Modal content"
        header-text="Popup Modal"
        @close-modal="toggleModal"
        @confirm-action="toggleModal"
      />
    </div>
  </KitchensinkPage>
</template>
