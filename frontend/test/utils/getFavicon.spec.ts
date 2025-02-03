import localFavicon from "@/assets/favicon-local.svg"
import productionFavicon from "@/assets/favicon-production.svg"
import stagingFavicon from "@/assets/favicon-staging.svg"
import uatFavicon from "@/assets/favicon-uat.svg"
import { getFavicon } from "@/utils/getFavicon"

describe("getFavicon", () => {
  it.each([
    { environment: "staging", expected: stagingFavicon },
    { environment: "uat", expected: uatFavicon },
    { environment: "production", expected: productionFavicon },
  ])(
    "returns correct favicon for %s",
    ({ environment, expected }: { environment: string; expected: string }) => {
      vi.stubEnv("MODE", "production")
      const favicon = getFavicon(environment)

      expect(favicon).toEqual(expected)
      vi.unstubAllEnvs()
    },
  )

  it("falls back to production favicon", () => {
    const favicon = getFavicon("foo")

    expect(favicon).toBe(productionFavicon)
  })

  it("returns correct favicon for local", () => {
    vi.stubEnv("MODE", "development")

    const favicon = getFavicon("staging")

    expect(favicon).toBe(localFavicon)
    vi.unstubAllEnvs()
  })
})
