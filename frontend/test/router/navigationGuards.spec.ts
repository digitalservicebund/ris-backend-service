import router, { beforeEach } from "@/router"
import authService from "@/services/authService"

vi.mock("@/services/authService")

describe("router's auth navigation guards", () => {
  afterEach(() => {
    vi.resetAllMocks()
  })
  it("does not redirect, if not authenticated", async () => {
    const authServiceMock = vi
      .mocked(authService)
      .isAuthenticated.mockResolvedValueOnce(false)

    const result = await beforeEach(router.resolve("/caselaw"), undefined)
    expect(authServiceMock).toHaveBeenCalledTimes(1)
    expect(result).toEqual(false)
  })

  it("does redirect, if 'from' present", async () => {
    const authServiceMock = vi
      .mocked(authService)
      .isAuthenticated.mockResolvedValueOnce(false)

    const result = await beforeEach(
      router.resolve("/caselaw"),
      router.resolve("/")
    )
    expect(authServiceMock).not.toHaveBeenCalled()
    expect(result).toEqual(true)
  })

  it("does redirect, if authenticated", async () => {
    const authServiceMock = vi
      .mocked(authService)
      .isAuthenticated.mockResolvedValueOnce(true)

    const result = await beforeEach(router.resolve("/caselaw"), undefined)
    expect(authServiceMock).toHaveBeenCalledTimes(1)
    expect(result).toEqual(true)
  })
})
