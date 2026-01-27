import { createTestingPinia } from "@pinia/testing"
import { render, screen } from "@testing-library/vue"
import { describe, it, expect } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import CountriesOfOrigin from "@/components/CountriesOfOrigin.vue"
import CountryOfOrigin from "@/domain/countryOfOrigin"
import { Decision } from "@/domain/decision"
import routes from "~/test-helper/routes"

const router = createRouter({
  history: createWebHistory(),
  routes: routes,
})

function renderComponent() {
  return {
    ...render(CountriesOfOrigin, {
      props: { label: "Herkunftsland" },
      global: {
        plugins: [
          [router],
          [
            createTestingPinia({
              initialState: {
                docunitStore: {
                  documentUnit: new Decision("123", {
                    documentNumber: "foo",
                    contentRelatedIndexing: {
                      countriesOfOrigin: [
                        new CountryOfOrigin({
                          legacyValue: "legacy value",
                        }),
                        new CountryOfOrigin({
                          country: {
                            identifier: "RE-07-DEU",
                            text: "Deutschland",
                            norms: [],
                            hasChildren: false,
                            children: [],
                          },
                        }),
                      ],
                    },
                  }),
                },
              },
            }),
          ],
        ],
      },
    }),
  }
}

describe("CountriesOfOrigin.vue", () => {
  it("should display label", () => {
    renderComponent()

    expect(screen.getByText("Herkunftsland")).toBeVisible()
    expect(screen.getByText("Altwert")).toBeVisible()
    expect(screen.getByText("legacy value")).toBeVisible()
    expect(screen.getByText("RE-07-DEU Deutschland")).toBeVisible()
  })
})
