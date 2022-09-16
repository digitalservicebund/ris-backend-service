<script lang="ts" setup>
import { onMounted } from "vue"
import TextButton from "./TextButton.vue"

defineProps<{
  headerText?: string
  contentText: string
  confirmText: string
  cancelButtonType?: string
  confirmButtonType?: string
}>()
defineEmits<{
  (e: "closeModal"): void
  (e: "confirmAction"): void
}>()

onMounted(() => {
  const popupModalElem = document.getElementsByClassName(
    "popup-modal-wrapper"
  )[0] as HTMLElement
  popupModalElem.focus()
  const focusableElemsSelector = ".modal-buttons-container button"
  const focusableElems = document.querySelectorAll(focusableElemsSelector)
  /** Modal focus trap event */
  document.addEventListener("keydown", (e) => {
    const isTabPressed = e.key === "Tab"
    if (isTabPressed) {
      if (e.shiftKey) {
        /** Shift + Tab select popupModal */
        if (document.activeElement === popupModalElem) {
          ;(focusableElems[focusableElems.length - 1] as HTMLElement).focus()
          e.preventDefault()
        }
      } else {
        if (
          /** Jump to first button */
          document.activeElement ===
          (focusableElems[focusableElems.length - 1] as HTMLElement)
        ) {
          ;(focusableElems[0] as HTMLElement).focus()
          e.preventDefault()
        }
      }
    }
  })
})
</script>

<template>
  <div
    class="popup-modal-wrapper"
    role="dialog"
    tabindex="0"
    @click.self="$emit('closeModal')"
    @keydown.esc="$emit('closeModal')"
  >
    <div class="modal-container">
      <div class="modal-text-title">
        {{ headerText }}
      </div>
      <div class="modal-text-content">{{ contentText }}</div>
      <div class="modal-buttons-container">
        <TextButton
          :button-type="cancelButtonType"
          label="Abbrechen"
          @click="$emit('closeModal')"
        />
        <TextButton
          :button-type="confirmButtonType"
          :label="confirmText"
          @click="$emit('confirmAction')"
        />
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@import "@/styles/variables";

.popup-modal-wrapper {
  position: fixed;
  z-index: 999;
  top: 0;
  left: 0;
  display: flex;
  width: 100vw;
  height: 100%;
  align-items: center;
  justify-content: center;
  background-color: rgb(111 119 133 / 60%);

  .modal-container {
    display: flex;
    max-width: 442px;
    min-height: 222px;
    box-sizing: border-box;
    flex-direction: column;
    align-items: flex-start;
    padding: 40px;
    border: 2px solid $blue800;
    background: $white;
    gap: 16px;

    .modal-text-title {
      color: $black;
      font-size: 18px;
      font-weight: 700;
      letter-spacing: 0.16px;
      line-height: 26px;
    }

    .modal-text-content {
      color: $black;
      font-size: 14px;
      font-style: normal;
      font-weight: 400;
      letter-spacing: 0.16px;
      line-height: 18px;
      text-align: left;
    }

    .modal-buttons-container {
      display: flex;
      min-width: 70%;
      flex-direction: row;
      justify-content: space-between;
      column-gap: 16px;
    }
  }
}
</style>
