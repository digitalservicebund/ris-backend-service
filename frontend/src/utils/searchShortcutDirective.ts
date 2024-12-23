import { Directive } from "vue"

export const searchShortcutDirective: Directive = {
  mounted(el, binding) {
    const handleKeydown = (event: KeyboardEvent) => {
      if (
        el.contains(document.activeElement) && // Ensure focus is inside the element
        event.key === "Enter" &&
        (event.ctrlKey || event.metaKey)
      ) {
        binding.value?.(event) // Call the provided callback
      }
    }

    el.__handleKeydown__ = handleKeydown // Store reference for cleanup
    window.addEventListener("keydown", handleKeydown)
  },
  unmounted(el) {
    window.removeEventListener("keydown", el.__handleKeydown__)
    delete el.__handleKeydown__
  },
}
