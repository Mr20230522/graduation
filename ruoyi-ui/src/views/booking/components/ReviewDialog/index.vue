<template>
  <el-dialog
    title="评价服务"
    :visible.sync="dialogVisible"
    width="500px"
    @close="resetForm"
    :close-on-click-modal="false">

    <el-form :model="reviewForm" label-width="100px">
      <el-form-item label="综合评分" required>
        <el-rate
          v-model="reviewForm.rating"
          :texts="ratingTexts"
          show-text
          :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
          :void-color="'#C0C4CC'"
          :disabled="submitting">
        </el-rate>
      </el-form-item>

      <el-form-item label="评价内容">
        <el-input
          type="textarea"
          v-model="reviewForm.content"
          :rows="4"
          placeholder="说说你的洗车体验...（选填）"
          maxlength="500"
          show-word-limit
          :disabled="submitting">
        </el-input>
      </el-form-item>

      <el-form-item label="上传图片">
        <el-upload
          :action="uploadUrl"
          list-type="picture-card"
          :on-success="handleUploadSuccess"
          :on-remove="handleRemove"
          :on-preview="handlePreview"
          :limit="9"
          :file-list="fileList"
          :disabled="submitting"
          :before-upload="beforeUpload">
          <i class="el-icon-plus"></i>
        </el-upload>

        <!-- 图片预览对话框 -->
        <el-dialog :visible.sync="previewVisible" append-to-body>
          <img width="100%" :src="previewUrl" alt="预览图片">
        </el-dialog>
      </el-form-item>

      <el-form-item>
        <el-checkbox v-model="reviewForm.isAnonymous" :disabled="submitting">匿名评价</el-checkbox>
      </el-form-item>
    </el-form>

    <div slot="footer">
      <el-button @click="dialogVisible = false" :disabled="submitting">取 消</el-button>
      <el-button type="primary" @click="submitReview" :loading="submitting">提 交 评 价</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { createReview } from '@/api/booking/booking'

export default {
  name: 'ReviewDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    bookingId: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      dialogVisible: false,
      submitting: false,
      ratingTexts: ['极差', '较差', '一般', '满意', '非常满意'],
      reviewForm: {
        rating: 5,
        content: '',
        isAnonymous: false,
        images: []
      },
      fileList: [],
      uploadUrl: process.env.VUE_APP_BASE_API + '/common/upload', // 若依通用上传接口
      previewVisible: false,
      previewUrl: ''
    }
  },
  watch: {
    visible(val) {
      this.dialogVisible = val
    },
    dialogVisible(val) {
      this.$emit('update:visible', val)
    }
  },
  methods: {
    // 上传前检查
    beforeUpload(file) {
      const isImage = file.type.startsWith('image/')
      const isLt5M = file.size / 1024 / 1024 < 5

      if (!isImage) {
        this.$message.error('只能上传图片文件')
        return false
      }
      if (!isLt5M) {
        this.$message.error('图片大小不能超过5MB')
        return false
      }
      return true
    },

    // 上传成功
    handleUploadSuccess(res, file, fileList) {
      if (res.code === 200) {
        this.reviewForm.images.push(res.fileName) // 假设返回的文件名是 fileName
        this.fileList = fileList
        this.$message.success('上传成功')
      } else {
        this.$message.error(res.msg || '上传失败')
      }
    },

    // 移除图片
    handleRemove(file, fileList) {
      // 从 images 数组中移除对应的 URL
      const index = this.reviewForm.images.findIndex(url => url === file.url || url === file.response?.fileName)
      if (index > -1) {
        this.reviewForm.images.splice(index, 1)
      }
      this.fileList = fileList
    },

    // 预览图片
    handlePreview(file) {
      this.previewUrl = file.url || file.response?.fileName
      this.previewVisible = true
    },

    // 提交评价
    submitReview() {
      if (!this.reviewForm.rating) {
        this.$message.error('请选择评分')
        return
      }

      this.submitting = true
      createReview({
        bookingId: this.bookingId,
        rating: this.reviewForm.rating,
        content: this.reviewForm.content,
        isAnonymous: this.reviewForm.isAnonymous ? 1 : 0,
        images: this.reviewForm.images
      }).then(() => {
        this.$message.success('评价成功')
        this.dialogVisible = false
        this.$emit('success')
      }).catch(err => {
        this.$message.error(err.msg || '评价失败')
      }).finally(() => {
        this.submitting = false
      })
    },

    // 重置表单
    resetForm() {
      this.reviewForm = {
        rating: 5,
        content: '',
        isAnonymous: false,
        images: []
      }
      this.fileList = []
    }
  }
}
</script>

<style scoped>
.el-rate {
  line-height: 2;
}
</style>
