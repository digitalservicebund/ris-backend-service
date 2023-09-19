import { render, screen } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import CitationDateInputGroup from "@/components/citationDate/CitationDateInputGroup.vue"

type CitationDateInputGroupProps = InstanceType<
  typeof CitationDateInputGroup
>["$props"]

function renderComponent(props?: CitationDateInputGroupProps) {
  const effectiveProps = {
    modelValue: props?.modelValue ?? {},
    "onUpdate:modelValue": props?.["onUpdate:modelValue"],
  }
  return render(CitationDateInputGroup, { props: effectiveProps })
}

describe("Citation date/year field", () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
  })
  it("Shows date label and passed date value", () => {
    renderComponent({ modelValue: { DATE: ["01.01.2020"] } })

    const citationDateInputField = screen.getByLabelText(
      "Zitierdatum",
    ) as HTMLInputElement

    expect(citationDateInputField).toBeInTheDocument()
    expect(citationDateInputField).toBeVisible()
    expect(citationDateInputField).toHaveValue("01.01.2020")
  })

  it("Shows year label and passed year value", async () => {
    renderComponent({ modelValue: { YEAR: ["2013"] } })

    const citationYearInputField = screen.getByLabelText(
      "Zitierdatum Jahresangabe",
    ) as HTMLInputElement

    expect(citationYearInputField).toBeInTheDocument()
    expect(citationYearInputField).toBeVisible()
    expect(citationYearInputField).toHaveValue("2013")
  })
})
