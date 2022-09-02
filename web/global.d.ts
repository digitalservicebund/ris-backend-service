declare global {
  namespace PlaywrightTest {
    interface Matchers<R> {
      toHaveInsideViewport(selector: string): Promise<R>
      toHaveOutsideViewport(selector: string): Promise<R>
    }
  }
}

export {}
