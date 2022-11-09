import { expect, test } from "@playwright/test"
import normCleanCars from "./testdata/norm_clean_cars.json"

// Declare the types of your fixtures.
type MyFixtures = {
  createdGuid: string
}

export const testWithImportedNorm = test.extend<MyFixtures>({
  createdGuid: async ({ request }, use) => {
    const backendHost = process.env.E2E_BASE_URL ?? "http://localhost:8080"
    const response = await request.post(`${backendHost}/api/v1/norms`, {
      data: normCleanCars,
    })
    expect(response.ok()).toBeTruthy()
    const location = response.headers()["location"]
    const normsGuid = location.slice(location.lastIndexOf("/") + 1)

    await use(normsGuid)
  },
})
