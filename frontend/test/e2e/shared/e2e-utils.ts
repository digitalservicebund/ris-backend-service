import { Page } from "playwright"

export const isInViewport = (page: Page, selector: string, inside: boolean) => {
  return page.locator(selector).evaluate((element, inside) => {
    return new Promise((resolve) => {
      let observer: IntersectionObserver | undefined
      const timeout: ReturnType<typeof setTimeout> = setTimeout(() => {
        stopObserving(false)
      }, 20 * 1000)

      function stopObserving(result: boolean) {
        if (observer) {
          observer.disconnect()
          observer = undefined
        }
        clearTimeout(timeout)
        resolve(result)
      }

      function onIntersection(entries: { isIntersecting: boolean }[]) {
        if (entries[0].isIntersecting == inside) {
          stopObserving(true)
        }
      }

      observer = new IntersectionObserver(onIntersection)
      observer.observe(element)
      requestAnimationFrame(() => {
        // Firefox does not call IntersectionObserver without request animation frames
      })
    })
  }, inside)
}
