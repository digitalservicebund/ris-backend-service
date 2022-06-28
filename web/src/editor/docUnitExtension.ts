import { Extension, mergeAttributes, Node } from "@tiptap/vue-3"

interface DocUnitOptions {
  types: string[]
}

interface RandnummerOptions {
  HTMLAttributes: Record<string, unknown>
}

declare module "@tiptap/core" {
  interface Commands<ReturnType> {
    heading: {
      setRandnummer: (attributes: { number: number }) => ReturnType
    }
  }
}

export const DocUnitParagraphExtension = Extension.create<DocUnitOptions>({
  name: "docUnit",
  addOptions() {
    return {
      types: ["paragraph"],
    }
  },
  addGlobalAttributes() {
    return [
      {
        types: this.options.types,
        attributes: {
          fontSize: {
            default: null,
            parseHTML: (element) =>
              element.style.fontSize.replace(/['"]+/g, ""),
            renderHTML: (attributes) => {
              if (!attributes.fontSize) {
                return {}
              }

              return {
                style: `font-size: ${attributes.fontSize}`,
              }
            },
          },
          textAlign: {
            default: null,
            parseHTML: (element) =>
              element.style.textAlign.replace(/['"]+/g, ""),
            renderHTML: (attributes) => {
              if (!attributes.textAlign) {
                return {}
              }

              return {
                style: `text-align: ${attributes.textAlign}`,
              }
            },
          },
          fontWeight: {
            default: null,
            parseHTML: (element) =>
              element.style.fontWeight.replace(/['"]+/g, ""),
            renderHTML: (attributes) => {
              if (!attributes.fontWeight) {
                return {}
              }

              return {
                style: `font-weight: ${attributes.fontWeight}`,
              }
            },
          },
          textDecoration: {
            default: null,
            parseHTML: (element) =>
              element.style.textDecoration.replace(/['"]+/g, ""),
            renderHTML: (attributes) => {
              if (!attributes.textDecoration) {
                return {}
              }

              return {
                style: `text-decoration: ${attributes.textDecoration}`,
              }
            },
          },
          color: {
            default: null,
            parseHTML: (element) => element.style.color.replace(/['"]+/g, ""),
            renderHTML: (attributes) => {
              if (!attributes.color) {
                return {}
              }

              return {
                style: `color: ${attributes.color}`,
              }
            },
          },
        },
      },
    ]
  },
})

export const DocUnitRunExtension = Extension.create<DocUnitOptions>({
  name: "docUnit",
  addOptions() {
    return {
      types: ["span"],
    }
  },
  addGlobalAttributes() {
    return [
      {
        types: this.options.types,
        attributes: {
          fontSize: {
            default: null,
            parseHTML: (element) =>
              element.style.fontSize.replace(/['"]+/g, ""),
            renderHTML: (attributes) => {
              if (!attributes.fontSize) {
                return {}
              }

              return {
                style: `font-size: ${attributes.fontSize}`,
              }
            },
          },
          textAlign: {
            default: null,
            parseHTML: (element) =>
              element.style.textAlign.replace(/['"]+/g, ""),
            renderHTML: (attributes) => {
              if (!attributes.textAlign) {
                return {}
              }

              return {
                style: `text-align: ${attributes.textAlign}`,
              }
            },
          },
          fontWeight: {
            default: null,
            parseHTML: (element) =>
              element.style.fontWeight.replace(/['"]+/g, ""),
            renderHTML: (attributes) => {
              if (!attributes.fontWeight) {
                return {}
              }

              return {
                style: `font-weight: ${attributes.fontWeight}`,
              }
            },
          },
          textDecoration: {
            default: null,
            parseHTML: (element) =>
              element.style.textDecoration.replace(/['"]+/g, ""),
            renderHTML: (attributes) => {
              if (!attributes.textDecoration) {
                return {}
              }

              return {
                style: `text-decoration: ${attributes.textDecoration}`,
              }
            },
          },
          color: {
            default: null,
            parseHTML: (element) => element.style.color.replace(/['"]+/g, ""),
            renderHTML: (attributes) => {
              if (!attributes.color) {
                return {}
              }

              return {
                style: `color: ${attributes.color}`,
              }
            },
          },
        },
      },
    ]
  },
})

export const Randnummer = Node.create<RandnummerOptions>({
  name: "randnummer",
  priority: 1000,
  group: "block",
  content: "inline*",
  addAttributes() {
    return { number: "1" }
  },
  addOptions() {
    return {
      HTMLAttributes: {
        style: "display: flex; margin-bottom: 10px",
      },
    }
  },
  parseHTML() {
    return [{ tag: "randnummer" }]
  },
  renderHTML({ HTMLAttributes }) {
    return [
      "div",
      mergeAttributes(this.options.HTMLAttributes, HTMLAttributes),
      [
        "div",
        { style: "padding-top: 10px; padding-left: 10px; min-width: 40px;" },
        HTMLAttributes.number.toString(),
      ],
      ["div", 0],
    ]
  },
  addCommands() {
    return {
      setRandnummer:
        (attributes) =>
        ({ commands }) => {
          return commands.setNode(this.name, attributes)
        },
    }
  },
})
