import { flushPromises } from "@vue/test-utils"
import { useSaveToRemote } from "@/shared/composables/useSaveToRemote"

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
        () => new Promise((resolve) => (resolveCallback = resolve))
      )
    const { triggerSave } = useSaveToRemote(callback)

    triggerSave()
    triggerSave()
    resolveCallback(undefined)
    await flushPromises()
    triggerSave()

    expect(callback).toHaveBeenCalledTimes(2)
  })

  it("toggles the in progress state while callback runs", async () => {
    let resolveCallback: (data: unknown) => void = vi.fn()
    const callback = vi
      .fn()
      .mockImplementation(
        () => new Promise((resolve) => (resolveCallback = resolve))
      )
    const { triggerSave, saveIsInProgress } = useSaveToRemote(callback)

    expect(saveIsInProgress.value).toBe(false)

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
    const { triggerSave, lastSavedOn } = useSaveToRemote(callback)

    expect(lastSavedOn.value).toBeUndefined()

    vi.setSystemTime(1000)
    callback.mockResolvedValueOnce({ status: 200, data: undefined })
    await triggerSave()

    expect(lastSavedOn.value?.getTime()).toBe(1000)

    vi.setSystemTime(2000)
    callback.mockResolvedValueOnce({ status: 400, error: { title: "error" } })
    await triggerSave()

    expect(lastSavedOn.value?.getTime()).toBe(1000)

    vi.setSystemTime(3000)
    callback.mockResolvedValueOnce({ status: 200, data: undefined })
    await triggerSave()

    expect(lastSavedOn.value?.getTime()).toBe(3000)
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
