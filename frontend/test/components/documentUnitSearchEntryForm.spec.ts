import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import { nextTick } from "vue"
import DocumentUnitSearchEntryForm from "@/components/DocumentUnitSearchEntryForm.vue"
import { PublicationState } from "@/domain/documentUnit"
import DocumentUnitSearchInput from "@/domain/documentUnitSearchInput"

function renderComponent(options?: { modelValue?: DocumentUnitSearchInput }) {
  const user = userEvent.setup()
  const props = {
    modelValue: new DocumentUnitSearchInput({ ...options?.modelValue }),
  }

  return {
    user,
    ...render(DocumentUnitSearchEntryForm, { props }),
  }
}

describe("Documentunit Search", () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
  })

  test("renders correctly", async () => {
    renderComponent()
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
    const { user } = renderComponent()
    expect(
      screen.queryByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    ).not.toBeInTheDocument()
    expect(screen.getByLabelText("Nur meine Dokstelle Filter")).toBeVisible()
    await user.click(screen.getByLabelText("Nur meine Dokstelle Filter"))
    expect(
      screen.getByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    ).toBeVisible()
  })

  it("renders search entry if given", () => {
    renderComponent({
      modelValue: {
        myDocOfficeOnly: true,
        documentNumberOrFileNumber: "fileNumber",
        court: {
          label: "t",
          type: "court type",
          location: "court location",
        },
        decisionDate: "2022-01-31T23:00:00.000Z",
        decisionDateEnd: "2023-01-31T23:00:00.000Z",
        status: {
          publicationStatus: PublicationState.UNPUBLISHED,
          withError: false,
        },
      },
    })

    const dateField = screen.getByLabelText(
      "Dokumentnummer oder Aktenzeichen Suche",
    )
    expect(dateField).toHaveValue("fileNumber")
  })

  test("click on 'Suche zurücksetzen' resets all input values", async () => {
    const { user } = renderComponent({
      modelValue: {
        myDocOfficeOnly: true,
        documentNumberOrFileNumber: "fileNumber",
        court: {
          label: "t",
          type: "court type",
          location: "court location",
        },
        decisionDate: "2022-01-31T23:00:00.000Z",
        decisionDateEnd: "2023-01-31T23:00:00.000Z",
        status: {
          publicationStatus: PublicationState.UNPUBLISHED,
          withError: false,
        },
      },
    })

    expect(screen.getByLabelText("Gerichtsort Suche")).toHaveValue(
      "court location",
    )
    expect(screen.getByLabelText("Gerichtstyp Suche")).toHaveValue("court type")
    expect(screen.getByLabelText("Entscheidungsdatum Suche")).toHaveValue(
      "31.01.2022",
    )
    expect(screen.getByLabelText("Entscheidungsdatum Suche Ende")).toHaveValue(
      "31.01.2023",
    )

    expect(screen.getByLabelText("Status Suche")).toHaveValue("UNPUBLISHED")
    expect(screen.getByLabelText("Nur meine Dokstelle Filter")).toBeChecked()
    expect(
      screen.getByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    ).toBeVisible()

    await user.click(screen.getByLabelText("Suche zurücksetzen"))
    expect(screen.queryByText(/documentNumber/)).not.toBeInTheDocument()

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

  test("click on 'Nur meine Dokstelle' renders 'Nur fehlerhafte Dokumentationseinheiten' checkbox", async () => {
    const { user } = renderComponent()
    expect(
      screen.queryByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    ).not.toBeInTheDocument()
    expect(screen.getByLabelText("Nur meine Dokstelle Filter")).toBeVisible()
    await user.click(screen.getByLabelText("Nur meine Dokstelle Filter"))
    expect(
      screen.getByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    ).toBeVisible()
  })

  test("click on 'Ergebnisse anzeigen' emits search event", async () => {
    const { user, emitted } = renderComponent()
    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )
    expect(emitted().search).toBeTruthy()
  })

  test("emits search event if only 1st date input given", async () => {
    const { user, emitted } = renderComponent({
      modelValue: {
        decisionDate: "2022-01-31T23:00:00.000Z",
      },
    })
    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )
    expect(emitted().search).toBeTruthy()
  })

  test("shows results for a range if 1st and 2nd date input given", async () => {
    const { user, emitted } = renderComponent({
      modelValue: {
        decisionDate: "2022-01-31T23:00:00.000Z",
        decisionDateEnd: "2023-01-31T23:00:00.000Z",
      },
    })
    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )
    expect(emitted().search).toBeTruthy()
  })

  test("shows error if 2nd date input given but 1st empty", async () => {
    renderComponent({
      modelValue: {
        decisionDateEnd: "2023-01-31T23:00:00.000Z",
      },
    })

    await nextTick()
    expect(screen.getByText("Startdatum fehlt")).toBeVisible()
  })

  test("shows error when 2nd date before 1st date", async () => {
    const { user } = renderComponent({
      modelValue: {
        decisionDate: "2023-01-31T23:00:00.000Z",
        decisionDateEnd: "2022-01-31T23:00:00.000Z",
      },
    })

    await nextTick()
    //TODO: the error should already occur before submitting search
    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )
    expect(
      screen.getByText("Enddatum darf nich vor Startdatum liegen"),
    ).toBeVisible()
  })

  test("shows error date input is invalid", async () => {
    renderComponent()
    const input = screen.getByLabelText("Entscheidungsdatum Suche")

    await userEvent.type(input, "29.02.2001")
    expect(screen.getByText("Kein valides Datum")).toBeVisible()
  })

  test("shows two errors when 1st and 2nd date input is invalid", async () => {
    renderComponent()

    const dateInput = screen.getByLabelText("Entscheidungsdatum Suche")
    const dateInputEnd = screen.getByLabelText("Entscheidungsdatum Suche Ende")

    await userEvent.type(dateInput, "29.02.2001")
    await userEvent.type(dateInputEnd, "29.02.2002")
    expect(screen.queryAllByText("Kein valides Datum").length).toBe(2)
  })

  test("shows error when clicking submit search button with errors in the search form", async () => {
    const { user } = renderComponent({
      modelValue: {
        decisionDateEnd: "2022-01-31T23:00:00.000Z",
      },
    })

    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )
    expect(screen.getByText("Startdatum fehlt")).toBeVisible()
    expect(screen.getByText("Fehler in Suchkriterien")).toBeVisible()
  })
})
