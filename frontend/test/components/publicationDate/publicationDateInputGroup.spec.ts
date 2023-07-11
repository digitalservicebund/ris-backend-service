import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import PublicationDateInputGroup from "@/components/publicationDate/PublicationDateInputGroup.vue"
import { Metadata } from "@/domain/Norm"

function renderComponent(options?: {
  ariaLabel?: string
  modelValue?: Metadata
}) {
  const user = userEvent.setup()
  const props = {
    ariaLabel: options?.ariaLabel ?? "aria-label",
    modelValue: options?.modelValue ?? {},
  }
  const utils = render(PublicationDateInputGroup, { props })
  return { user, props, ...utils }
}

describe("Publication date/year field", () => {
  it("Shows date label and passed date value", () => {
    renderComponent({ modelValue: { DATE: ["05.05.2021"] } })

    const publicationDateInputField = screen.getByLabelText(
      "Veröffentlichungsdatum Datum"
    ) as HTMLInputElement

    expect(publicationDateInputField).toBeInTheDocument()
    expect(publicationDateInputField).toBeVisible()
    expect(publicationDateInputField).toHaveValue("05.05.2021")
  })

  it("Shows year label and passed year value", async () => {
    renderComponent({ modelValue: { YEAR: ["1998"] } })

    const publicationYearInputField = screen.getByLabelText(
      "Veröffentlichungsdatum Jahresangabe"
    ) as HTMLInputElement

    expect(publicationYearInputField).toBeInTheDocument()
    expect(publicationYearInputField).toBeVisible()
    expect(publicationYearInputField).toHaveValue("1998")
  })
})
