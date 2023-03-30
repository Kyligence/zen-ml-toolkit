function isSupportURL () {
  return ['hash', 'host', 'hostname', 'href', 'port', 'protocol', 'search', 'toString', 'pathname', 'origin', 'searchParams'].every(func => func in window.URL.prototype)
}

export default async function getPolyfills () {
  if (!window.URL || !isSupportURL()) {
    await import(/* webpackChunkName: "url-polyfill" */ 'url-polyfill')
  }
  if (!window.WeakMap) {
    await import(/* webpackChunkName: "weakmap-polyfill" */ 'weakmap-polyfill')
  }
  if (!window.ResizeObserver) {
    window.ResizeObserver = (await import(/* webpackChunkName: "resize-observer-polyfill" */ 'resize-observer-polyfill')).default
  }
}
