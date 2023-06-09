<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->
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
        <div class="el-upload__tip" slot="tip"> Only [.tds, .twb, .sql] file supported, we help you export calculated fields or measures into an Excel spreadsheet. 5MB maximum file size. </br> <a target="_blank" href="https://github.com/Kyligence/zen-ml-toolkit/">Source Code on Github</a>, <a target="_blank" href="https://github.com/Kyligence/zen-ml-toolkit/releases">Release: v0.1.3</a></div>
      </el-upload>
      <p v-if="errorMsg" class="error-msg">{{errorMsg}}</p>
    </div>
    <div class="dowload-box" v-else>
      <div class="btn-box">
        <div>
          <a download :href="dowloadFileUrl" class="el-button el-button--primary btn"><i class="el-icon-download"></i> Download</a>
        </div>
        <el-button type="text" @click="dowloadFileUrl = ''">Try another [.tds, .twb, .sql] file</el-button>
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
      const sufix = strArr.length ? strArr[strArr.length - 1] : ''
      const fileTypeValid = sufix.toLocaleLowerCase() === 'tds' || sufix.toLocaleLowerCase() === 'twb'
        || sufix.toLocaleLowerCase() === 'sql'
      const isLt1M = file.size <= 5 * 1024 * 1024

      if (!fileTypeValid) {
        this.errorMsg = 'Only support [.tds, .twb, .sql] file'
      } else if (!isLt1M) {
        this.errorMsg = '5MB maximum file size'
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
