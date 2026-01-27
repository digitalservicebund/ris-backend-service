import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { config } from "@vue/test-utils"
import InputText from "primevue/inputtext"
import { createRouter, createWebHistory } from "vue-router"
import InboxSearch from "@/components/inbox/shared/InboxSearch.vue"
import routes from "~/test-helper/routes"

function renderComponent() {
  const user = userEvent.setup()

  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  return {
    user,
    ...render(InboxSearch, {
      global: {
        plugins: [router],
      },
    }),
  }
}

describe("inbox search", () => {
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

  test("renders all input fields", async () => {
    renderComponent()

    expect(screen.getByLabelText("Aktenzeichen Suche")).toBeInTheDocument()
    expect(screen.getByLabelText("Gerichtstyp Suche")).toBeInTheDocument()
    expect(screen.getByLabelText("Gerichtsort Suche")).toBeInTheDocument()
    expect(screen.getByLabelText("Dokumentnummer Suche")).toBeInTheDocument()
  })

  test("emits 'search' event when valid search is submitted", async () => {
    const { user, emitted } = renderComponent()

    await user.type(screen.getByLabelText("Aktenzeichen Suche"), "12345")
    await user.keyboard("{Control>}{Enter}")

    expect(emitted().search).toBeTruthy()
  })

  test("resets form and emits 'resetSearchResults' on reset", async () => {
    const { user, emitted } = renderComponent()

    await user.type(screen.getByLabelText("Aktenzeichen Suche"), "12345")
    await user.click(screen.getByLabelText("Suche zurücksetzen"))

    expect(emitted().resetSearchResults).toBeTruthy()
    expect(screen.getByLabelText("Aktenzeichen Suche")).toHaveValue("")
  })

  test("validates date range and shows error if end date is before start date", async () => {
    const { user } = renderComponent()

    const startDateInput = screen.getByLabelText("Entscheidungsdatum Suche")
    const endDateInput = screen.getByLabelText("Entscheidungsdatum Suche Ende")

    await user.type(startDateInput, "01.01.2025")
    await user.type(endDateInput, "01.01.2023")

    await user.keyboard("{Control>}{Enter}")

    expect(
      screen.getByText("Enddatum darf nicht vor Startdatum liegen"),
    ).toBeInTheDocument()
  })
})
