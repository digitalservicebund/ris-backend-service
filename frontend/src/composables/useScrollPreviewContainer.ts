import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { useExtraContentSidePanelStore } from "@/stores/extraContentSidePanelStore"

export function useScrollPreviewContainer() {
  const extraContentSidePanelStore = useExtraContentSidePanelStore()
  const documentUnitStore = useDocumentUnitStore()

  async function openSidePanel(documentUnitNumber?: string) {
    if (documentUnitNumber) {
      await documentUnitStore.loadDocumentUnit(documentUnitNumber)
      extraContentSidePanelStore.togglePanel(true)

      const container = document.getElementById("preview-container")
      setTimeout(() => {
        if (!container) return
        const target =
          document.getElementById("previewGuidingPrinciple") ??
          document.getElementById("previewTenor")
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
    openSidePanel,
  }
}
