import { render, screen } from "@testing-library/vue"
import NormsList from "@/components/NormsList.vue"
import { Norm } from "@/domain/Norm"

describe("norms list", () => {
  test("renders list of norms", async () => {
    const longTitle = "test"
    const guid = "123"
    const articleMock = [
      {
        guid: "123",
        title: "title",
        marker: "(1)",
        paragraphs: [{ guid: "123", marker: "(1)", text: "text" }],
      },
    ]

    const norm = new Norm(longTitle, guid, articleMock)

    render(NormsList, {
      props: {
        norms: [norm],
      },
    })

    await screen.findByText("test")
    expect(
      screen.queryByText("Keine Dokumentationseinheiten gefunden")
    ).not.toBeInTheDocument()
  })
})
