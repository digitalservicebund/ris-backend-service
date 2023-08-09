<script lang="ts" setup>
import { onMounted } from "vue"
import TextButton from "./input/TextButton.vue"

defineProps<{
  ariaLabel?: string
  headerText?: string
  contentText: string
  confirmText: string
  cancelButtonType?: string
  confirmButtonType?: string
}>()

defineEmits<{
  closeModal: []
  confirmAction: []
}>()

onMounted(() => {
  const popupModalElem = document.getElementsByClassName(
    "popup-modal-wrapper",
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
      } else if (
        /** Jump to first button */
        document.activeElement ===
        (focusableElems[focusableElems.length - 1] as HTMLElement)
      ) {
        ;(focusableElems[0] as HTMLElement).focus()
        e.preventDefault()
      }
    }
  })
})
</script>

<template>
  <div
    :aria-label="ariaLabel"
    class="popup-modal-wrapper z-999 fixed left-0 top-0 flex h-full w-screen items-center justify-center bg-background"
    role="dialog"
    tabindex="0"
    @click.self="$emit('closeModal')"
    @keydown.esc="$emit('closeModal')"
  >
    <div
      class="modal-container box-border flex flex-col items-start gap-[1rem] border-2 border-solid border-blue-800 bg-white px-[3.5rem] py-[2.5rem]"
    >
      <div class="ds-label-01-bold text-black">
        {{ headerText }}
      </div>
      <div class="ds-label-03-reg text-black">{{ contentText }}</div>
      <div class="modal-buttons-container flex flex-row gap-[1rem]">
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
