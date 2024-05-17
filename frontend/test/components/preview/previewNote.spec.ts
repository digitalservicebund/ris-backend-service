import { render, screen } from "@testing-library/vue"
import PreviewNote from "@/components/preview/PreviewNote.vue"

function renderComponent(note: string | null) {
  return render(PreviewNote, {
    props: {
      note: note,
    },
  })
}

describe("preview note", () => {
  test.each([null, ""])(
    "exclude note if null or empty",
    async (noteContent: string | null) => {
      renderComponent(noteContent)
      expect(screen.queryByTestId("note")).not.toBeInTheDocument()
    },
  )

  test("renders note in preview", async () => {
    const noteContent = "note preview content is displayed"
    renderComponent(noteContent)
    expect(screen.getByTestId("note")).toBeInTheDocument()
    expect(await screen.findByText(noteContent)).toBeInTheDocument()
  })
})
