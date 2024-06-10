import { useFavicon } from "@/composables/useFavicon"

describe("useFavicon", () => {
  it.each(["staging", "uat", "production"])(
    "returns correct favicon for %s",
    (environment) => {
      const favicon = useFavicon(environment)

      expect(favicon.value).toBe(`/src/assets/favicon-${environment}.svg`)
    },
  )

  it("falls back to production favicon", () => {
    const favicon = useFavicon("foo")

    expect(favicon.value).toBe(`/src/assets/favicon-production.svg`)
  })
})
