import router, { beforeEach } from "@/router"
import { isAuthenticated } from "@/services/authService"

vi.mock("@/services/authService")

describe("router's auth navigation guards", () => {
  const assignMock = vi.fn()
  window.location = { assign: assignMock as Location["assign"] } as Location
  window.location.href = "/"

  afterEach(() => {
    vi.resetAllMocks()
    assignMock.mockClear()
    document.cookie = ""
  })

  it("does not redirect, if not authenticated", async () => {
    const authServiceMock = vi
      .mocked(isAuthenticated)
      .mockResolvedValueOnce(false)

    const result = await beforeEach(router.resolve("/caselaw"))
    expect(authServiceMock).toHaveBeenCalledTimes(1)
    expect(result).toEqual(false)
  })

  it("does redirect, if authenticated", async () => {
    const authServiceMock = vi
      .mocked(isAuthenticated)
      .mockResolvedValueOnce(true)

    const result = await beforeEach(router.resolve("/caselaw"))
    expect(authServiceMock).toHaveBeenCalledTimes(1)
    expect(result).toEqual(true)
  })

  it("does safe location cookie if not authenticated", async () => {
    vi.mocked(isAuthenticated).mockResolvedValueOnce(false)

    await beforeEach(router.resolve("/caselaw/documentunit/123456/categories"))
    expect(document.cookie).toEqual(
      "location=/caselaw/documentunit/123456/categories"
    )
  })

  it("does follow location cookie if authenticated", async () => {
    vi.mocked(isAuthenticated).mockResolvedValueOnce(true)
    document.cookie = "location=/norms; path=/;"

    await beforeEach(router.resolve("/"))
    expect(document.cookie).toEqual("")
    expect(window.location.href).toEqual("/norms")
  })

  it("clears session cookie, if not authenticated", async () => {
    document.cookie = "SESSION=invalid; path=/;"
    vi.mocked(isAuthenticated).mockResolvedValueOnce(false)

    await beforeEach(router.resolve("/"))
    expect(document.cookie).not.toContain("SESSION")
  })
})
