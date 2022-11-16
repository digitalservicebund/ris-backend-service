import { fireEvent, render, screen } from "@testing-library/vue"
import { ref } from "vue"
import SaveButton from "@/components/SaveButton.vue"
import { useSaveToRemote } from "@/composables/useSaveToRemote"
import { ResponseError } from "@/services/httpClient"

vi.mock("@/composables/useSaveToRemote")

describe("SaveButton", () => {
  it("data is being saved", async () => {
    vi.mocked(useSaveToRemote).mockReturnValue({
      saveIsInProgress: ref(true),
      triggerSave: vi.fn(),
      lastSaveError: ref<ResponseError | undefined>(undefined),
      lastSavedOn: ref<Date | undefined>(undefined),
    })
    const getByText = await renderAndClick()
    expect(getByText("Daten werden gespeichert")).toBeVisible()
  })

  it("data was saved", async () => {
    vi.mocked(useSaveToRemote).mockReturnValue({
      saveIsInProgress: ref(false),
      triggerSave: vi.fn(),
      lastSaveError: ref<ResponseError | undefined>(undefined),
      lastSavedOn: ref<Date | undefined>(new Date()),
    })
    const getByText = await renderAndClick()
    expect(getByText("Zuletzt gespeichert um", { exact: false })).toBeVisible()
  })

  it("error when saving", async () => {
    vi.mocked(useSaveToRemote).mockReturnValue({
      saveIsInProgress: ref(false),
      triggerSave: vi.fn(),
      lastSaveError: ref<ResponseError | undefined>({
        title: "test",
      } as ResponseError),
      lastSavedOn: ref<Date | undefined>(new Date()),
    })
    const getByText = await renderAndClick()
    expect(getByText("Fehler beim Speichern")).toBeVisible()
  })
})

async function renderAndClick() {
  const { emitted, getByText } = render(SaveButton, {
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
  expect(emitted().fetchNorm).toBeTruthy()
  return getByText
}
