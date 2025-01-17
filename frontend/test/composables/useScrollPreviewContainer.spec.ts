import { createPinia, setActivePinia } from "pinia"
import { describe, it, vi, beforeEach, afterEach, expect } from "vitest"
import { useScrollPreviewContainer } from "@/composables/useScrollPreviewContainer"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { useExtraContentSidePanelStore } from "@/stores/extraContentSidePanelStore"

// Mock the stores
vi.mock("@/stores/documentUnitStore", () => ({
  useDocumentUnitStore: vi.fn(),
}))

vi.mock("@/stores/extraContentSidePanelStore", () => ({
  useExtraContentSidePanelStore: vi.fn(),
}))

describe("useOpenSidePanelWithDocUnit", () => {
  let documentUnitStoreMock: any // eslint-disable-line @typescript-eslint/no-explicit-any
  let extraContentSidePanelStoreMock: any // eslint-disable-line @typescript-eslint/no-explicit-any

  beforeEach(() => {
    vi.useFakeTimers()
    // Activate Pinia
    setActivePinia(createPinia())

    // Mock the stores
    documentUnitStoreMock = {
      loadDocumentUnit: vi.fn(),
    }
    extraContentSidePanelStoreMock = {
      togglePanel: vi.fn(),
    }

    // Replace implementations of the stores with mocks
    vi.mocked(useDocumentUnitStore).mockReturnValue(documentUnitStoreMock)
    vi.mocked(useExtraContentSidePanelStore).mockReturnValue(
      extraContentSidePanelStoreMock,
    )
  })

  afterEach(() => {
    vi.resetAllMocks()
    vi.useRealTimers()
  })

  it("loads the document and opens the side panel when a documentUnitNumber is provided", async () => {
    // Arrange
    const documentUnitNumber = "12345"
    const { openSidePanel } = useScrollPreviewContainer()

    // Act
    await openSidePanel(documentUnitNumber)

    // Assert
    expect(documentUnitStoreMock.loadDocumentUnit).toHaveBeenCalledWith(
      documentUnitNumber,
    )
    expect(extraContentSidePanelStoreMock.togglePanel).toHaveBeenCalledWith(
      true,
    )
  })

  it("does nothing if no documentUnitNumber is provided", async () => {
    // Arrange
    const { openSidePanel } = useScrollPreviewContainer()

    // Act
    await openSidePanel()

    // Assert
    expect(documentUnitStoreMock.loadDocumentUnit).not.toHaveBeenCalled()
    expect(extraContentSidePanelStoreMock.togglePanel).not.toHaveBeenCalled()
  })

  it("scrolls to 'previewGuidingPrinciple' if it exists", async () => {
    // Arrange
    const documentUnitNumber = "12345"

    const container = Object.create(HTMLElement.prototype)
    container.scrollTo = vi.fn()
    Object.defineProperty(container, "offsetTop", {
      get: () => 50,
    })

    const targetGuidingPrinciple = Object.create(HTMLElement.prototype)
    Object.defineProperty(targetGuidingPrinciple, "offsetTop", {
      get: () => 200,
    })

    const targetTenor = Object.create(HTMLElement.prototype)
    Object.defineProperty(targetTenor, "offsetTop", {
      get: () => 300,
    })

    // Mocking getElementById to return our mock elements
    vi.spyOn(document, "getElementById").mockImplementation((id: string) => {
      if (id === "preview-container") {
        return container
      }
      if (id === "previewGuidingPrinciple") {
        return targetGuidingPrinciple
      }
      if (id === "previewTenor") {
        return targetTenor
      }
      return null
    })

    // Get the composable function
    const { openSidePanel } = useScrollPreviewContainer()

    // Act - Case 1: previewGuidingPrinciple exists
    await openSidePanel(documentUnitNumber)
    vi.runAllTimers() // Make sure any setTimeout is processed

    // Assert - Scrolls to previewGuidingPrinciple
    expect(container.scrollTo).toHaveBeenCalledWith({
      top: 150, // targetGuidingPrinciple.top - container.top => 200 - 50 = 150
      behavior: "smooth",
    })
  })

  it("scrolls to 'previewTenor' if 'previewGuidingPrinciple' doesn't exist", async () => {
    // Arrange
    const documentUnitNumber = "12345"

    const container = Object.create(HTMLElement.prototype)
    container.scrollTo = vi.fn()
    Object.defineProperty(container, "offsetTop", {
      get: () => 50,
    })

    const targetTenor = Object.create(HTMLElement.prototype)
    Object.defineProperty(targetTenor, "offsetTop", {
      get: () => 300,
    })

    // Mocking getElementById to return our mock elements
    vi.spyOn(document, "getElementById").mockImplementation((id: string) => {
      if (id === "preview-container") {
        return container
      }
      if (id === "previewGuidingPrinciple") {
        return null
      }
      if (id === "previewTenor") {
        return targetTenor
      }
      return null
    })

    const { openSidePanel } = useScrollPreviewContainer()

    await openSidePanel(documentUnitNumber)
    vi.runAllTimers()

    expect(container.scrollTo).toHaveBeenCalledWith({
      top: 250, // targetTenor.top - container.top => 300 - 50 = 250
      behavior: "smooth",
    })
  })

  it("scrolls to top if neither 'previewTenor' or 'previewGuidingPrinciple' exist", async () => {
    // Arrange
    const documentUnitNumber = "12345"

    const container = Object.create(HTMLElement.prototype)
    container.scrollTo = vi.fn()
    Object.defineProperty(container, "offsetTop", {
      get: () => 50,
    })

    // Mocking getElementById to return our mock elements
    vi.spyOn(document, "getElementById").mockImplementation((id: string) => {
      if (id === "preview-container") {
        return container
      }
      if (id === "previewGuidingPrinciple") {
        return null
      }
      if (id === "previewTenor") {
        return null
      }
      return null
    })

    const { openSidePanel } = useScrollPreviewContainer()

    await openSidePanel(documentUnitNumber)
    vi.runAllTimers()

    expect(container.scrollTo).toHaveBeenCalledWith({
      top: 0,
      behavior: "smooth",
    })
  })
})
