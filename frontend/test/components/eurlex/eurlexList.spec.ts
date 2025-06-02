import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { http, HttpResponse } from "msw"
import { setupServer } from "msw/node"
import EURLexList from "@/components/eurlex/EURLexList.vue"
import { Page } from "@/components/Pagination.vue"
import DocumentationOffice from "@/domain/documentationOffice"
import EURLexResult from "@/domain/eurlex"
import service from "@/services/documentUnitService"

const addToastMock = vi.fn()
vi.mock("primevue/usetoast", () => ({
  useToast: () => ({ add: addToastMock }),
}))

function renderComponent(entries: Page<EURLexResult>) {
  return render(EURLexList, {
    props: {
      pageEntries: entries,
    },
  })
}

const server = setupServer(
  http.get("/api/v1/caselaw/documentationoffices", () => {
    const documentationOffice: DocumentationOffice = {
      id: "id",
      abbreviation: "DS",
    }
    return HttpResponse.json([documentationOffice])
  }),
)

describe("eurlex list", () => {
  const documentationUnitServiceMock = vi.spyOn(
    service,
    "createNewOutOfEurlexDecision",
  )
  documentationUnitServiceMock.mockResolvedValue({
    data: ["doc-number"],
    status: 201,
  })
  const user = userEvent.setup()

  beforeAll(() => server.listen())
  afterAll(() => server.close())

  test("renders the entry in the result page", async () => {
    renderComponent({
      content: [
        {
          ecli: "ecli",
          celex: "celex",
          courtType: "court-type",
          courtLocation: "court-location",
          date: "2000-05-01",
          title: "title",
          fileNumber: "file-number",
          publicationDate: "2010-06-01",
          uri: "uri",
          htmlLink: "html-link",
        },
      ],
      size: 1,
      number: 1,
      numberOfElements: 1,
      totalElements: 1,
      totalPages: 1,
      first: true,
      last: true,
      empty: false,
    })

    expect(screen.getByText("celex")).toBeVisible()
    expect(screen.getByText("court-type")).toBeVisible()
    expect(screen.getByTestId("court-location").innerHTML).toBe("-")
    expect(screen.getByText("01.05.2000")).toBeVisible()
    expect(screen.getByText("file-number")).toBeVisible()
    expect(screen.getByText("01.06.2010")).toBeVisible()
    expect(screen.getByLabelText("Öffne Vorschau")).toBeVisible()
  })

  test("renders entry without html link doesn't show preview button", async () => {
    renderComponent({
      content: [
        {
          ecli: "ecli",
          celex: "celex",
          courtType: "court-type",
          courtLocation: "court-location",
          date: "2000-05-01",
          title: "title",
          fileNumber: "file-number",
          publicationDate: "2010-06-01",
          uri: "uri",
        },
      ],
      size: 1,
      number: 1,
      numberOfElements: 1,
      totalElements: 1,
      totalPages: 1,
      first: true,
      last: true,
      empty: false,
    })

    expect(screen.getByText("celex")).toBeVisible()
    expect(screen.getByText("court-type")).toBeVisible()
    expect(screen.getByTestId("court-location").innerHTML).toBe("-")
    expect(screen.getByText("01.05.2000")).toBeVisible()
    expect(screen.getByText("file-number")).toBeVisible()
    expect(screen.getByText("01.06.2010")).toBeVisible()
    expect(screen.queryByLabelText("Öffne Vorschau")).not.toBeInTheDocument()
  })

  test(
    "select entry, select a doc office and press Zuweisen should call service to generate " +
      "documentation out out of eurlex decisions, deselect all checkboxes, emit assignment and show toast",
    async () => {
      const { emitted } = renderComponent({
        content: [
          {
            ecli: "ecli",
            celex: "celex",
            courtType: "court-type",
            courtLocation: "court-location",
            date: "2000-05-01",
            title: "title",
            fileNumber: "file-number",
            publicationDate: "2010-06-01",
            uri: "uri",
          },
        ],
        size: 1,
        number: 1,
        numberOfElements: 1,
        totalElements: 1,
        totalPages: 1,
        first: true,
        last: true,
        empty: false,
      })
      const user = userEvent.setup()

      await user.click(screen.getAllByRole("checkbox")[0])
      await user.click(screen.getByLabelText("Dokumentationsstelle auswählen"))
      await user.click(screen.getByText("DS"))
      await user.click(screen.getByLabelText("Dokumentationsstelle zuweisen"))

      expect(documentationUnitServiceMock).toHaveBeenCalledWith({
        celexNumbers: ["celex"],
        documentationOffice: {
          id: "id",
          abbreviation: "DS",
        },
      })

      // clear earlier service error info modal
      expect(emitted("handleServiceError")[0]).toStrictEqual([undefined])
      screen.getAllByRole("checkbox").forEach((checkbox) => {
        expect(checkbox).not.toBeChecked()
      })

      expect(addToastMock).toHaveBeenCalledExactlyOnceWith({
        detail:
          "Die Dokumentationseinheit wurde der Dokumentationsstelle DS zugewiesen.",
        life: 5000,
        severity: "success",
        summary: "Zuweisen erfolgreich",
      })

      expect(emitted()["assign"]).toBeTruthy()
    },
  )

  test(
    "select entry, select a doc office, press Zuweisen and call service throws " +
      "error should deselect all checkboxes, emit handleServiceError",
    async () => {
      const { emitted } = renderComponent({
        content: [
          {
            ecli: "ecli",
            celex: "celex",
            courtType: "court-type",
            courtLocation: "court-location",
            date: "2000-05-01",
            title: "title",
            fileNumber: "file-number",
            publicationDate: "2010-06-01",
            uri: "uri",
          },
        ],
        size: 1,
        number: 1,
        numberOfElements: 1,
        totalElements: 1,
        totalPages: 1,
        first: true,
        last: true,
        empty: false,
      })
      documentationUnitServiceMock.mockResolvedValue({
        error: { title: "error title", description: "error description 1" },
        status: 500,
      })

      await user.click(screen.getAllByRole("checkbox")[0])
      await user.click(screen.getByLabelText("Dokumentationsstelle auswählen"))
      await user.click(screen.getByText("DS"))
      await user.click(screen.getByLabelText("Dokumentationsstelle zuweisen"))

      expect(emitted("handleServiceError")[0]).toStrictEqual([
        {
          title: "error title",
          description: "error description 1",
        },
      ])

      screen.getAllByRole("checkbox").forEach((checkbox) => {
        expect(checkbox).not.toBeChecked()
      })
    },
  )
})
