import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { http, HttpResponse } from "msw"
import { setupServer } from "msw/node"
import { describe, vi } from "vitest"
import { createRouter, createWebHistory } from "vue-router"

import CourtBranchLocation from "@/components/CourtBranchLocation.vue"
import { Court } from "@/domain/court"
import routes from "~/test-helper/routes"

const server = setupServer(
  http.get("/api/v1/caselaw/courts/branchlocations", ({ request }) => {
    const type = new URL(request.url).searchParams.get("type")
    if (type === "FG") {
      return HttpResponse.json(["Augsburg", "Kammer Ingolstadt"])
    } else {
      return HttpResponse.json([])
    }
  }),
)

function renderComponent(court?: Court, modelValue?: string) {
  const user = userEvent.setup()

  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  return {
    user,
    ...render(CourtBranchLocation, {
      props: { court: court, modelValue: modelValue ?? "" },
      global: {
        plugins: [[router]],
      },
    }),
  }
}

describe("court branch location", () => {
  beforeAll(() => server.listen())
  afterAll(() => server.close())
  beforeEach(() => {
    vi.resetModules()
    vi.resetAllMocks()
  })
  describe("without court", () => {
    it("should show placeholder, be disabled and emit undefined model value", async () => {
      const { emitted } = renderComponent()
      // Need to wait for Wertetabelle to be loaded in OnMounted
      await new Promise((resolve) => setTimeout(resolve, 0))

      expect(
        screen.getByRole("combobox", { name: "Sitz der Außenstelle" }),
      ).toHaveTextContent("Bitte auswählen")

      expect(
        screen.getByRole("combobox", { name: "Sitz der Außenstelle" }),
      ).toHaveAttribute("aria-disabled", "true")

      expect(emitted()["update:modelValue"]).toBeUndefined()
    })
  })

  describe("with court that has branch locations", () => {
    it("display options in dropdown and select one", async () => {
      const { user, emitted } = renderComponent({
        label: "FG München",
        type: "FG",
        location: "München",
      })
      // Need to wait for Wertetabelle to be loaded in OnMounted
      await new Promise((resolve) => setTimeout(resolve, 0))

      expect(
        screen.getByRole("combobox", { name: "Sitz der Außenstelle" }),
      ).toHaveTextContent("Bitte auswählen")

      await user.click(
        screen.getByRole("combobox", { name: "Sitz der Außenstelle" }),
      )
      expect(screen.getByLabelText("Augsburg")).toBeInTheDocument()
      expect(screen.getByLabelText("Kammer Ingolstadt")).toBeInTheDocument()

      await user.click(screen.getByLabelText("Augsburg"))

      expect(screen.queryByLabelText("Sitz der Außenstelle")).toHaveTextContent(
        "Augsburg",
      )
      expect(emitted()["update:modelValue"]).toEqual([["Augsburg"]])
    })
  })

  describe("with court that has no branch locations", () => {
    it("should show placeholder, be disabled and emit undefined model value", async () => {
      const { emitted } = renderComponent({
        label: "BFH",
        type: "BFH",
      })
      // Need to wait for Wertetabelle to be loaded in OnMounted
      await new Promise((resolve) => setTimeout(resolve, 0))

      expect(
        screen.getByRole("combobox", { name: "Sitz der Außenstelle" }),
      ).toHaveTextContent("Bitte auswählen")

      expect(
        screen.getByRole("combobox", { name: "Sitz der Außenstelle" }),
      ).toHaveAttribute("aria-disabled", "true")

      expect(emitted()["update:modelValue"]).toBeUndefined()
    })
  })

  describe("with model value", () => {
    it("display model value", async () => {
      renderComponent(
        {
          label: "FG München",
          type: "FG",
          location: "München",
        },
        "Augsburg",
      )
      // Need to wait for Wertetabelle to be loaded in OnMounted
      await new Promise((resolve) => setTimeout(resolve, 0))

      expect(
        screen.getByRole("combobox", { name: "Sitz der Außenstelle" }),
      ).toHaveTextContent("Augsburg")
    })
  })
})
