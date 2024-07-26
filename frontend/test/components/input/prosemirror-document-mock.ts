/* eslint-disable @typescript-eslint/no-explicit-any */

export const mockDocumentForProsemirror = () => {
  function getBoundingClientRect() {
    const rec = {
      x: 0,
      y: 0,
      bottom: 0,
      height: 0,
      left: 0,
      right: 0,
      top: 0,
      width: 0,
    }
    return { ...rec, toJSON: () => rec }
  }

  class FakeDOMRectList extends DOMRect {
    item(index: any) {
      return (this as any)[index]
    }
  }

  document.elementFromPoint = () => null
  HTMLElement.prototype.getBoundingClientRect = getBoundingClientRect
  HTMLElement.prototype.getClientRects = () => new FakeDOMRectList() as any
  Range.prototype.getBoundingClientRect = getBoundingClientRect
  Range.prototype.getClientRects = () => new FakeDOMRectList() as any
}
