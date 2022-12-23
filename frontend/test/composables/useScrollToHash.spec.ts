import { nextTick, ref } from "vue"
import { useScrollToHash } from "@/composables/useScrollToHash"

describe("text editor", async () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it("scrolls element into view if element with hash exists", async () => {
    const hashElement = { scrollIntoView: vi.fn() }
    document.querySelector = vi.fn((hash: string) =>
      hash === "test-hash" ? hashElement : undefined
    )

    useScrollToHash(ref("test-hash"))
    vi.runAllTimers()

    expect(document.querySelector).toHaveBeenCalledWith("test-hash")
    expect(hashElement.scrollIntoView).toHaveBeenCalledOnce()
  })

  it("does not scroll if no element exists for this hash", async () => {
    const hashElement = { scrollIntoView: vi.fn() }
    document.querySelector = vi.fn((hash: string) =>
      hash === "test-hash" ? hashElement : undefined
    )

    useScrollToHash(ref("unknown-hash"))
    vi.runAllTimers()

    expect(document.querySelector).toHaveBeenCalledWith("unknown-hash")
    expect(hashElement.scrollIntoView).not.toHaveBeenCalled()
  })

  it("does nothing if given route has no hash", async () => {
    document.querySelector = vi.fn()

    useScrollToHash(ref(undefined))
    vi.runAllTimers()

    expect(document.querySelector).not.toHaveBeenCalled()
  })

  it("detects new hash on route changes", async () => {
    const hashElement = { scrollIntoView: vi.fn() }
    document.querySelector = vi.fn().mockReturnValue(hashElement)
    const route = ref<string | undefined>(undefined)

    useScrollToHash(route)
    vi.runAllTimers()

    expect(document.querySelector).not.toHaveBeenCalled()
    expect(hashElement.scrollIntoView).not.toHaveBeenCalled()

    route.value = "yolo"
    await nextTick()
    vi.runAllTimers()

    expect(document.querySelector).toHaveBeenCalledOnce()
    expect(hashElement.scrollIntoView).toHaveBeenCalledOnce()
  })
})
