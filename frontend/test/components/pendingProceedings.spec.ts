import { createTestingPinia } from "@pinia/testing"
import { render, screen } from "@testing-library/vue"
import { describe, it, expect } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import PendingProceedings from "@/components/PendingProceedings.vue"
import { Decision } from "@/domain/decision"
import RelatedPendingProceeding from "@/domain/pendingProceedingReference"
import routes from "~/test-helper/routes"

const router = createRouter({
  history: createWebHistory(),
  routes: routes,
})

function renderComponent() {
  return {
    ...render(PendingProceedings, {
      props: { label: "Verknüpfung anhänges Verfahren" },
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
                      relatedPendingProceedings: [
                        new RelatedPendingProceeding({
                          documentNumber: "YYTestDoc0017",
                          court: {
                            type: "BGH",
                            label: "BGH",
                          },
                          decisionDate: "2022-02-01",
                          fileNumber: "IV R 99/99",
                        }),
                        new RelatedPendingProceeding({
                          documentNumber: "YYTestDoc0018",
                          court: {
                            type: "BFH",
                            label: "BFH",
                          },
                          decisionDate: "2025-05-05",
                          fileNumber: "AV R 77/77",
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

describe("PendingProceedings.vue", () => {
  it("should display all data", () => {
    renderComponent()

    expect(screen.getByText("Verknüpfung anhänges Verfahren")).toBeVisible()
    expect(screen.getByText(/bgh, 01\.02\.2022, iv r 99\/99 \|/i)).toBeVisible()
    expect(screen.getByRole("link", { name: "YYTestDoc0017" })).toBeVisible()
    expect(screen.getByText(/bfh, 05\.05\.2025, av r 77\/77 \|/i)).toBeVisible()
    expect(screen.getByRole("link", { name: "YYTestDoc0018" })).toBeVisible()
  })
})
