import { Browser } from "playwright"

export const getAuthenticatedPage = async (browser: Browser) => {
  const context = await browser.newContext({
    httpCredentials: {
      username: process.env.STAGING_USER ?? "",
      password: process.env.STAGING_PASSWORD ?? "",
    },
  })
  return await context.newPage()
}
