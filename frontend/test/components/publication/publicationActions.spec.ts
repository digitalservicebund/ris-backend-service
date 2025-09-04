import { createTestingPinia } from "@pinia/testing"
import { fireEvent, render, screen } from "@testing-library/vue"
import { flushPromises } from "@vue/test-utils"
import { setActivePinia } from "pinia"
import PublicationActions from "@/components/publication/PublicationActions.vue"
import { Decision } from "@/domain/decision"
import { PortalPublicationStatus } from "@/domain/portalPublicationStatus"
import { ServiceResponse } from "@/services/httpClient"
import publishDocumentationUnitService from "@/services/publishDocumentationUnitService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { useFeatureToggleServiceMock } from "~/test-helper/useFeatureToggleServiceMock"

const publishMock = vi.spyOn(publishDocumentationUnitService, "publishDocument")
const withdrawMock = vi.spyOn(
  publishDocumentationUnitService,
  "withdrawDocument",
)

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
          "Das Hochladen der Stammdaten und der Informationen im Portal-Tab „Details“ dauert ungefähr 5 Minuten.",
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
  })

  describe("Action: Publish", () => {
    describe("Error handling", () => {
      it("should not show error on mount", async () => {
        mockDocUnitStore(PortalPublicationStatus.UNPUBLISHED)
        await renderComponent({ isPublishable: true, publicationWarnings: [] })

        expect(
          screen.queryByLabelText(
            "Fehler bei der Veröffentlichung/Zurückziehung",
          ),
        ).not.toBeInTheDocument()
      })

      it("should not show error when publish is successful", async () => {
        mockDocUnitStore(PortalPublicationStatus.UNPUBLISHED)
        await renderComponent({ isPublishable: true, publicationWarnings: [] })
        vi.spyOn(
          publishDocumentationUnitService,
          "publishDocument",
        ).mockResolvedValue({ status: 200, data: undefined })

        await fireEvent.click(
          screen.getByRole("button", { name: "Veröffentlichen" }),
        )

        expect(
          screen.queryByLabelText(
            "Fehler bei der Veröffentlichung/Zurückziehung",
          ),
        ).not.toBeInTheDocument()
      })

      it("should reset previous error when publish is successful", async () => {
        mockDocUnitStore(PortalPublicationStatus.UNPUBLISHED)
        await renderComponent({ isPublishable: true, publicationWarnings: [] })
        vi.spyOn(publishDocumentationUnitService, "publishDocument")
          .mockResolvedValueOnce({
            status: 500,
            error: { title: "Error-Titel", description: "Error-Beschreibung" },
          })
          .mockResolvedValueOnce({ status: 200, data: undefined })

        // This one fails and shows error
        await fireEvent.click(
          screen.getByRole("button", { name: "Veröffentlichen" }),
        )

        expect(
          screen.getByLabelText(
            "Fehler bei der Veröffentlichung/Zurückziehung",
          ),
        ).toHaveTextContent("Error-TitelError-Beschreibung")

        // This one succeeds and resets the error
        await fireEvent.click(
          screen.getByRole("button", { name: "Veröffentlichen" }),
        )

        expect(
          screen.queryByLabelText(
            "Fehler bei der Veröffentlichung/Zurückziehung",
          ),
        ).not.toBeInTheDocument()
      })

      it("should show error when publish fails", async () => {
        mockDocUnitStore(PortalPublicationStatus.UNPUBLISHED)
        await renderComponent({ isPublishable: false, publicationWarnings: [] })
        vi.spyOn(
          publishDocumentationUnitService,
          "publishDocument",
        ).mockResolvedValue({
          status: 500,
          error: { title: "Error-Titel", description: "Error-Beschreibung" },
        })

        await fireEvent.click(
          screen.getByRole("button", { name: "Veröffentlichen" }),
        )

        expect(
          screen.getByLabelText(
            "Fehler bei der Veröffentlichung/Zurückziehung",
          ),
        ).toHaveTextContent("Error-TitelError-Beschreibung")
      })
    })

    describe("Loading state", () => {
      it("should not allow publish/withdraw while publishing", async () => {
        mockDocUnitStore(PortalPublicationStatus.PUBLISHED)
        await renderComponent({ isPublishable: true, publicationWarnings: [] })

        const { promise: publishPromise, resolve: resolvePublish } =
          Promise.withResolvers<ServiceResponse<void>>()
        vi.spyOn(
          publishDocumentationUnitService,
          "publishDocument",
        ).mockImplementation(() => publishPromise)

        await fireEvent.click(
          screen.getByRole("button", { name: "Veröffentlichen" }),
        )

        expect(
          screen.getByRole("button", { name: "Veröffentlichen" }),
        ).toBeDisabled()
        expect(
          screen.getByRole("button", { name: "Zurückziehen" }),
        ).toBeDisabled()

        // This simulates the server response
        resolvePublish({ status: 200, data: undefined })
        await flushPromises()

        expect(
          screen.getByRole("button", { name: "Veröffentlichen" }),
        ).toBeEnabled()
        expect(
          screen.getByRole("button", { name: "Zurückziehen" }),
        ).toBeEnabled()
      })
    })

    it("should update the status", async () => {
      const store = mockDocUnitStore(PortalPublicationStatus.UNPUBLISHED)
      await renderComponent({ isPublishable: true, publicationWarnings: [] })
      const loadDocUnitSpy = vi.spyOn(store, "loadDocumentUnit")
      vi.spyOn(
        publishDocumentationUnitService,
        "publishDocument",
      ).mockResolvedValue({ status: 200, data: undefined })

      store.documentUnit = new Decision("q834", {
        portalPublicationStatus: PortalPublicationStatus.PUBLISHED,
      })
      await fireEvent.click(
        screen.getByRole("button", { name: "Veröffentlichen" }),
      )

      expect(loadDocUnitSpy).toHaveBeenCalledOnce()
      expect(screen.getByText("Veröffentlicht")).toBeVisible()
    })

    it("should publish with the correct id", async () => {
      mockDocUnitStore(PortalPublicationStatus.UNPUBLISHED)
      await renderComponent({ isPublishable: true, publicationWarnings: [] })
      const publishSpy = vi
        .spyOn(publishDocumentationUnitService, "publishDocument")
        .mockResolvedValue({ status: 200, data: undefined })

      await fireEvent.click(
        screen.getByRole("button", { name: "Veröffentlichen" }),
      )

      expect(publishSpy).toHaveBeenCalledWith("q834")
    })

    describe("Publication warnings", () => {
      it("should not show dialog without warnings", async () => {
        mockDocUnitStore(PortalPublicationStatus.UNPUBLISHED)
        await renderComponent({ isPublishable: true, publicationWarnings: [] })

        await fireEvent.click(
          screen.getByRole("button", { name: "Veröffentlichen" }),
        )

        expect(
          screen.queryByRole("button", { name: "Trotzdem übergeben" }),
        ).not.toBeInTheDocument()
      })

      it("should show dialog with warnings", async () => {
        mockDocUnitStore(PortalPublicationStatus.UNPUBLISHED)
        await renderComponent({
          isPublishable: true,
          publicationWarnings: ["Dublettenwarnung", "Randnummernwarnung"],
        })

        await fireEvent.click(
          screen.getByRole("button", { name: "Veröffentlichen" }),
        )

        expect(
          screen.getByRole("button", { name: "Trotzdem veröffentlichen" }),
        ).toBeVisible()
        expect(
          screen.getByText(
            "Dublettenwarnung Randnummernwarnung Wollen Sie das Dokument dennoch übergeben?",
          ),
        ).toBeVisible()
      })

      it("should allow to publish despite warnings", async () => {
        mockDocUnitStore(PortalPublicationStatus.UNPUBLISHED)
        await renderComponent({
          isPublishable: true,
          publicationWarnings: ["Dublettenwarnung", "Randnummernwarnung"],
        })
        const publishServiceSpy = vi.spyOn(
          publishDocumentationUnitService,
          "publishDocument",
        )
        await fireEvent.click(
          screen.getByRole("button", { name: "Veröffentlichen" }),
        )
        await fireEvent.click(
          screen.getByRole("button", { name: "Trotzdem veröffentlichen" }),
        )

        expect(publishServiceSpy).toHaveBeenCalledWith("q834")
      })

      it("should allow to cancel publish with warnings", async () => {
        mockDocUnitStore(PortalPublicationStatus.UNPUBLISHED)
        await renderComponent({
          isPublishable: true,
          publicationWarnings: ["Dublettenwarnung", "Randnummernwarnung"],
        })
        const publishServiceSpy = vi.spyOn(
          publishDocumentationUnitService,
          "publishDocument",
        )
        await fireEvent.click(
          screen.getByRole("button", { name: "Veröffentlichen" }),
        )
        await fireEvent.click(screen.getByRole("button", { name: "Abbrechen" }))

        expect(publishServiceSpy).not.toHaveBeenCalled()
      })
    })
  })

  describe("Action: Withdraw", () => {
    describe("Error handling", () => {
      it("should not show error on mount", async () => {
        mockDocUnitStore(PortalPublicationStatus.PUBLISHED)
        await renderComponent({ isPublishable: true, publicationWarnings: [] })

        expect(
          screen.queryByLabelText(
            "Fehler bei der Veröffentlichung/Zurückziehung",
          ),
        ).not.toBeInTheDocument()
      })

      it("should show error when withdrawal fails", async () => {
        mockDocUnitStore(PortalPublicationStatus.PUBLISHED)
        await renderComponent({ isPublishable: true, publicationWarnings: [] })
        vi.spyOn(
          publishDocumentationUnitService,
          "withdrawDocument",
        ).mockResolvedValue({
          status: 500,
          error: { title: "Error-Titel", description: "Error-Beschreibung" },
        })

        await fireEvent.click(
          screen.getByRole("button", { name: "Zurückziehen" }),
        )

        expect(
          screen.getByLabelText(
            "Fehler bei der Veröffentlichung/Zurückziehung",
          ),
        ).toHaveTextContent("Error-TitelError-Beschreibung")
      })

      it("should not show error when withdrawal is successful", async () => {
        mockDocUnitStore(PortalPublicationStatus.PUBLISHED)
        await renderComponent({ isPublishable: true, publicationWarnings: [] })
        vi.spyOn(
          publishDocumentationUnitService,
          "withdrawDocument",
        ).mockResolvedValue({ status: 200, data: undefined })

        await fireEvent.click(
          screen.getByRole("button", { name: "Zurückziehen" }),
        )

        expect(
          screen.queryByLabelText(
            "Fehler bei der Veröffentlichung/Zurückziehung",
          ),
        ).not.toBeInTheDocument()
      })

      it("should reset previous error when publish is successful", async () => {
        mockDocUnitStore(PortalPublicationStatus.PUBLISHED)
        await renderComponent({ isPublishable: true, publicationWarnings: [] })
        vi.spyOn(publishDocumentationUnitService, "withdrawDocument")
          .mockResolvedValueOnce({
            status: 500,
            error: { title: "Error-Titel", description: "Error-Beschreibung" },
          })
          .mockResolvedValueOnce({ status: 200, data: undefined })

        // This one fails and shows error
        await fireEvent.click(
          screen.getByRole("button", { name: "Zurückziehen" }),
        )

        expect(
          screen.getByLabelText(
            "Fehler bei der Veröffentlichung/Zurückziehung",
          ),
        ).toHaveTextContent("Error-TitelError-Beschreibung")

        // This one succeeds and resets the error
        await fireEvent.click(
          screen.getByRole("button", { name: "Veröffentlichen" }),
        )

        expect(
          screen.queryByLabelText(
            "Fehler bei der Veröffentlichung/Zurückziehung",
          ),
        ).not.toBeInTheDocument()
      })
    })

    describe("Loading state", () => {
      it("should not allow publish/withdraw while withdrawing", async () => {
        mockDocUnitStore(PortalPublicationStatus.PUBLISHED)
        await renderComponent({ isPublishable: true, publicationWarnings: [] })

        const { promise: withdrawPromise, resolve: resolveWithdraw } =
          Promise.withResolvers<ServiceResponse<void>>()
        vi.spyOn(
          publishDocumentationUnitService,
          "withdrawDocument",
        ).mockImplementation(() => withdrawPromise)

        await fireEvent.click(
          screen.getByRole("button", { name: "Zurückziehen" }),
        )

        expect(
          screen.getByRole("button", { name: "Veröffentlichen" }),
        ).toBeDisabled()
        expect(
          screen.getByRole("button", { name: "Zurückziehen" }),
        ).toBeDisabled()

        // This simulates the server response
        resolveWithdraw({ status: 200, data: undefined })
        await flushPromises()

        expect(
          screen.getByRole("button", { name: "Veröffentlichen" }),
        ).toBeEnabled()
        expect(
          screen.getByRole("button", { name: "Zurückziehen" }),
        ).toBeEnabled()
      })
    })

    it("should update the status", async () => {
      const store = mockDocUnitStore(PortalPublicationStatus.PUBLISHED)
      await renderComponent({ isPublishable: true, publicationWarnings: [] })
      const loadDocUnitSpy = vi.spyOn(store, "loadDocumentUnit")
      vi.spyOn(
        publishDocumentationUnitService,
        "withdrawDocument",
      ).mockResolvedValue({ status: 200, data: undefined })

      store.documentUnit = new Decision("q834", {
        portalPublicationStatus: PortalPublicationStatus.WITHDRAWN,
      })
      await fireEvent.click(
        screen.getByRole("button", { name: "Zurückziehen" }),
      )

      expect(loadDocUnitSpy).toHaveBeenCalledOnce()
      expect(screen.getByText("Zurückgezogen")).toBeVisible()
    })

    it("should withdraw with the correct id", async () => {
      mockDocUnitStore(PortalPublicationStatus.PUBLISHED)
      await renderComponent({ isPublishable: true, publicationWarnings: [] })
      const withdrawSpy = vi
        .spyOn(publishDocumentationUnitService, "withdrawDocument")
        .mockResolvedValue({ status: 200, data: undefined })

      await fireEvent.click(
        screen.getByRole("button", { name: "Zurückziehen" }),
      )

      expect(withdrawSpy).toHaveBeenCalledWith("q834")
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

    it("shows no hint for withdrawn doc unit after successful withdraw", async () => {
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
        screen.queryByText(/Das Hochladen der Stammdaten.*2 Minuten/),
      ).not.toBeInTheDocument()
    })
  })
})

async function renderComponent(props: {
  isPublishable: boolean
  publicationWarnings: string[]
}) {
  return render(PublicationActions, { props })
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
