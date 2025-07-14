import { vi } from "vitest"

export const useBubbleMenuMock = () => {
  vi.mock("@tiptap/vue-3/menus", async () => {
    const actual = await vi.importActual<typeof import("@tiptap/vue-3/menus")>(
      "@tiptap/vue-3/menus",
    )

    return {
      ...actual,
      BubbleMenu: {
        name: "BubbleMenu",
        template: "<div><slot /></div>",
        props: ["editor", "options", "shouldShow"],
      },
    }
  })
}
