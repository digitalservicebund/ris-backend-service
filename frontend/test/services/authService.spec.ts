import service from "@/services/authService"
import httpClient from "@/services/httpClient"

vi.mock("@/services/httpClient")

describe("authService", () => {
  const assignMock = vi.fn()
  window.location = { assign: assignMock as Location["assign"] } as Location

  beforeEach(() => {
    vi.resetAllMocks()
    assignMock.mockClear()
    window.location.href = "/"
  })

  it("should return false and redirect if 401", async () => {
    const httpClientGet = vi.mocked(httpClient).get.mockResolvedValueOnce({
      status: 401,
      error: { title: "Not authenticated" },
    })

    const result = await service.isAuthenticated()

    expect(httpClientGet).toHaveBeenCalledOnce()
    expect(result).toEqual(false)
    expect(window.location.href).toEqual("/oauth2/authorization/oidcclient")
  })

  it("should return false and redirect if 403", async () => {
    const httpClientGet = vi.mocked(httpClient).get.mockResolvedValueOnce({
      status: 403,
      error: { title: "Not authorized" },
    })

    const result = await service.isAuthenticated()

    expect(httpClientGet).toHaveBeenCalledOnce()
    expect(result).toEqual(false)
    expect(window.location.href).toEqual("/oauth2/authorization/oidcclient")
  })

  it("should return true and not redirect if 200", async () => {
    const httpClientGet = vi.mocked(httpClient).get.mockResolvedValueOnce({
      status: 200,
      data: { title: "welcome" },
    })

    const result = await service.isAuthenticated()

    expect(httpClientGet).toHaveBeenCalledOnce()
    expect(result).toEqual(true)
    expect(window.location.href).toEqual("/")
  })
})
