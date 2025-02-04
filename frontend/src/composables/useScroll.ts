import { Ref } from "vue"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { useExtraContentSidePanelStore } from "@/stores/extraContentSidePanelStore"

export function useScroll() {
  const extraContentSidePanelStore = useExtraContentSidePanelStore()
  const documentUnitStore = useDocumentUnitStore()
  const headerOffset = 170

  async function scrollIntoViewportById(id: string) {
    setTimeout(() => {
      const element = document.getElementById(id)
      if (element) {
        const elementPosition = element?.getBoundingClientRect().top
        const offsetPosition = elementPosition + window.scrollY - headerOffset

        window.scrollTo({
          top: offsetPosition,
          behavior: "smooth",
        })
      }
    })
  }

  async function scrollIntoViewportByRef(ref: Ref) {
    // Wait until the ref is available
    setTimeout(() => {
      const element = ref.value
      if (element) {
        const elementPosition = element.getBoundingClientRect().top
        const offsetPosition = elementPosition + window.scrollY - headerOffset

        window.scrollTo({
          top: offsetPosition,
          behavior: "smooth",
        })
      }
    })
  }

  async function openSidePanelAndScrollToSection(documentUnitNumber?: string) {
    if (documentUnitNumber) {
      await documentUnitStore.loadDocumentUnit(documentUnitNumber)
      extraContentSidePanelStore.togglePanel(true)

      const container = document.getElementById("preview-container")
      setTimeout(() => {
        if (!container) return
        const target =
          document.getElementById("previewGuidingPrinciple") ??
          document.getElementById("previewHeadnote")
        const scrollPosition = target
          ? target.offsetTop - container.offsetTop
          : 0

        container.scrollTo({
          top: scrollPosition,
          behavior: "smooth",
        })
      })
    }
  }

  return {
    scrollIntoViewportById,
    scrollIntoViewportByRef,
    openSidePanelAndScrollToSection,
  }
}
