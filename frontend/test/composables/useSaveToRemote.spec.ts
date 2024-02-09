import { flushPromises } from "@vue/test-utils"
import { useSaveToRemote } from "@/composables/useSaveToRemote"

vi.mock("vue", async (importActual) => {
  const vue: Record<string, unknown> = await importActual()
  return { ...vue, onUnmounted: vi.fn() }
})

describe("useSaveToRemote", () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it("calls the callback every time the trigger is called", async () => {
    const callback = vi.fn()
    const { triggerSave } = useSaveToRemote(callback)

    await triggerSave()
    expect(callback).toHaveBeenCalledTimes(1)

    await triggerSave()
    expect(callback).toHaveBeenCalledTimes(2)
  })

  it("does not call the callback if a call is still in progress", async () => {
    let resolveCallback: (data: unknown) => void = vi.fn()
    const callback = vi
      .fn()
      .mockImplementation(
        () => new Promise((resolve) => (resolveCallback = resolve)),
      )
    const { triggerSave } = useSaveToRemote(callback)

    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    triggerSave()
    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    triggerSave()
    resolveCallback(undefined)
    await flushPromises()
    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    triggerSave()

    expect(callback).toHaveBeenCalledTimes(2)
  })

  it("toggles the in progress state while callback runs", async () => {
    let resolveCallback: (data: unknown) => void = vi.fn()
    const callback = vi
      .fn()
      .mockImplementation(
        () => new Promise((resolve) => (resolveCallback = resolve)),
      )
    const { triggerSave, saveIsInProgress } = useSaveToRemote(callback)

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
    const { triggerSave, saveIsInProgress } = useSaveToRemote(callback)

    await triggerSave()

    expect(saveIsInProgress.value).toBe(false)
  })

  it("sets the response error if callback returns one", async () => {
    const callback = vi
      .fn()
      .mockResolvedValue({ status: 400, error: { title: "error" } })
    const { triggerSave, lastSaveError } = useSaveToRemote(callback)

    await triggerSave()

    expect(lastSaveError.value).toEqual({ title: "error" })
  })

  it("sets connection error if callback throws exception one", async () => {
    const callback = vi.fn().mockRejectedValue(new Error())
    const { triggerSave, lastSaveError } = useSaveToRemote(callback)

    await triggerSave()

    expect(lastSaveError.value).toEqual({ title: "Verbindung fehlgeschlagen" })
  })

  it("resets the response error after the next successful save", async () => {
    const callback = vi.fn()
    const { triggerSave, lastSaveError } = useSaveToRemote(callback)

    expect(lastSaveError.value).toBeUndefined()

    callback.mockResolvedValueOnce({ status: 400, error: { title: "error" } })
    await triggerSave()

    expect(lastSaveError.value).toBeDefined()

    callback.mockResolvedValueOnce({ status: 200, data: undefined })
    await triggerSave()

    expect(lastSaveError.value).toBeUndefined()
  })

  it("sets the last save on date only after each successfully callback call", async () => {
    const callback = vi.fn()
    const { triggerSave, formattedLastSavedOn } = useSaveToRemote(callback)

    expect(formattedLastSavedOn.value).toBeUndefined()

    vi.setSystemTime(60_000)
    callback.mockResolvedValueOnce({ status: 200, data: undefined })
    await triggerSave()

    expect(formattedLastSavedOn.value).toBe("00:01")

    vi.setSystemTime(120_000)
    callback.mockResolvedValueOnce({ status: 400, error: { title: "error" } })
    await triggerSave()

    expect(formattedLastSavedOn.value).toBe("00:01")

    vi.setSystemTime(180_000)
    callback.mockResolvedValueOnce({ status: 200, data: undefined })
    await triggerSave()

    expect(formattedLastSavedOn.value).toBe("00:03")
  })

  it("automatically triggers the callback once per set interval", async () => {
    const callback = vi.fn()
    useSaveToRemote(callback, 30000)

    vi.advanceTimersToNextTimer()
    await flushPromises()
    expect(callback).toHaveBeenCalledTimes(1)

    vi.advanceTimersToNextTimer()
    await flushPromises()
    expect(callback).toHaveBeenCalledTimes(2)

    vi.advanceTimersToNextTimer()
    await flushPromises()
    expect(callback).toHaveBeenCalledTimes(3)
  })
})
