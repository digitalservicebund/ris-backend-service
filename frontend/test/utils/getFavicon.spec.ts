import { getFavicon } from "@/utils/getFavicon"

describe("getFavicon", () => {
  it.each(["staging", "uat", "production"])(
    "returns correct favicon for %s",
    (environment) => {
      const favicon = getFavicon(environment)

      expect(favicon).toBe(`/src/assets/favicon-${environment}.svg`)
    },
  )

  it("falls back to production favicon", () => {
    const favicon = getFavicon("foo")

    expect(favicon).toBe(`/src/assets/favicon-production.svg`)
  })
})
