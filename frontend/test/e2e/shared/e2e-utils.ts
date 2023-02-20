import { JSHandle, Page } from "playwright"

export const isInViewport = (page: Page, selector: string, inside: boolean) => {
  return page
    .locator(selector)
    .first()
    .evaluate((element, inside) => {
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

export async function createDataTransfer(
  page: Page,
  fileContent: Buffer,
  fileName: string,
  fileType: string
): Promise<JSHandle<DataTransfer>> {
  return page.evaluateHandle(
    async ({ buffer, fileName, fileType }) => {
      const blob = await fetch(buffer).then((value) => value.blob())
      const file = new File([blob], fileName, { type: fileType })
      const data = new DataTransfer()
      data.items.add(file)
      return data
    },
    {
      buffer: `data:application/octet-stream;base64,${fileContent.toString(
        "base64"
      )}`,
      fileName,
      fileType,
    }
  )
}
