import { JSHandle, Page } from "@playwright/test"

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
