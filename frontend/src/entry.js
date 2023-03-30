import Vue from 'vue'
import { Button, Upload } from 'element-ui';
import './assets/styles/style.css'
import App from './App'

Vue.config.productionTip = false

Vue.use(Upload)
Vue.use(Button)

window.kyVm = new Vue({
  render: h => h(App)
}).$mount('#app')
