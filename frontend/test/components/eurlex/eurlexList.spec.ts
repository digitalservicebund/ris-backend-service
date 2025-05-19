import { render, screen } from "@testing-library/vue"
import EURLexList from "@/components/eurlex/EURLexList.vue"
import { Page } from "@/components/Pagination.vue"
import EURLexResult from "@/domain/eurlex"

function renderComponent(entries: Page<EURLexResult>) {
  return render(EURLexList, {
    props: {
      pageEntries: entries,
    },
  })
}

describe("eurlex list", () => {
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

    expect(await screen.findByText("celex")).toBeInTheDocument()
    expect(await screen.findByText("court-type")).toBeInTheDocument()
    expect(await screen.findByText("court-location")).toBeInTheDocument()
    expect(await screen.findByText("01.05.2000")).toBeInTheDocument()
    expect(await screen.findByText("file-number")).toBeInTheDocument()
    expect(await screen.findByText("01.06.2010")).toBeInTheDocument()
    expect(await screen.findByLabelText("Öffne Vorschau")).toBeVisible()
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

    expect(await screen.findByText("celex")).toBeInTheDocument()
    expect(await screen.findByText("court-type")).toBeInTheDocument()
    expect(await screen.findByText("court-location")).toBeInTheDocument()
    expect(await screen.findByText("01.05.2000")).toBeInTheDocument()
    expect(await screen.findByText("file-number")).toBeInTheDocument()
    expect(await screen.findByText("01.06.2010")).toBeInTheDocument()
    expect(screen.queryByLabelText("Öffne Vorschau")).not.toBeInTheDocument()
  })
})
