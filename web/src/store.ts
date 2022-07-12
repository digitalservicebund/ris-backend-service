import { defineStore } from "pinia"

export const useLayoutStore = defineStore("layoutStateStore", {
  state: () => ({
    showOdocPanel: false, // only on Rubriken page
    showSidebar: true,
  }),
})
