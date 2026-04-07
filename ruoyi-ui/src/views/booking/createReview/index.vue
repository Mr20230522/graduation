<template>
  <div class="create-review-page">
    <el-card class="review-card">
      <div slot="header" class="card-header">
        <i class="el-icon-edit-outline"></i>
        <span>服务评价</span>
      </div>

      <el-form :model="reviewForm" label-width="100px" class="review-form">
        <!-- 预约信息 -->
        <div class="booking-info">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="预约单号">{{ bookingInfo.bookingNo }}</el-descriptions-item>
            <el-descriptions-item label="服务门店">{{ bookingInfo.deptName }}</el-descriptions-item>
            <el-descriptions-item label="服务套餐">{{ bookingInfo.comboName }}</el-descriptions-item>
            <el-descriptions-item label="预约时间">{{ bookingInfo.time }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 评分 -->
        <el-form-item label="综合评分" required>
          <div class="rating-wrapper">
            <el-rate
              v-model="reviewForm.rating"
              :texts="ratingTexts"
              show-text
              :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
              :void-color="'#C0C4CC'"
              :disabled="submitting">
            </el-rate>
            <span class="rating-hint">{{ ratingHint }}</span>
          </div>
        </el-form-item>

        <!-- 评价内容 -->
        <el-form-item label="评价内容">
          <el-input
            type="textarea"
            v-model="reviewForm.content"
            :rows="4"
            placeholder="分享您的洗车体验，帮助我们做得更好...（选填）"
            maxlength="500"
            show-word-limit
            :disabled="submitting">
          </el-input>
        </el-form-item>

        <!-- 上传图片 -->
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
          <div class="upload-tip">最多上传9张图片，每张不超过5MB</div>

          <!-- 图片预览对话框 -->
          <el-dialog :visible.sync="previewVisible" append-to-body>
            <img width="100%" :src="previewUrl" alt="预览图片">
          </el-dialog>
        </el-form-item>

        <!-- 匿名评价 -->
        <el-form-item>
          <el-checkbox v-model="reviewForm.isAnonymous" :disabled="submitting">
            匿名评价（匿名后其他用户看不到您的头像和昵称）
          </el-checkbox>
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item class="submit-item">
          <el-button @click="goBack" :disabled="submitting">返回</el-button>
          <el-button type="primary" @click="submitReview" :loading="submitting" :disabled="!reviewForm.rating">
            提交评价
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { createReview, getDeptReviews } from '@/api/booking/booking'

export default {
  name: 'CreateReview',
  data() {
    return {
      submitting: false,
      ratingTexts: ['极差', '较差', '一般', '满意', '非常满意'],
      reviewForm: {
        bookingId: null,
        rating: 5,
        content: '',
        isAnonymous: false,
        images: []
      },
      bookingInfo: {
        bookingNo: '',
        deptName: '',
        comboName: '',
        time: ''
      },
      fileList: [],
      uploadUrl: process.env.VUE_APP_BASE_API + '/common/upload',
      previewVisible: false,
      previewUrl: ''
    }
  },
  computed: {
    ratingHint() {
      const hints = {
        1: '极差，体验很差',
        2: '较差，需要改进',
        3: '一般，还有提升空间',
        4: '满意，服务不错',
        5: '非常满意，五星好评'
      }
      return hints[this.reviewForm.rating] || ''
    }
  },
  created() {
    const bookingId = this.$route.query.bookingId
    const bookingNo = this.$route.query.bookingNo
    if (bookingId) {
      this.reviewForm.bookingId = parseInt(bookingId)
      this.bookingInfo.bookingNo = bookingNo || '未知'
      this.bookingInfo.deptName = this.$route.query.deptName || '未知门店'
      this.bookingInfo.comboName = this.$route.query.comboName || this.getComboName(this.$route.query.comboMinutes)
      this.bookingInfo.time = this.$route.query.time || ''
    } else {
      this.$message.error('缺少预约ID')
      this.goBack()
    }
  },
  methods: {
    getComboName(minutes) {
      const map = { 30: '标准洗车', 60: '精洗+打蜡', 90: '全套护理' }
      return map[minutes] || '未知套餐'
    },
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
    handleUploadSuccess(res, file, fileList) {
      if (res.code === 200) {
        const fileName = res.fileName || res.url || res.data?.url
        this.reviewForm.images.push(fileName)
        this.fileList = fileList.map(f => ({
          ...f,
          url: f.url || f.response?.fileName || f.response?.url || fileName
        }))
        this.$message.success('上传成功')
      } else {
        this.$message.error(res.msg || '上传失败')
      }
    },
    handleRemove(file, fileList) {
      const fileName = file.url || file.response?.fileName || file.response?.url
      const index = this.reviewForm.images.findIndex(url => url === fileName)
      if (index > -1) {
        this.reviewForm.images.splice(index, 1)
      }
      this.fileList = fileList
    },
    handlePreview(file) {
      this.previewUrl = file.url || file.response?.fileName || file.response?.url
      this.previewVisible = true
    },
    submitReview() {
      if (!this.reviewForm.rating) {
        this.$message.error('请选择评分')
        return
      }
      this.submitting = true
      createReview({
        bookingId: this.reviewForm.bookingId,
        rating: this.reviewForm.rating,
        content: this.reviewForm.content,
        isAnonymous: this.reviewForm.isAnonymous ? 1 : 0,
        images: this.reviewForm.images
      }).then(res => {
        this.$message.success('评价成功，感谢您的反馈！')
        setTimeout(() => {
          this.goBack()
        }, 1500)
      }).catch(err => {
        this.$message.error(err.msg || '评价失败，请重试')
      }).finally(() => {
        this.submitting = false
      })
    },
    goBack() {
      this.$router.go(-1)
    }
  }
}
</script>

<style scoped lang="scss">
.create-review-page {
  padding: 24px;
  background: #f5f7fb;
  min-height: 100vh;

  .review-card {
    max-width: 700px;
    margin: 0 auto;
    border-radius: 16px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);

    .card-header {
      font-size: 18px;
      font-weight: 600;
      color: #2c3e50;

      i {
        margin-right: 8px;
        color: #409eff;
      }
    }

    .review-form {
      padding: 20px 0;

      .booking-info {
        margin-bottom: 24px;
        padding: 16px;
        background: #f8f9fa;
        border-radius: 8px;
      }

      .rating-wrapper {
        display: flex;
        align-items: center;
        gap: 16px;

        .el-rate {
          line-height: 2;
        }

        .rating-hint {
          font-size: 14px;
          color: #8a8f9d;
        }
      }

      .upload-tip {
        font-size: 12px;
        color: #8a8f9d;
        margin-top: 8px;
      }

      .submit-item {
        margin-top: 32px;
        text-align: center;

        .el-button {
          min-width: 120px;
        }
      }
    }
  }
}
</style>
