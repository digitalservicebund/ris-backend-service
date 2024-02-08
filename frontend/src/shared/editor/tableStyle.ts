import { Extension } from "@tiptap/core"

export const TableStyle = Extension.create({
  addGlobalAttributes() {
    return [
      {
        types: ["table", "tableRow", "tableCell", "tableHeader,"],
        attributes: {
          style: {
            renderHTML: (attributes) => {
              return {
                style: attributes.style,
              }
            },
          },
        },
      },
    ]
  },
})
