import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { config } from "@vue/test-utils"
import InputText from "primevue/inputtext"
import EURLexSearch from "@/components/eurlex/EURLexSearch.vue"
import eurlexService from "@/services/eurlexService"
import { onSearchShortcutDirective } from "@/utils/onSearchShortcutDirective"

describe("eurlex search", () => {
  const eurlexMock = vi.spyOn(eurlexService, "get")
  const user = userEvent.setup()

  vi.mock("primevue/usetoast", () => ({
    useToast: () => ({ add: vi.fn() }),
  }))

  function renderComponent() {
    return render(EURLexSearch, {
      global: {
        directives: { "ctrl-enter": onSearchShortcutDirective },
        stubs: {
          EURLexSearchForm: {
            template: `
                <div>
                  <button
                    aria-label="Nach Dokumentationseinheiten suchen"
                    @click="$emit('update-page', { celex: 'celex' })"
                  >
                    Search
                  </button>
                  <button
                    aria-label="Handle service error"
                    @click="$emit('handle-service-error', { title: 'remote error title', description: 'remote error description' })"
                  >
                    Search
                  </button>
                </div>
              `,
          },
          InputMask: InputText,
        },
      },
    })
  }

  beforeEach(() => {
    eurlexMock.mockClear()
    eurlexMock.mockResolvedValue({
      data: {
        content: [],
        size: 0,
        number: 0,
        numberOfElements: 0,
        first: false,
        last: false,
        empty: true,
      },
      status: 200,
    })
  })

  afterEach(() => {
    config.global.stubs = {}
  })

  test("rendering calls the service without query parameter", async () => {
    renderComponent()

    expect(eurlexMock).toHaveBeenCalledTimes(1)
  })

  test("without filled search fields calls the service without query parameter", async () => {
    renderComponent()

    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )

    expect(eurlexMock).toHaveBeenCalledTimes(2)
  })

  test("eurlex service returns an error", async () => {
    renderComponent()
    eurlexMock.mockResolvedValue({
      error: {
        title: "error title",
        description: "error description",
      },
      status: 500,
    })

    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )

    expect(screen.getByText("error title")).toBeVisible()
    expect(screen.getByText("error description")).toBeVisible()
    expect(screen.getByText("Laden Sie die Seite bitte neu.")).toBeVisible()
  })

  test("child component emitted service returns an error", async () => {
    renderComponent()

    await user.click(screen.getByLabelText("Handle service error"))

    expect(screen.getByText("remote error title")).toBeVisible()
    expect(screen.getByText("remote error description")).toBeVisible()
    expect(screen.getByText("Laden Sie die Seite bitte neu.")).toBeVisible()
  })
})
