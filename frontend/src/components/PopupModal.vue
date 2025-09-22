<script lang="ts" setup>
import Button from "primevue/button"
import { onMounted } from "vue"

defineProps<{
  ariaLabel?: string
  headerText?: string
  contentText: string
  primaryButtonText: string
  secondaryButtonText?: string
}>()

defineEmits<{
  closeModal: []
  primaryAction: []
  secondaryAction: []
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
  <!-- eslint-disable-next-line vuejs-accessibility/no-static-element-interactions -->
  <dialog
    :aria-label="ariaLabel"
    class="popup-modal-wrapper fixed top-0 left-0 z-50 flex h-full w-screen items-center justify-center bg-black/40"
    @click.self="$emit('closeModal')"
    @keydown.esc="$emit('closeModal')"
  >
    <div
      class="modal-container box-border flex flex-col items-start gap-[1rem] rounded-sm bg-white px-[1.5rem] py-[1.5rem]"
    >
      <div class="ris-label1-bold text-black">
        {{ headerText }}
      </div>
      <div class="ris-label3-regular whitespace-pre-line text-black">
        {{ contentText }}
      </div>
      <div
        class="modal-buttons-container flex w-full flex-row justify-end gap-[1rem]"
      >
        <Button
          :aria-label="primaryButtonText"
          :label="primaryButtonText"
          size="small"
          @click="$emit('primaryAction')"
        ></Button>
        <Button
          v-if="secondaryButtonText"
          :aria-label="secondaryButtonText"
          :label="secondaryButtonText"
          severity="secondary"
          size="small"
          @click="$emit('secondaryAction')"
        ></Button>
        <Button
          aria-label="Abbrechen"
          label="Abbrechen"
          severity="secondary"
          size="small"
          @click="$emit('closeModal')"
        ></Button>
      </div>
    </div>
  </dialog>
</template>
