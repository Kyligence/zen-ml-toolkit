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
        :on-change="handleChange"
        :before-upload="handleBeforeUpload">
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">Drag your files here, or <em>click to upload</em></div>
        <div class="el-upload__tip" slot="tip">Upload your .tds or .twb file, we help you export your Tableau calculated fields into an Excel spreadsheet. 1MB maximum file size.</div>
      </el-upload>
      <p v-if="errorMsg" class="error-msg">{{errorMsg}}</p>
    </div>
    <div class="dowload-box" v-else>
      <div class="btn-box">
        <div>
          <a download :href="dowloadFileUrl" class="el-button el-button--primary btn"><i class="el-icon-download"></i> Download</a>
        </div>
        <el-button type="text" @click="dowloadFileUrl = ''">Try another .tds or .twb file</el-button>
      </div>
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
    handleBeforeUpload (file) {
      const filename = file.name
      const strArr = filename.split('.')
      const sufix = strArr[strArr.length - 1]
      const fileTypeValid = sufix === 'tds' || sufix === 'twb'
      const isLt1M = file.size <= 1 * 1024 * 1024

      if (!fileTypeValid) {
        this.errorMsg = 'Only support .tds or .twb file'
      } else if (!isLt1M) {
        this.errorMsg = '1MB maximum file size'
      } else {
        this.errorMsg = ''
      }
      return fileTypeValid && isLt1M
    },
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
        msg = errorJson.error ? errorJson.error : 'Upload Error'
      } catch (e) {
        console.log(e)
        msg = 'Upload Error'
      }
      this.isUploading = false
      this.dowloadFileUrl = ''
      this.errorMsg = msg
    }
  }
}
</script>
