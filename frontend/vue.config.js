module.exports = {
  lintOnSave: false,
  publicPath: process.env.BASE_URL,
  assetsDir: 'static', // 配置js、css静态资源二级目录的位置
  devServer: {
    port: process.env.LOCAL_SERVER_PORT,
    proxy: process.env.PROXY_SERVER
  },
  transpileDependencies: [
    'js-base64'
  ],
  chainWebpack: config => {
    // 定义HTML标题
    config
      .plugin('html')
      .tap(args => {
        args[0].title = 'Kyligence'
        return args
      })
      .end()
  }
}
