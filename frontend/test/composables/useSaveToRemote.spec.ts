import { createTestingPinia } from "@pinia/testing"
import { flushPromises } from "@vue/test-utils"
import { setActivePinia } from "pinia"
import { useSaveToRemote } from "@/composables/useSaveToRemote"
import errorMessages from "@/i18n/errors.json"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

vi.mock("vue", async (importActual) => {
  const vue: Record<string, unknown> = await importActual()
  return { ...vue, onUnmounted: vi.fn() }
})

function mockDocumentUnitStore(callback = vi.fn()) {
  const documentUnitStore = useDocumentUnitStore()
  documentUnitStore.updateDocumentUnit = callback

  return documentUnitStore
}

describe("useSaveToRemote", () => {
  beforeEach(() => {
    vi.useFakeTimers()
    setActivePinia(createTestingPinia())
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it("calls the callback every time the trigger is called", async () => {
    const documentUnitStore = mockDocumentUnitStore()
    const { triggerSave } = useSaveToRemote()

    await triggerSave()
    expect(documentUnitStore.updateDocumentUnit).toHaveBeenCalledTimes(1)

    await triggerSave()
    expect(documentUnitStore.updateDocumentUnit).toHaveBeenCalledTimes(2)
  })

  it("does not call the callback if a call is still in progress", async () => {
    let resolveCallback: (data: unknown) => void = vi.fn()
    const callback = vi
      .fn()
      .mockImplementation(
        () => new Promise((resolve) => (resolveCallback = resolve)),
      )

    const documentUnitStore = mockDocumentUnitStore(callback)
    const { triggerSave } = useSaveToRemote()

    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    triggerSave()
    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    triggerSave()
    resolveCallback(undefined)
    await flushPromises()
    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    triggerSave()

    expect(documentUnitStore.updateDocumentUnit).toHaveBeenCalledTimes(2)
  })

  it("toggles the in progress state while callback runs", async () => {
    let resolveCallback: (data: unknown) => void = vi.fn()
    const callback = vi
      .fn()
      .mockImplementation(
        () => new Promise((resolve) => (resolveCallback = resolve)),
      )

    mockDocumentUnitStore(callback)
    const { triggerSave, saveIsInProgress } = useSaveToRemote()

    expect(saveIsInProgress.value).toBe(false)

    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    triggerSave()

    expect(saveIsInProgress.value).toBe(true)

    resolveCallback(undefined)
    await flushPromises()

    expect(saveIsInProgress.value).toBe(false)
  })

  it("also sets back the in progress state when callback throws exception", async () => {
    const callback = vi.fn().mockRejectedValue(new Error())
    mockDocumentUnitStore(callback)
    const { triggerSave, saveIsInProgress } = useSaveToRemote()

    await triggerSave()

    expect(saveIsInProgress.value).toBe(false)
  })

  it("sets the response error if callback returns one", async () => {
    const callback = vi
      .fn()
      .mockResolvedValue({ status: 400, error: { title: "error" } })
    mockDocumentUnitStore(callback)
    const { triggerSave, lastSaveError } = useSaveToRemote()

    await triggerSave()

    expect(lastSaveError.value).toEqual({ title: "error" })
  })

  it("sets connection error if callback throws exception one", async () => {
    const callback = vi.fn().mockRejectedValue(new Error())
    mockDocumentUnitStore(callback)
    const { triggerSave, lastSaveError } = useSaveToRemote()

    await triggerSave()

    expect(lastSaveError.value).toEqual({ title: "Verbindung fehlgeschlagen" })
  })

  it("resets the response error after the next successful save", async () => {
    mockDocumentUnitStore()
    const { triggerSave, lastSaveError } = useSaveToRemote()

    expect(lastSaveError.value).toBeUndefined()

    mockDocumentUnitStore(
      vi.fn().mockResolvedValueOnce({ status: 400, error: { title: "error" } }),
    )

    await triggerSave()

    expect(lastSaveError.value).toBeDefined()

    mockDocumentUnitStore(
      vi.fn().mockResolvedValueOnce({ status: 200, data: undefined }),
    )
    await triggerSave()

    expect(lastSaveError.value).toBeUndefined()
  })

  it("sets the last save on date only after each successfully callback call", async () => {
    mockDocumentUnitStore()
    const { triggerSave, formattedLastSavedOn } = useSaveToRemote()

    expect(formattedLastSavedOn.value).toBeUndefined()

    vi.setSystemTime(60_000)
    mockDocumentUnitStore(
      vi.fn().mockResolvedValueOnce({ status: 200, data: undefined }),
    )
    await triggerSave()

    expect(formattedLastSavedOn.value).toBe("01:01")

    vi.setSystemTime(120_000)
    mockDocumentUnitStore(
      vi.fn().mockResolvedValueOnce({ status: 400, error: { title: "error" } }),
    )
    await triggerSave()

    expect(formattedLastSavedOn.value).toBe("01:01")

    vi.setSystemTime(180_000)
    mockDocumentUnitStore(
      vi.fn().mockResolvedValueOnce({ status: 200, data: undefined }),
    )
    await triggerSave()

    expect(formattedLastSavedOn.value).toBe("01:03")
  })

  it("automatically triggers the callback once per set interval", async () => {
    const documentUnitStore = mockDocumentUnitStore()
    useSaveToRemote(30000)

    vi.advanceTimersToNextTimer()
    await flushPromises()
    expect(documentUnitStore.updateDocumentUnit).toHaveBeenCalledTimes(1)

    vi.advanceTimersToNextTimer()
    await flushPromises()
    expect(documentUnitStore.updateDocumentUnit).toHaveBeenCalledTimes(2)

    vi.advanceTimersToNextTimer()
    await flushPromises()
    expect(documentUnitStore.updateDocumentUnit).toHaveBeenCalledTimes(3)
  })

  it("does not reset error if callback did not change anything", async () => {
    mockDocumentUnitStore()
    const { triggerSave, formattedLastSavedOn, lastSaveError } =
      useSaveToRemote()

    mockDocumentUnitStore(
      vi
        .fn()
        .mockResolvedValueOnce({ status: 400, error: { title: "error" } })
        .mockResolvedValueOnce({ status: 304, data: undefined }),
    )

    // first save attempt with error response
    await triggerSave()
    expect(formattedLastSavedOn.value).toBeUndefined()
    expect(lastSaveError.value).toEqual({ title: "error" })

    // second save attepmpt, nothing changed
    await triggerSave()
    expect(formattedLastSavedOn.value).toBeUndefined()
    expect(lastSaveError.value).toEqual({ title: "error" })
  })

  it("shows alert one time when PATCH_SIZE_TOO_BIG error occurs", async () => {
    const alertSpy = vi.spyOn(window, "alert").mockImplementation(() => {})
    const store = useDocumentUnitStore()

    const updateSpy = vi.spyOn(store, "updateDocumentUnit")
    updateSpy.mockRejectedValue(
      new Error(errorMessages.PATCH_SIZE_TOO_BIG.title),
    )

    const { triggerSave } = useSaveToRemote()
    // Trigger save more than once to make sure the alert is just shown once
    await triggerSave()
    await triggerSave()

    expect(alertSpy).toHaveBeenCalledOnce()
    expect(alertSpy).toHaveBeenCalledWith(
      errorMessages.PATCH_SIZE_TOO_BIG.title +
        ": " +
        errorMessages.PATCH_SIZE_TOO_BIG.description,
    )
  })
})
