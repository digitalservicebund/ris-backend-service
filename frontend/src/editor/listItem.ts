import { ListItem } from "@tiptap/extension-list-item"

const addListIndex = (element: HTMLLIElement) => {
  const isFristChildP =
    !!element.firstChild &&
    (element.firstChild as HTMLElement).tagName.toLowerCase() === "p"
  const isSecondChildP =
    !!element.childNodes[1] &&
    (element.childNodes[1] as HTMLElement).tagName.toLowerCase() === "p"

  if (isFristChildP && !isSecondChildP) {
    const index = document.createElement("p")
    index.style.display = "table-cell"
    index.textContent = ""
    element.insertBefore(index, element.firstChild)
  }
}

export const CustomListItem = ListItem.extend({
  addAttributes() {
    return {
      ...this.parent?.(),
      style: {
        default: null,
        parseHTML: (element) => {
          if (element.tagName.toLowerCase() === "li") {
            addListIndex(element as HTMLLIElement)
          }
          return element.getAttribute("style")
        },
        renderHTML: (attributes) => {
          return { style: attributes.style }
        },
      },
    }
  },
})
