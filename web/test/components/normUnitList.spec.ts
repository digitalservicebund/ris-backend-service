import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import NormsList from "@/components/NormsList.vue"

describe("norms list", () => {
  test("renders list of norms", async () => {
    const articleMock = [
      {
        guid: "123",
        title: "title",
        marker: "(1)",
        paragraphs: [{ guid: "123", marker: "(1)", text: "text" }],
      },
    ]

    const norm = { longTitle: "test", guid: "123", articles: articleMock }

    render(NormsList, {
      props: {
        norms: [norm],
      },
      global: {
        plugins: [
          createRouter({
            history: createWebHistory(),
            routes: [
              {
                path: "",
                name: "norms-norm-:normGuid",
                component: {},
              },
            ],
          }),
        ],
      },
    })

    await screen.findByText("test")
    expect(
      screen.queryByText("Keine Dokumentationseinheiten gefunden")
    ).not.toBeInTheDocument()
  })
})
