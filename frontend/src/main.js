import getPolyfills from './utils/polyfill'

(async function () {
  // 先按需加载polyfill
  await getPolyfills()
  // 再载入代码
  await import('./entry')
})()
