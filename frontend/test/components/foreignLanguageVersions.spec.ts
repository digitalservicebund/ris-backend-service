import { createTestingPinia } from "@pinia/testing"
import { render, screen } from "@testing-library/vue"
import { describe, it, expect } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import ForeignLanguageVersions from "@/components/ForeignLanguageVersions.vue"
import { Decision } from "@/domain/decision"
import ForeignLanguageVersion from "@/domain/foreignLanguageVersion"
import routes from "~/test-helper/routes"

const router = createRouter({
  history: createWebHistory(),
  routes: routes,
})

function renderComponent() {
  return {
    ...render(ForeignLanguageVersions, {
      props: { label: "Fremdsprachige Fassung" },
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
                      foreignLanguageVersions: [
                        new ForeignLanguageVersion({
                          languageCode: { id: "en", label: "Englisch" },
                          link: "https://link-to-translation.en",
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

describe("ForeignLanguageVersions.vue", () => {
  it("should display label", () => {
    renderComponent()

    expect(screen.getByText("Fremdsprachige Fassung")).toBeVisible()
    expect(screen.getByText("Englisch:")).toBeVisible()
    expect(screen.getByText("https://link-to-translation.en")).toBeVisible()
  })
})
