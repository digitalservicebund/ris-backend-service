import { createTestingPinia } from "@pinia/testing"
import { render, screen } from "@testing-library/vue"
import { setActivePinia } from "pinia"
import { vi } from "vitest"
import { ref, Ref } from "vue"
import OtherCategories from "@/components/OtherCategories.vue"
import DocumentUnit from "@/domain/documentUnit"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

let courtTypeRef: Ref | undefined = undefined
vi.mock("@/composables/useCourtType", () => {
  return {
    useInjectCourtType: () => courtTypeRef,
  }
})
function mockSessionStore(hasLegislativeMandate: boolean) {
  const mockedSessionStore = useDocumentUnitStore()
  mockedSessionStore.documentUnit = new DocumentUnit("q834", {
    contentRelatedIndexing: { hasLegislativeMandate: hasLegislativeMandate },
  })

  return mockedSessionStore
}
describe("other categories", () => {
  beforeEach(() => {
    vi.resetModules()
    vi.resetAllMocks()
    setActivePinia(createTestingPinia())
  })
  test("should not display legislative mandate when it is false and courtType is non-constitutional", async () => {
    // Arrange
    courtTypeRef = ref("BAG")
    mockSessionStore(false)

    // Act
    render(OtherCategories)

    // Assert
    expect(screen.getByText("Berufsbild")).toBeInTheDocument()
    expect(screen.queryByText("Gesetzgebungsauftrag")).not.toBeInTheDocument()
    expect(
      screen.queryByText("Gesetzgebungsauftrag vorhanden"),
    ).not.toBeInTheDocument()
  })

  test("should display legislative mandate button when it is false and courtType is constitutional", async () => {
    // Arrange
    courtTypeRef = ref("BVerfG")
    mockSessionStore(false)

    // Act
    render(OtherCategories)

    // Assert
    expect(screen.getByText("Berufsbild")).toBeInTheDocument()
    expect(screen.getByText("Gesetzgebungsauftrag")).toBeInTheDocument()
    expect(
      screen.queryByText("Gesetzgebungsauftrag vorhanden"),
    ).not.toBeInTheDocument()
  })

  test("should display checked legislative mandate when it is true and has constitutional courtType", async () => {
    // Arrange
    courtTypeRef = ref("BVerfG")
    mockSessionStore(true)

    // Act
    render(OtherCategories)

    // Assert
    expect(screen.getByText("Berufsbild")).toBeInTheDocument()
    expect(screen.getByText("Gesetzgebungsauftrag")).toBeInTheDocument()
    expect(
      screen.getByText("Gesetzgebungsauftrag vorhanden"),
    ).toBeInTheDocument()
  })

  test("should display checked legislative mandate when it is true and has non-constitutional courtType", async () => {
    // Arrange
    courtTypeRef = ref("BAG")
    mockSessionStore(true)

    // Act
    render(OtherCategories)

    // Assert
    expect(screen.getByText("Berufsbild")).toBeInTheDocument()
    expect(screen.getByText("Gesetzgebungsauftrag")).toBeInTheDocument()
    expect(
      screen.getByText("Gesetzgebungsauftrag vorhanden"),
    ).toBeInTheDocument()
  })
})
