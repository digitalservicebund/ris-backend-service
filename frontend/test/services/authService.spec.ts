import { isAuthenticated } from "@/services/authService"
import httpClient from "@/services/httpClient"

vi.mock("@/services/httpClient")

describe("authService", () => {
  it("should return false if 401", async () => {
    vi.mocked(httpClient).get.mockResolvedValueOnce({
      status: 401,
      error: { title: "Not authenticated" },
    })

    const result = await isAuthenticated()
    expect(result).toEqual(false)
  })

  it("should return false if 403", async () => {
    vi.mocked(httpClient).get.mockResolvedValueOnce({
      status: 403,
      error: { title: "Not authorized" },
    })

    const result = await isAuthenticated()
    expect(result).toEqual(false)
  })

  it("should return true if 200", async () => {
    vi.mocked(httpClient).get.mockResolvedValueOnce({
      status: 200,
      data: { title: "welcome" },
    })

    const result = await isAuthenticated()
    expect(result).toEqual(true)
  })
})
