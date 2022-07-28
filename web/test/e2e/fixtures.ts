import { test } from "@playwright/test"

export const testWithDocUnit = test.extend<{ documentNumber: string }>({
  documentNumber: async ({ request }, use) => {
    const response = await request.post("api/v1/docunits", {
      data: { documentationCenterAbbreviation: "foo", documentType: "X" },
    })
    const { uuid, documentnumber: documentNumber } = await response.json()

    await use(documentNumber)

    await request.delete(`api/v1/docunits/${uuid}`)
  },
})
