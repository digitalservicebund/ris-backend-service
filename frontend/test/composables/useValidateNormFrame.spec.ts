import { flushPromises } from "@vue/test-utils"
import { createPinia, setActivePinia } from "pinia"
import { useValidateNormFrame } from "@/composables/useValidateNormFrame"

vi.mock("vue", async (importActual) => {
  const vue: Record<string, unknown> = await importActual()
  return { ...vue, onUnmounted: vi.fn() }
})

describe("useValidateNormFrame", () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
  })

  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it("calls beforeCallback and callback every time the trigger is called with no data", async () => {
    const beforeCallback = vi.fn()
    const callback = vi.fn()
    const afterCallback = vi.fn()
    const { triggerValidation } = useValidateNormFrame(
      beforeCallback,
      callback,
      afterCallback,
    )
    await triggerValidation()
    vi.runAllTimers()
    await triggerValidation()
    expect(beforeCallback).toHaveBeenCalledTimes(2)
    expect(callback).toHaveBeenCalledTimes(2)
    expect(afterCallback).toHaveBeenCalledTimes(0)
  })

  it("calls beforeCallback, callback and afterCallback every time the trigger is called with data", async () => {
    const beforeCallback = vi.fn()
    const callback = vi.fn().mockResolvedValue({
      status: 200,
      data: [
        {
          code: "CODE",
          message: "error message",
          instance: "instance",
        },
      ],
    })
    const afterCallback = vi.fn()
    const { triggerValidation } = useValidateNormFrame(
      beforeCallback,
      callback,
      afterCallback,
    )
    await triggerValidation()
    vi.runAllTimers()
    await triggerValidation()
    expect(beforeCallback).toHaveBeenCalledTimes(2)
    expect(callback).toHaveBeenCalledTimes(2)
    expect(afterCallback).toHaveBeenCalledTimes(2)
  })

  it("calls beforeCallback and callback but not aftercallback because error", async () => {
    const beforeCallback = vi.fn()
    const callback = vi.fn().mockRejectedValue(undefined)
    const afterCallback = vi.fn()
    const { triggerValidation, lastValidateError } = useValidateNormFrame(
      beforeCallback,
      callback,
      afterCallback,
    )
    await triggerValidation()
    expect(beforeCallback).toHaveBeenCalledTimes(1)
    expect(callback).toHaveBeenCalledTimes(1)
    expect(lastValidateError.value).toBeTruthy()
    expect(afterCallback).toHaveBeenCalledTimes(0)
  })

  it("calls beforeCallback and callback but not aftercallback because error", async () => {
    const beforeCallback = vi.fn()
    const callback = vi.fn().mockResolvedValue({
      status: 404,
      error: {
        title: "ERROR",
      },
    })
    const afterCallback = vi.fn()
    const { triggerValidation, lastValidateError } = useValidateNormFrame(
      beforeCallback,
      callback,
      afterCallback,
    )
    await triggerValidation()
    expect(beforeCallback).toHaveBeenCalledTimes(1)
    expect(callback).toHaveBeenCalledTimes(1)
    expect(lastValidateError.value).toEqual({ title: "ERROR" })
    expect(afterCallback).toHaveBeenCalledTimes(0)
  })

  it("does not call the callback if a call is still in progress", async () => {
    let resolveCallback: (data: unknown) => void = vi.fn()
    const beforeCallback = vi.fn()
    const callback = vi
      .fn()
      .mockImplementation(
        () => new Promise((resolve) => (resolveCallback = resolve)),
      )
    const afterCallback = vi.fn()
    const { triggerValidation } = useValidateNormFrame(
      beforeCallback,
      callback,
      afterCallback,
    )

    await triggerValidation()
    vi.runAllTimers()
    await triggerValidation()
    resolveCallback(undefined)
    await flushPromises()
    vi.runAllTimers()
    await triggerValidation()

    expect(beforeCallback).toHaveBeenCalledTimes(2)
    expect(callback).toHaveBeenCalledTimes(2)
    expect(afterCallback).toHaveBeenCalledTimes(0)
  })

  it("toggles the in progress state while callback runs", async () => {
    const beforeCallback = vi.fn()
    let resolveCallback: (data: unknown) => void = vi.fn()
    const callback = vi
      .fn()
      .mockImplementation(
        () => new Promise((resolve) => (resolveCallback = resolve)),
      )
    const afterCallback = vi.fn()
    const { triggerValidation, validateIsInProgress } = useValidateNormFrame(
      beforeCallback,
      callback,
      afterCallback,
    )

    expect(validateIsInProgress.value).toBe(false)

    await triggerValidation()

    expect(validateIsInProgress.value).toBe(true)

    resolveCallback(undefined)
    await flushPromises()
    vi.runAllTimers()
    expect(validateIsInProgress.value).toBe(false)
  })

  it("also sets back the in progress state when callback throws exception", async () => {
    const beforeCallback = vi.fn()
    const callback = vi.fn().mockRejectedValue(new Error())
    const afterCallback = vi.fn()
    const { triggerValidation, validateIsInProgress } = useValidateNormFrame(
      beforeCallback,
      callback,
      afterCallback,
    )

    await triggerValidation()
    vi.runAllTimers()
    expect(validateIsInProgress.value).toBe(false)
  })

  it("resets the response error after the next successful save", async () => {
    const beforeCallback = vi.fn()
    const callback = vi.fn().mockRejectedValue(new Error())
    const afterCallback = vi.fn()
    const { triggerValidation, lastValidateError } = useValidateNormFrame(
      beforeCallback,
      callback,
      afterCallback,
    )

    expect(lastValidateError.value).toBeUndefined()

    callback.mockResolvedValueOnce({ status: 400, error: { title: "error" } })
    await triggerValidation()
    vi.runAllTimers()
    expect(lastValidateError.value).toBeDefined()

    callback.mockResolvedValueOnce({ status: 200, data: undefined })
    await triggerValidation()
    vi.runAllTimers()
    expect(lastValidateError.value).toBeUndefined()
  })
})
