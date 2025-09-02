import { createTestingPinia } from "@pinia/testing"
import { fireEvent, render, screen } from "@testing-library/vue"
import { flushPromises } from "@vue/test-utils"
import { setActivePinia } from "pinia"
import PublicationActions from "@/components/publication/PublicationActions.vue"
import { Decision } from "@/domain/decision"
import { PortalPublicationStatus } from "@/domain/portalPublicationStatus"
import publishDocumentationUnitService from "@/services/publishDocumentationUnitService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { useFeatureToggleServiceMock } from "~/test-helper/useFeatureToggleServiceMock"

const publishMock = vi.spyOn(publishDocumentationUnitService, "publishDocument")
const withdrawMock = vi.spyOn(
  publishDocumentationUnitService,
  "withdrawDocument",
)

async function renderComponent(props: {
  isPublishable: boolean
  publicationWarnings: string[]
}) {
  return render(PublicationActions, {
    props,
  })
}
function mockDocUnitStore(
  portalPublicationStatus: PortalPublicationStatus,
  more = {},
) {
  const mockedDocUnitStore = useDocumentUnitStore()
  mockedDocUnitStore.documentUnit = new Decision("q834", {
    portalPublicationStatus,
    ...more,
  })
  return mockedDocUnitStore
}

describe("PublicationActions", () => {
  beforeEach(() => {
    setActivePinia(createTestingPinia())
    useFeatureToggleServiceMock()
  })

  describe("Status: Unpublished", () => {
    it("should show the status", async () => {
      mockDocUnitStore(PortalPublicationStatus.UNPUBLISHED)
      await renderComponent({ isPublishable: false, publicationWarnings: [] })

      expect(screen.getByText("Unveröffentlicht")).toBeInTheDocument()
    })

    it("should not allow to publish an unpublishable doc unit", async () => {
      mockDocUnitStore(PortalPublicationStatus.UNPUBLISHED)
      await renderComponent({ isPublishable: false, publicationWarnings: [] })

      expect(
        screen.getByRole("button", { name: "Veröffentlichen" }),
      ).toBeDisabled()
    })

    it("should allow to publish a publishable doc unit", async () => {
      mockDocUnitStore(PortalPublicationStatus.UNPUBLISHED)
      await renderComponent({ isPublishable: true, publicationWarnings: [] })

      expect(
        screen.getByRole("button", { name: "Veröffentlichen" }),
      ).toBeEnabled()
    })

    it("should not show withdraw for unpublished doc unit", async () => {
      mockDocUnitStore(PortalPublicationStatus.UNPUBLISHED)
      await renderComponent({ isPublishable: false, publicationWarnings: [] })

      expect(
        screen.queryByRole("button", { name: "Zurückziehen" }),
      ).not.toBeInTheDocument()
    })
  })

  describe("Status: Published", () => {
    it("should show the status", async () => {
      mockDocUnitStore(PortalPublicationStatus.PUBLISHED)
      await renderComponent({ isPublishable: false, publicationWarnings: [] })

      expect(screen.getByText("Veröffentlicht")).toBeInTheDocument()
    })

    it("should not allow to publish an unpublishable doc unit again", async () => {
      mockDocUnitStore(PortalPublicationStatus.PUBLISHED)
      await renderComponent({ isPublishable: false, publicationWarnings: [] })

      expect(
        screen.getByRole("button", { name: "Veröffentlichen" }),
      ).toBeDisabled()
    })

    it("should allow to publish a publishable doc unit again", async () => {
      mockDocUnitStore(PortalPublicationStatus.PUBLISHED)
      await renderComponent({ isPublishable: true, publicationWarnings: [] })

      expect(
        screen.getByRole("button", { name: "Veröffentlichen" }),
      ).toBeEnabled()
    })

    it("should allow to withdraw publishable published doc unit", async () => {
      mockDocUnitStore(PortalPublicationStatus.PUBLISHED)
      await renderComponent({ isPublishable: false, publicationWarnings: [] })

      expect(
        screen.queryByRole("button", { name: "Zurückziehen" }),
      ).toBeEnabled()
    })

    it("should allow to withdraw unpublishable published doc unit", async () => {
      mockDocUnitStore(PortalPublicationStatus.PUBLISHED)
      await renderComponent({ isPublishable: true, publicationWarnings: [] })

      expect(
        screen.queryByRole("button", { name: "Zurückziehen" }),
      ).toBeEnabled()
    })

    it("shows portal link for published doc unit", async () => {
      mockDocUnitStore(PortalPublicationStatus.PUBLISHED, {
        documentNumber: "PUB123",
      })
      await renderComponent({ isPublishable: true, publicationWarnings: [] })

      expect(
        screen.getByText("Portalseite der Dokumentationseinheit"),
      ).toBeInTheDocument()
      const link = screen.getByRole("link", {
        name: "Portalseite der Dokumentationseinheit",
      })
      expect(link).toBeInTheDocument()
      expect(link).toHaveAttribute(
        "href",
        expect.stringContaining("/case-law/PUB123"),
      )
      expect(link).toHaveAttribute("target", "_blank")
      expect(link).toHaveAttribute("rel", "noopener noreferrer")
    })

    it("shows lastPublishedAt for published doc unit", async () => {
      mockDocUnitStore(PortalPublicationStatus.PUBLISHED, {
        managementData: { lastPublishedAtDateTime: "2022-01-02T13:45:00Z" },
      })
      await renderComponent({ isPublishable: true, publicationWarnings: [] })
      expect(screen.getByText(/Zuletzt veröffentlicht am:/)).toBeInTheDocument()
      expect(screen.getByText(/02\.01\.2022/)).toBeInTheDocument()
    })

    it("shows no hint for published doc unit after failed publication", async () => {
      mockDocUnitStore(PortalPublicationStatus.PUBLISHED)
      await renderComponent({ isPublishable: true, publicationWarnings: [] })

      expect(
        screen.queryByText(/Das Hochladen der Stammdaten.*2 Minuten/),
      ).not.toBeInTheDocument()
    })

    it("shows hint for published doc unit after successful publication", async () => {
      mockDocUnitStore(PortalPublicationStatus.PUBLISHED)
      publishMock.mockResolvedValue({
        status: 200,
        data: undefined,
      })
      await renderComponent({ isPublishable: true, publicationWarnings: [] })

      await fireEvent.click(
        screen.getByRole("button", { name: "Veröffentlichen" }),
      )
      await flushPromises()
      expect(
        screen.getByText(
          "Das Hochladen der Stammdaten und der Informationen im Portal-Tab „Details“ dauert etwa 2 Minuten.",
        ),
      ).toBeInTheDocument()
    })
  })

  describe("Status: Withdrawn", () => {
    it("should show the status", async () => {
      mockDocUnitStore(PortalPublicationStatus.WITHDRAWN)
      await renderComponent({ isPublishable: false, publicationWarnings: [] })

      expect(screen.getByText("Zurückgezogen")).toBeInTheDocument()
    })

    it("should not allow to publish an unpublishable doc unit", async () => {
      mockDocUnitStore(PortalPublicationStatus.WITHDRAWN)
      await renderComponent({ isPublishable: false, publicationWarnings: [] })

      expect(
        screen.getByRole("button", { name: "Veröffentlichen" }),
      ).toBeDisabled()
    })

    it("should allow to publish a publishable doc unit", async () => {
      mockDocUnitStore(PortalPublicationStatus.WITHDRAWN)
      await renderComponent({ isPublishable: true, publicationWarnings: [] })

      expect(
        screen.getByRole("button", { name: "Veröffentlichen" }),
      ).toBeEnabled()
    })

    it("should not show withdraw for unpublished doc unit", async () => {
      mockDocUnitStore(PortalPublicationStatus.WITHDRAWN)
      await renderComponent({ isPublishable: true, publicationWarnings: [] })

      expect(
        screen.queryByRole("button", { name: "Zurückziehen" }),
      ).not.toBeInTheDocument()
    })

    it("shows removal text for withdrawn doc unit", async () => {
      mockDocUnitStore(PortalPublicationStatus.WITHDRAWN)
      await renderComponent({ isPublishable: true, publicationWarnings: [] })

      expect(
        screen.getByText("Portalseite der Dokumentationseinheit"),
      ).toBeInTheDocument()
      expect(screen.getByText("wurde entfernt")).toBeInTheDocument()
      expect(
        screen.getByRole("link", {
          name: "Portalseite der Dokumentationseinheit",
        }),
      ).toBeInTheDocument()
    })

    it("shows lastPublishedAt date for withdrawn doc unit", async () => {
      mockDocUnitStore(PortalPublicationStatus.WITHDRAWN, {
        managementData: { lastPublishedAtDateTime: "2023-04-05T12:00:00Z" },
      })
      await renderComponent({ isPublishable: true, publicationWarnings: [] })

      expect(screen.getByText(/Zuletzt veröffentlicht am:/)).toBeInTheDocument()
      expect(screen.getByText(/05\.04\.2023/)).toBeInTheDocument()
    })

    it("shows no hint for withdrawn doc unit after failed withdraw", async () => {
      mockDocUnitStore(PortalPublicationStatus.WITHDRAWN)
      await renderComponent({ isPublishable: true, publicationWarnings: [] })

      expect(
        screen.queryByText(/Das Hochladen der Stammdaten.*2 Minuten/),
      ).not.toBeInTheDocument()
    })

    it("shows hint for withdrawn doc unit after successful withdraw", async () => {
      mockDocUnitStore(PortalPublicationStatus.PUBLISHED)
      withdrawMock.mockResolvedValue({
        status: 200,
        data: undefined,
      })
      await renderComponent({ isPublishable: true, publicationWarnings: [] })

      await fireEvent.click(
        screen.getByRole("button", { name: "Zurückziehen" }),
      )
      await flushPromises()
      expect(
        screen.getByText(
          "Das Hochladen der Stammdaten und der Informationen im Portal-Tab „Details“ dauert etwa 2 Minuten.",
        ),
      ).toBeInTheDocument()
    })
  })
})
