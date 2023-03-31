<template>
  <div class="kyligence-zen-ml-toolkit">
    <div class="upload-box" v-if="!dowloadFileUrl" :loading="isUploading">
      <el-upload
        drag
        :multiple="false"
        class="upload"
        name="file"
        action="/upload"
        :show-file-list="false"
        :on-success="handleSuccess"
        :on-error="handleError"
        :on-change="handleChange">
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <div class="el-upload__tip" slot="tip">只能上传 tds 文件，且不超过 500kb</div>
        <!-- <el-button class="upload-btn" type="primary" :loading="isUploading">Upload</el-button> -->
      </el-upload>
      <p v-if="errorMsg" class="error-msg">{{errorMsg}}</p>
    </div>
    <div class="dowload-box" v-else>
      <div>
        <a download :href="dowloadFileUrl" class="el-button el-button--primary btn">下载</a>
      </div>
      <el-button type="text" @click="dowloadFileUrl = ''">转换另一个</el-button>
    </div>
  </div>
</template>

<script>
export default {
  data () {
    return {
      dowloadFileUrl: '',
      isUploading: false,
      errorMsg: ''
    }
  },
  methods: {
    handleChange (file, fileList) {
      this.isUploading = true
    },
    handleSuccess (res, file, fileList) {
      this.errorMsg = ''
      this.isUploading = false
      const fileName = file.name
      this.dowloadFileUrl = '/download?uuid=' + res.uuid
    },
    handleError (err, file, fileList) {
      // error 是个 Error 对象，所以要获取内部的内容，需要用 err.message 取出，可参考 js 对 new Error 的文档说明
      let msg = ''
      try {
        const errorJson = JSON.parse(err.message)
        console.log(errorJson)
        msg = errorJson.error ? errorJson.error : 'Upload Error'
      } catch (e) {
        console.log(e)
        msg = 'Upload Error'
      }
      console.log(msg)
      this.isUploading = false
      this.dowloadFileUrl = ''
      this.errorMsg = msg
    }
  }
}
</script>
