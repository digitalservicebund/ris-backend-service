export function useResizeObserverMock() {
  global.ResizeObserver = class MockResizeObserver {
    public callback: ResizeObserverCallback
    public observe = vi.fn()
    public unobserve = vi.fn()
    public disconnect = vi.fn()

    constructor(callback: ResizeObserverCallback) {
      this.callback = callback
    }
  } as unknown as {
    new (callback: ResizeObserverCallback): ResizeObserver
    prototype: ResizeObserver
  }
}
