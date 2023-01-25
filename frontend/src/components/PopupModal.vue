<script lang="ts" setup>
import { onMounted } from "vue"
import TextButton from "./TextButton.vue"

defineProps<{
  ariaLabel?: string
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
    :aria-label="ariaLabel"
    class="bg-background fixed flex h-full items-center justify-center left-0 popup-modal-wrapper top-0 w-screen z-999"
    role="dialog"
    tabindex="0"
    @click.self="$emit('closeModal')"
    @keydown.esc="$emit('closeModal')"
  >
    <div
      class="bg-white border-2 border-blue-800 border-solid box-border flex flex-col gap-[1rem] items-start modal-container px-[3.5rem] py-[2.5rem]"
    >
      <div class="label-01-bold text-black">
        {{ headerText }}
      </div>
      <div class="label-03-reg text-black">{{ contentText }}</div>
      <div class="flex flex-row gap-[1rem] modal-buttons-container">
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
