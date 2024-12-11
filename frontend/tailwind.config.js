/** @type {import('tailwindcss').Config} */
import styleDictionary from "@digitalservice4germany/style-dictionary/tailwind"
import plugins from "@digitalservice4germany/angie"

export default {
  content: ["./src/**/*.{html,vue,js,ts}"],
  presets: [styleDictionary],
  plugins: [plugins],

  theme: {
    fontWeight: {
      normal: 400,
      bold: 700,
    },
    fontSize: {
      base: "1rem",
      10: "0.625rem",
      11: "0.688rem",
      14: "0.875rem",
      16: "1rem",
      18: "1.125rem",
      20: "1.25rem",
      22: "1.375rem",
      24: "1.5rem",
      28: "1.75rem",
      30: "1.875rem",
      32: "2rem",
      48: "3rem",
      56: "3.5rem",
      64: "4rem",
      72: "4.5rem",
    },
    lineHeight: {
      default: "1.625 !important",
      13: "0.8125rem",
      16: "1rem",
      18: "1.125rem",
      20: "1.25rem",
      22: "1.375rem",
      24: "1.5rem",
      26: "1.625rem",
      30: "1.875rem",
      36: "2.25rem",
      40: "2.5rem",
      68: "4.25rem",
    },
    extend: {
      screens: {
        print: { raw: "print" },
        screen: { raw: "screen" },
      },
      borderWidth: {
        1: "1px",
        3: "3px",
      },
      boxShadow: {
        blue: "inset 0 0 0 2px rgba(0, 75, 118, 1)",
        focus: "inset 0 0 0 4px rgba(0, 75, 118, 1)",
        hover: "inset 0 0 0 4px rgba(0, 75, 118, 1)",
        button: "inset 0 0 0 2px",
      },
      borderRadius: {
        10: "0.625rem",
      },
      colors: {
        background: "rgba(184, 189, 195, 0.5)",
        neutral: {
          20: "#f4f5f7",
          700: "#253858",
        },
      },
      spacing: {
        unset: "unset",
        3: "0.167rem",
        38: "2.375rem",
        480: "30rem",
        640: "40rem",
      },
    },
  },
}
