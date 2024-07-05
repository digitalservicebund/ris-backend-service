import { render, screen } from "@testing-library/vue"
import { previewLayoutInjectionKey } from "@/components/preview/constants"
import PreviewNote from "@/components/preview/PreviewNote.vue"

function renderComponent(note?: string) {
  return render(PreviewNote, {
    props: {
      note: note,
    },
    global: {
      provide: {
        [previewLayoutInjectionKey as symbol]: "wide",
      },
    },
  })
}

describe("preview note", () => {
  test.each([undefined, ""])(
    "exclude note if null or empty",
    async (noteContent?: string) => {
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
