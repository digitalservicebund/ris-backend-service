import { createTestingPinia } from "@pinia/testing"
import { setActivePinia } from "pinia"
import { describe, it, vi, beforeEach, afterEach, expect } from "vitest"
import { useScroll } from "@/composables/useScroll"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { useExtraContentSidePanelStore } from "@/stores/extraContentSidePanelStore"

// Mock the stores
vi.mock("@/stores/documentUnitStore", () => ({
  useDocumentUnitStore: vi.fn(),
}))

vi.mock("@/stores/extraContentSidePanelStore", () => ({
  useExtraContentSidePanelStore: vi.fn(),
}))

describe("useScroll", () => {
  let documentUnitStoreMock: any // eslint-disable-line @typescript-eslint/no-explicit-any
  let extraContentSidePanelStoreMock: any // eslint-disable-line @typescript-eslint/no-explicit-any

  beforeEach(() => {
    vi.useFakeTimers()
    // Activate Pinia
    setActivePinia(createTestingPinia())

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

  it("scrollIntoViewportById scrolls to the element with the correct offset", async () => {
    // Arrange
    const id = "test-element"

    const element = Object.create(HTMLElement.prototype)
    Object.defineProperty(element, "getBoundingClientRect", {
      value: () => ({
        top: 300,
      }),
    })

    vi.spyOn(document, "getElementById").mockImplementation((elementId) => {
      return elementId === id ? element : null
    })

    Object.defineProperty(window, "scrollY", {
      value: 50,
    })

    window.scrollTo = vi.fn()

    const { scrollIntoViewportById } = useScroll()

    // Act
    await scrollIntoViewportById(id)
    vi.runAllTimers() // Process the setTimeout

    // Assert
    expect(window.scrollTo).toHaveBeenCalledWith({
      top: 180, // 300 (element top) + 50 (scrollY) - 170 (headerOffset)
      behavior: "smooth",
    })
  })

  it("scrollIntoViewportById does nothing if the element is not found", async () => {
    // Arrange
    const id = "non-existent-element"

    vi.spyOn(document, "getElementById").mockImplementation(() => null)
    window.scrollTo = vi.fn()

    const { scrollIntoViewportById } = useScroll()

    // Act
    await scrollIntoViewportById(id)
    vi.runAllTimers()

    // Assert
    expect(window.scrollTo).not.toHaveBeenCalled()
  })

  it("openSidePanelAndScrollToSection loads the document and opens the side panel when a documentUnitNumber is provided", async () => {
    // Arrange
    const documentUnitNumber = "12345"
    const { openSidePanelAndScrollToSection } = useScroll()

    // Act
    await openSidePanelAndScrollToSection(documentUnitNumber)

    // Assert
    expect(documentUnitStoreMock.loadDocumentUnit).toHaveBeenCalledWith(
      documentUnitNumber,
    )
    expect(extraContentSidePanelStoreMock.togglePanel).toHaveBeenCalledWith(
      true,
    )
  })

  it("openSidePanelAndScrollToSection does nothing if no documentUnitNumber is provided", async () => {
    // Arrange
    const { openSidePanelAndScrollToSection } = useScroll()

    // Act
    await openSidePanelAndScrollToSection()

    // Assert
    expect(documentUnitStoreMock.loadDocumentUnit).not.toHaveBeenCalled()
    expect(extraContentSidePanelStoreMock.togglePanel).not.toHaveBeenCalled()
  })

  it("openSidePanelAndScrollToSection scrolls to 'previewGuidingPrinciple' if it exists", async () => {
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
    const { openSidePanelAndScrollToSection } = useScroll()

    // Act - Case 1: previewGuidingPrinciple exists
    await openSidePanelAndScrollToSection(documentUnitNumber)
    vi.runAllTimers() // Make sure any setTimeout is processed

    // Assert - Scrolls to previewGuidingPrinciple
    expect(container.scrollTo).toHaveBeenCalledWith({
      top: 150, // targetGuidingPrinciple.top - container.top => 200 - 50 = 150
      behavior: "smooth",
    })
  })

  it("openSidePanelAndScrollToSection scrolls to 'previewTenor' if 'previewGuidingPrinciple' doesn't exist", async () => {
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

    const { openSidePanelAndScrollToSection } = useScroll()

    await openSidePanelAndScrollToSection(documentUnitNumber)
    vi.runAllTimers()

    expect(container.scrollTo).toHaveBeenCalledWith({
      top: 250, // targetTenor.top - container.top => 300 - 50 = 250
      behavior: "smooth",
    })
  })

  it("openSidePanelAndScrollToSection scrolls to top if neither 'previewTenor' or 'previewGuidingPrinciple' exist", async () => {
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

    const { openSidePanelAndScrollToSection } = useScroll()

    await openSidePanelAndScrollToSection(documentUnitNumber)
    vi.runAllTimers()

    expect(container.scrollTo).toHaveBeenCalledWith({
      top: 0,
      behavior: "smooth",
    })
  })
})
