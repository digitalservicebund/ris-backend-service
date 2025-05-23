import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { config } from "@vue/test-utils"
import InputText from "primevue/inputtext"
import EURLexSearchForm from "@/components/eurlex/EURLexSearchForm.vue"
import { onSearchShortcutDirective } from "@/utils/onSearchShortcutDirective"

describe("eurlex search", () => {
  const user = userEvent.setup()

  function renderComponent() {
    return render(EURLexSearchForm, {
      global: {
        directives: { "ctrl-enter": onSearchShortcutDirective },
      },
    })
  }

  beforeEach(() => {
    config.global.stubs = {
      InputMask: InputText,
    }
  })

  afterEach(() => {
    config.global.stubs = {}
  })

  test("without filled search fields calls the service without query parameter", async () => {
    const { emitted } = renderComponent()

    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )

    expect(emitted("updatePage")[0]).toStrictEqual([
      0,
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
    ])
  })

  test("with value in file number input calls the service with only file number query parameter", async () => {
    const { emitted } = renderComponent()

    await user.type(screen.getByLabelText("Aktenzeichen Suche"), "file-number")
    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )

    expect(emitted("updatePage")[0]).toStrictEqual([
      0,
      "file-number",
      undefined,
      undefined,
      undefined,
      undefined,
    ])
  })

  test("with value in celex input calls the service with only celex query parameter", async () => {
    const { emitted } = renderComponent()

    await user.type(screen.getByLabelText("CELEX-Nummer Suche"), "celex")
    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )

    expect(emitted("updatePage")[0]).toStrictEqual([
      0,
      undefined,
      "celex",
      undefined,
      undefined,
      undefined,
    ])
  })

  test("with value in court input calls the service with only court query parameter", async () => {
    const { emitted } = renderComponent()

    await user.type(screen.getByLabelText("Gerichtstyp Suche"), "court-type")
    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )

    expect(emitted("updatePage")[0]).toStrictEqual([
      0,
      undefined,
      undefined,
      "court-type",
      undefined,
      undefined,
    ])
  })

  test("with value in start date input calls the service with only start date query parameter", async () => {
    const { emitted } = renderComponent()

    await user.type(
      screen.getByLabelText("Entscheidungsdatum Suche Start"),
      "01.01.2010",
    )
    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )

    expect(emitted("updatePage")[0]).toStrictEqual([
      0,
      undefined,
      undefined,
      undefined,
      "2010-01-01",
      undefined,
    ])
  })

  test("with value in end date input calls the service with only end date query parameter", async () => {
    const { emitted } = renderComponent()

    await user.type(
      screen.getByLabelText("Entscheidungsdatum Suche Ende"),
      "31.12.2010",
    )
    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )

    expect(emitted("updatePage")[0]).toStrictEqual([
      0,
      undefined,
      undefined,
      undefined,
      undefined,
      "2010-12-31",
    ])
  })
})
