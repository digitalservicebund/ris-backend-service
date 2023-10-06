import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnitSearchEntryForm from "@/components/DocumentUnitSearchEntryForm.vue"

function renderComponent(options?: { isLoading: boolean }) {
  // eslint-disable-next-line testing-library/await-async-events
  const props = {
    isLoading: options?.isLoading ? options?.isLoading : false,
  }

  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "/caselaw/documentUnit/new",
        name: "new",
        component: {},
      },
      {
        path: "/",
        name: "home",
        component: {},
      },
      {
        path: "/caselaw/documentUnit/:documentNumber/categories",
        name: "caselaw-documentUnit-documentNumber-categories",
        component: {},
      },
      {
        path: "/caselaw/documentUnit/:documentNumber/files",
        name: "caselaw-documentUnit-documentNumber-files",
        component: {},
      },
    ],
  })

  return {
    ...render(DocumentUnitSearchEntryForm, {
      props,
      global: { plugins: [router] },
    }),
  }
}

describe("Documentunit Search", () => {
  const user = userEvent.setup()

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

  test("click on 'Suche zurücksetzen' emits 'resetSearchResults'", async () => {
    const { emitted } = renderComponent()
    await user.type(
      screen.getByLabelText("Entscheidungsdatum Suche"),
      "22.02.2001",
    )
    await user.click(screen.getByLabelText("Suche zurücksetzen"))
    expect(emitted().resetSearchResults).toBeTruthy()
  })

  test("click on 'Ergebnisse anzeigen' emits search event", async () => {
    const { emitted } = renderComponent()

    const input = screen.getByLabelText("Entscheidungsdatum Suche")

    await user.type(input, "22.02.2001")
    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )

    expect(emitted().search).toBeTruthy()
  })

  test("click on 'Nur meine Dokstelle' renders 'Nur fehlerhafte Dokumentationseinheiten' checkbox", async () => {
    renderComponent()
    expect(
      screen.queryByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    ).not.toBeInTheDocument()
    expect(screen.getByLabelText("Nur meine Dokstelle Filter")).toBeVisible()
    await user.click(screen.getByLabelText("Nur meine Dokstelle Filter"))
    expect(
      screen.getByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    ).toBeVisible()
  })

  test("click on 'Nur meine Dokstelle' renders 'Nur fehlerhafte Dokumentationseinheiten' checkbox", async () => {
    renderComponent()
    expect(
      screen.queryByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    ).not.toBeInTheDocument()
    expect(screen.getByLabelText("Nur meine Dokstelle Filter")).toBeVisible()
    await user.click(screen.getByLabelText("Nur meine Dokstelle Filter"))
    expect(
      screen.getByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    ).toBeVisible()
  })
})
