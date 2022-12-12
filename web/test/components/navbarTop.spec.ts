import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import NavbarTop from "../../src/components/NavbarTop.vue"

describe("navbar top", () => {
  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "",
        name: "caselaw",
        component: {},
      },
      {
        path: "",
        name: "norms",
        component: {},
      },
    ],
  })

  test("navbar top should be rendered without error", async () => {
    render(NavbarTop, {
      props: {},
      global: { plugins: [router] },
    })

    expect(screen.getByText("Rechtsinformationen")).toBeInTheDocument()
    expect(screen.getByText("Normen")).toBeInTheDocument()
    expect(screen.getByText("Rechtsinformationen")).toBeInTheDocument()
    expect(screen.getByText("des Bundes")).toBeInTheDocument()
  })
})
