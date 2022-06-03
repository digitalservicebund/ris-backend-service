import { Extension, mergeAttributes, Node } from "@tiptap/vue-3"

interface DocUnitOptions {
  types: string[],
}

export const DocUnitParagraphExtension = Extension.create<DocUnitOptions>({
  name: 'docUnit',
  addOptions() {
    return {
      types: ['paragraph'],
    }
  },
  addGlobalAttributes() {
    return [
      {
        types: this.options.types,
        attributes: {
          fontSize: {
            default: null,
            parseHTML: element => element.style.fontSize.replace(/['"]+/g, ''),
            renderHTML: attributes => {
              if (!attributes.fontSize) {
                return {}
              }

              return {
                style: `font-size: ${attributes.fontSize}`
              }
            }
          },
          textAlign: {
            default: null,
            parseHTML: element => element.style.textAlign.replace(/['"]+/g, ''),
            renderHTML: attributes => {
              if (!attributes.textAlign) {
                return {}
              }

              return {
                style: `text-align: ${attributes.textAlign}`
              }
            }
          },
        }
      }
    ]
  }
})

/*
export const DocUnitDivExtension = Extension.create<DocUnitOptions>({
  name: 'docUnit',
  addOptions() {
    return {
      types: ['randnummer'],
    }
  },
  addGlobalAttributes() {
    return [
      {
        types: this.options.types,
        attributes: {
          randnummer: {
            default: null,
            parseHTML: element => element.getAttribute('class'),
            renderHTML: attributes => {
              if (attributes['class'] !== 'randnummer') {
                return {}
              }

              return {
                style: 'display: flex; margin-bottom: 10px'
              }
            }
          }
        }
      }
    ]
  }
})
*/

interface RandnummerOptions {
  HTMLAttributes: Record<string, any>,
}

export const Randnummer = Node.create<RandnummerOptions>({
  name: 'randnummer',
  priority: 1000,
  group: 'block',
  content: 'inline*',
  addAttributes() {
    return { number: '1' }
  },
  addOptions() {
    return {
      HTMLAttributes: {
        style: 'display: flex; margin-bottom: 10px'
      },
    }
  },
  parseHTML() {
    return [
      { tag: 'randnummer' },
    ]
  },
  renderHTML({ HTMLAttributes }) {
    return [ 
      'div', 
      mergeAttributes(this.options.HTMLAttributes, HTMLAttributes), 
      [
        'div',
        { style: 'padding-top: 20px; min-width: 40px;' },
        HTMLAttributes.number.toString()
      ],
      [
        'div',
        0 
      ],
    ]
  },
})
