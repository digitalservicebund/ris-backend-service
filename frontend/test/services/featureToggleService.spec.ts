import FeatureToggleService from "@/services/featureToggleService"

describe("featureToggleService", () => {
  it("is enabled should return true", async () => {
    const response = await FeatureToggleService.isEnabled("feature-toggle")
    expect(response).toEqual({
      status: 200,
      data: true,
    })
  })
})
