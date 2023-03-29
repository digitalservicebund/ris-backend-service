import { fireEvent, render, screen } from "@testing-library/vue"
import { ref } from "vue"
import { ResponseError } from "@/services/httpClient"
import SaveButton from "@/shared/components/input/SaveButton.vue"
import { useSaveToRemote } from "@/shared/composables/useSaveToRemote"

vi.mock("@/shared/composables/useSaveToRemote")

describe("SaveButton", () => {
  it("renders with info message that data is being saved", async () => {
    vi.mocked(useSaveToRemote).mockReturnValue({
      saveIsInProgress: ref(true),
      triggerSave: vi.fn(),
      lastSaveError: ref<ResponseError | undefined>(undefined),
      lastSavedOn: ref<Date | undefined>(undefined),
      timer: setInterval(() => vi.fn()),
    })
    await renderAndClick()
    expect(screen.getByText("Daten werden gespeichert")).toBeVisible()
  })

  it("renders with info message that data was last saved on a specific time", async () => {
    vi.mocked(useSaveToRemote).mockReturnValue({
      saveIsInProgress: ref(false),
      triggerSave: vi.fn(),
      lastSaveError: ref<ResponseError | undefined>(undefined),
      lastSavedOn: ref<Date | undefined>(new Date()),
      timer: setInterval(() => vi.fn()),
    })
    await renderAndClick()
    expect(
      screen.getByText("Zuletzt gespeichert um", { exact: false })
    ).toBeVisible()
  })

  it("renders with info message that there was an error when saving data", async () => {
    vi.mocked(useSaveToRemote).mockReturnValue({
      saveIsInProgress: ref(false),
      triggerSave: vi.fn(),
      lastSaveError: ref<ResponseError | undefined>({
        title: "test",
      } as ResponseError),
      lastSavedOn: ref<Date | undefined>(new Date()),
      timer: setInterval(() => vi.fn()),
    })
    await renderAndClick()
    expect(screen.getByText("Fehler beim Speichern")).toBeVisible()
  })
})

async function renderAndClick() {
  render(SaveButton, {
    props: {
      ariaLabel: "Foo",
      serviceCallback: vi.fn(),
    },
  })

  const saveButton = screen.getByRole("button")
  expect(saveButton).toBeInTheDocument()
  expect(saveButton.textContent?.replace(/\s/g, "")).toEqual("Speichern")
  expect(saveButton).toHaveAttribute("aria-label", "Foo")
  await fireEvent.click(saveButton)
}
