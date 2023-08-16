import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import DocumentUnitSearch from "@/components/DocumentUnitSearch.vue"

describe("Documentunit Search", () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
  })

  test("renders correctly", async () => {
    render(DocumentUnitSearch)
    expect(
      screen.getByLabelText("Dokumentnummer oder Aktenzeichen Suche"),
    ).toBeVisible()
    expect(screen.getByLabelText("Gerichtstyp Suche")).toBeVisible()
    expect(screen.getByLabelText("Gerichtsort Suche")).toBeVisible()
    expect(screen.getByLabelText("Entscheidungsdatum Suche")).toBeVisible()
    expect(screen.getByLabelText("Status Suche")).toBeVisible()
    expect(screen.getByLabelText("Nur meine Dokstelle Filter")).toBeVisible()
  })

  test("click on 'Nur meine Dokstelle' renders 'Nur fehlerhafte Dokumentationseinheiten' checkbox", async () => {
    const user = userEvent.setup()
    render(DocumentUnitSearch)
    expect(
      screen.queryByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    ).not.toBeInTheDocument()
    expect(screen.getByLabelText("Nur meine Dokstelle Filter")).toBeVisible()
    await user.click(screen.getByLabelText("Nur meine Dokstelle Filter"))
    expect(
      screen.getByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    ).toBeVisible()
  })

  test("click on 'Suche zurücksetzen' resets all input values", async () => {
    const user = userEvent.setup()
    render(DocumentUnitSearch)

    await user.click(screen.getByLabelText("Nur meine Dokstelle Filter"))
    await user.type(screen.getByLabelText("Gerichtstyp Suche"), "AG")
    await user.type(screen.getByLabelText("Gerichtsort Suche"), "Aachen")
    await user.type(
      screen.getByLabelText("Entscheidungsdatum Suche"),
      "01.02.2022",
    )
    await user.selectOptions(screen.getByLabelText("Status Suche"), "PUBLISHED")

    expect(screen.getByLabelText("Gerichtstyp Suche")).toHaveValue("AG")
    expect(screen.getByLabelText("Gerichtsort Suche")).toHaveValue("Aachen")
    expect(screen.getByLabelText("Entscheidungsdatum Suche")).toHaveValue(
      "01.02.2022",
    )
    expect(screen.getByLabelText("Status Suche")).toHaveValue("PUBLISHED")
    expect(screen.getByLabelText("Nur meine Dokstelle Filter")).toBeChecked()
    expect(
      screen.getByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    ).toBeVisible()

    await user.click(screen.getByLabelText("Suche zurücksetzen"))
    expect(screen.getByLabelText("Gerichtstyp Suche")).toHaveValue("")
    expect(screen.getByLabelText("Gerichtsort Suche")).toHaveValue("")
    expect(screen.getByLabelText("Entscheidungsdatum Suche")).toHaveValue("")
    expect(screen.getByLabelText("Status Suche")).toHaveValue("")
    expect(
      screen.getByLabelText("Nur meine Dokstelle Filter"),
    ).not.toBeChecked()
    expect(
      screen.queryByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    ).not.toBeInTheDocument()
  })
})
