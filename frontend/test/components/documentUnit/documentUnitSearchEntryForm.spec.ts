import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { config } from "@vue/test-utils"
import InputText from "primevue/inputtext"
import { expect } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnitSearchEntryForm from "@/components/DocumentUnitSearchEntryForm.vue"
import { onSearchShortcutDirective } from "@/utils/onSearchShortcutDirective"

async function renderComponent(options?: { isLoading: boolean }) {
  const props = {
    isLoading: options?.isLoading ? options?.isLoading : false,
  }

  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "/",
        name: "home",
        component: {},
      },
    ],
  })

  return {
    ...render(DocumentUnitSearchEntryForm, {
      props,
      global: {
        directives: {
          "ctrl-enter": onSearchShortcutDirective,
        },
        plugins: [router],
      },
    }),
    user: userEvent.setup(),
    router: router,
  }
}

describe("Documentunit search form", () => {
  beforeEach(() => {
    // InputMask evaluates cursor position on every keystroke, however, our browser vitest setup does not
    // implement any layout-related functionality, meaning the required functions for cursor offset
    // calculation are missing. When we deal with typing in date/ year / time inputs, we can mock it with
    // TextInput, as we only need the string and do not need to test the actual mask behaviour.
    config.global.stubs = {
      InputMask: InputText,
    }
  })

  afterEach(() => {
    // Mock needs to be reset (and can not be mocked globally) because InputMask has interdependencies
    // with the PrimeVue select component. When testing the select components with InputMask
    // mocked globally, they fail due to these dependencies.
    config.global.stubs = {}
  })
  test("renders correctly", async () => {
    await renderComponent()
    ;[
      "Aktenzeichen Suche",
      "Gerichtstyp Suche",
      "Gerichtsort Suche",
      "Entscheidungsdatum Suche",
      "Dokumentnummer Suche",
      "Status Suche",
    ].forEach((label) => expect(screen.getByLabelText(label)).toBeVisible())

    expect(
      screen.getByLabelText("Nur meine Dokstelle Filter"),
    ).toBeInTheDocument()
  })

  test("click on 'Suche zurücksetzen' emits 'resetSearchResults'", async () => {
    const { emitted, user } = await renderComponent()
    await user.type(
      screen.getByLabelText("Entscheidungsdatum Suche"),
      "22.02.2001",
    )
    await user.click(screen.getByLabelText("Suche zurücksetzen"))
    expect(emitted().resetSearchResults).toBeTruthy()
  })

  test("click on 'Ergebnisse anzeigen' emits search event", async () => {
    const { emitted, user } = await renderComponent()

    await user.type(
      screen.getByLabelText("Entscheidungsdatum Suche"),
      "22.02.2001",
    )
    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )

    expect(emitted().search).toBeTruthy()
  })

  test("click on 'Ergebnisse anzeigen' with the same search entry, emits search event again", async () => {
    const { emitted, user, router } = await renderComponent()
    // we need to reset the query in order to make sure the test works together with the other tests
    await router.push({ path: "/", query: {} })

    await user.type(
      screen.getByLabelText("Entscheidungsdatum Suche"),
      "23.03.2003",
    )
    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )
    expect(emitted().search.length).toBe(1)

    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )

    expect(emitted().search.length).toBe(2)
  })

  test("click on 'Nur meine Dokstelle' renders 'Nur fehlerhafte Dokumentationseinheiten' checkbox", async () => {
    const { user } = await renderComponent()
    expect(
      screen.queryByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    ).not.toBeInTheDocument()
    expect(
      screen.getByLabelText("Nur meine Dokstelle Filter"),
    ).toBeInTheDocument()
    await user.click(screen.getByLabelText("Nur meine Dokstelle Filter"))
    expect(
      screen.getByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    ).toBeInTheDocument()
  })

  test("click on 'Nur meine Dokstelle' renders scheduled publication fields", async () => {
    const { user } = await renderComponent()
    expect(
      screen.queryByLabelText("jDV Übergabedatum Suche"),
    ).not.toBeInTheDocument()
    expect(screen.queryByLabelText("Terminiert Filter")).not.toBeInTheDocument()

    await user.click(screen.getByLabelText("Nur meine Dokstelle Filter"))

    expect(screen.getByLabelText("jDV Übergabedatum Suche")).toBeVisible()
    expect(screen.getByLabelText("Terminiert Filter")).toBeInTheDocument()
  })

  test("click on 'Nur meine Dokstelle' renders 'Dublettenverdacht' checkbox", async () => {
    const { user } = await renderComponent()
    expect(
      screen.queryByLabelText("Dokumentationseinheiten mit Dublettenverdacht"),
    ).not.toBeInTheDocument()

    await user.click(screen.getByLabelText("Nur meine Dokstelle Filter"))

    expect(
      screen.getByLabelText("Dokumentationseinheiten mit Dublettenverdacht"),
    ).toBeInTheDocument()
  })

  test("unchecking 'Nur meine Dokstelle' removes scheduled publication input", async () => {
    const { user } = await renderComponent()

    // 1) After enabling Nur meine Dokstelle, the inputs can be filled
    await user.click(screen.getByLabelText("Nur meine Dokstelle Filter"))
    await user.type(
      screen.getByLabelText("jDV Übergabedatum Suche"),
      "10.10.2020",
    )
    await user.click(screen.getByLabelText("Terminiert Filter"))

    expect(screen.getByLabelText("jDV Übergabedatum Suche")).toHaveValue(
      "10.10.2020",
    )
    expect(screen.getByLabelText("Terminiert Filter")).toBeChecked()

    // 2) When disabling Nur meine Dokstelle, the inputs are gone
    await user.click(screen.getByLabelText("Nur meine Dokstelle Filter"))

    expect(
      screen.queryByLabelText("jDV Übergabedatum Suche"),
    ).not.toBeInTheDocument()
    expect(screen.queryByLabelText("Terminiert Filter")).not.toBeInTheDocument()

    // 3) When enabling Nur meine Dokstelle again, the inputs are in empty default state
    await user.click(screen.getByLabelText("Nur meine Dokstelle Filter"))

    expect(screen.getByLabelText("jDV Übergabedatum Suche")).toHaveValue("")
    expect(screen.getByLabelText("Terminiert Filter")).not.toBeChecked()
  })

  test("search by pressing ctrl & enter", async () => {
    const { emitted, router, user } = await renderComponent()
    await router.push("/") // reset for whatever reason

    await user.type(
      screen.getByLabelText("Entscheidungsdatum Suche"),
      "22.02.2002",
    )

    await user.keyboard("{Control>}{Enter}")

    expect(emitted("search")).toBeTruthy()
  })
})
