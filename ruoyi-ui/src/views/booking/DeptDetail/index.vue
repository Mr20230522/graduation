<template>
  <div class="app-container">
    <!-- 门店基本信息 -->
    <el-card class="dept-info-card" shadow="hover">
      <div class="dept-header">
        <h2>{{ deptInfo.deptName }}</h2>
        <div class="rating-summary" v-if="stats">
          <div class="score-box">
            <span class="score">{{ stats.avgRating }}</span>
            <span class="total">/5分</span>
          </div>
          <el-rate v-model="stats.avgRating" disabled></el-rate>
          <span class="review-count">{{ stats.totalReviews }}条评价</span>
        </div>
      </div>

      <div class="dept-contact">
        <p><i class="el-icon-location"></i> {{ deptInfo.address || '暂无地址' }}</p>
        <p><i class="el-icon-phone"></i> {{ deptInfo.phone || '暂无电话' }}</p>
        <p><i class="el-icon-time"></i> 营业时间：{{ deptInfo.businessHours || '08:00 - 20:00' }}</p>
      </div>
    </el-card>

    <!-- 评分分布 -->
    <el-card class="rating-distribution-card" shadow="hover" v-if="stats">
      <div slot="header">
        <span>评分分布</span>
      </div>
      <div class="distribution-list">
        <div class="distribution-item" v-for="star in 5" :key="star">
          <span class="star-label">{{ 6-star }}星</span>
          <el-progress
            :percentage="getStarPercentage(6-star)"
            :color="getStarColor(6-star)"
            :show-text="false">
          </el-progress>
          <span class="star-count">{{ getStarCount(6-star) }}条</span>
        </div>
      </div>
    </el-card>

    <!-- 评价列表 -->
    <el-card class="review-list-card" shadow="hover">
      <div slot="header" class="review-header">
        <span>用户评价</span>
        <div class="filter-tabs">
          <el-radio-group v-model="filterType" size="small" @change="handleFilterChange">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="image">有图</el-radio-button>
            <el-radio-button label="good">好评 (4-5星)</el-radio-button>
            <el-radio-button label="bad">差评 (1-3星)</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <div v-loading="loading" class="review-list">
        <div v-if="reviewList.length === 0" class="empty-review">
          <i class="el-icon-chat-dot-round"></i>
          <p>暂无评价</p>
        </div>

        <div v-for="review in reviewList" :key="review.id" class="review-item">
          <div class="review-header">
            <div class="user-info">
              <el-avatar :size="40" :src="review.avatar || defaultAvatar"></el-avatar>
              <div class="user-meta">
                <span class="username">{{ review.userName || '匿名用户' }}</span>
                <el-rate v-model="review.rating" disabled size="small"></el-rate>
              </div>
            </div>
            <span class="review-time">{{ formatDate(review.createTime) }}</span>
          </div>

          <div class="review-content">
            <p>{{ review.content || '用户没有填写评价内容' }}</p>

            <!-- 评价图片 -->
            <div class="review-images" v-if="review.images && review.images.length">
              <el-image
                v-for="(img, idx) in review.images"
                :key="idx"
                :src="img"
                :preview-src-list="review.images"
                fit="cover"
                class="review-image">
              </el-image>
            </div>
          </div>

          <!-- 商家回复 -->
          <div class="merchant-reply" v-if="review.replyContent">
            <i class="el-icon-chat-dot-round"></i>
            <div class="reply-content">
              <span class="reply-label">商家回复：</span>
              {{ review.replyContent }}
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <el-pagination
          v-if="total > 0"
          :current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="handlePageChange"
          class="pagination">
        </el-pagination>
      </div>
    </el-card>

    <!-- 底部预约按钮 -->
    <div class="bottom-bar">
      <el-button type="primary" size="large" @click="goToBook">立即预约</el-button>
    </div>
  </div>
</template>

<script>
import { getDeptDetail } from '@/api/booking/booking'
import { getDeptReviews, getDeptRatingStats } from '@/api/booking/booking'

export default {
  name: 'DeptDetail',
  data() {
    return {
      deptId: null,
      deptInfo: {},
      stats: null,
      reviewList: [],
      loading: false,
      pageNum: 1,
      pageSize: 10,
      total: 0,
      filterType: 'all',
      defaultAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
    }
  },
  created() {
    this.deptId = this.$route.params.deptId
    if (!this.deptId) {
      this.$message.error('门店ID不存在')
      this.$router.back()
      return
    }
    this.loadDeptInfo()
    this.loadReviews()
  },
  methods: {
    // 加载门店信息
    loadDeptInfo() {
      // 这里需要实现获取门店详情的接口
      // 暂时从路由参数获取
      this.deptInfo = {
        deptName: this.$route.query.deptName || '门店详情',
        address: this.$route.query.address || ''
      }

      // 获取评分统计
      getDeptRatingStats(this.deptId).then(res => {
        this.stats = res.data
      })
    },

    // 加载评价列表
    loadReviews() {
      this.loading = true
      getDeptReviews(this.deptId, this.pageNum, this.pageSize).then(res => {
        this.reviewList = res.rows || []
        this.total = res.total
      }).finally(() => {
        this.loading = false
      })
    },

    // 筛选变化
    handleFilterChange() {
      this.pageNum = 1
      // 这里可以根据筛选类型调用不同的接口
      this.loadReviews()
    },

    // 分页变化
    handlePageChange(page) {
      this.pageNum = page
      this.loadReviews()
    },

    // 获取星级百分比
    getStarPercentage(star) {
      if (!this.stats || this.stats.totalReviews === 0) return 0
      const count = this.stats[`${star}StarCount`] || 0
      return Math.round((count / this.stats.totalReviews) * 100)
    },

    // 获取星级颜色
    getStarColor(star) {
      const colors = {
        5: '#67C23A',
        4: '#67C23A',
        3: '#E6A23C',
        2: '#F56C6C',
        1: '#F56C6C'
      }
      return colors[star] || '#909399'
    },

    // 获取星级数量
    getStarCount(star) {
      return this.stats ? (this.stats[`${star}StarCount`] || 0) : 0
    },

    // 格式化日期
    formatDate(date) {
      if (!date) return ''
      const d = new Date(date)
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },

    // 去预约
    goToBook() {
      this.$router.push({
        path: '/booking/calendar',
        query: {
          deptId: this.deptId,
          deptName: this.deptInfo.deptName
        }
      })
    }
  }
}
</script>

<style scoped>
.app-container {
  max-width: 1200px;
  margin: 0 auto;
  padding-bottom: 80px;
}

.dept-info-card {
  margin-bottom: 20px;
}

.dept-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.dept-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.rating-summary {
  display: flex;
  align-items: center;
  gap: 15px;
}

.score-box {
  display: flex;
  align-items: baseline;
}

.score {
  font-size: 28px;
  font-weight: bold;
  color: #F56C6C;
}

.total {
  font-size: 14px;
  color: #909399;
  margin-left: 2px;
}

.review-count {
  color: #909399;
  font-size: 14px;
}

.dept-contact p {
  margin: 8px 0;
  color: #666;
}

.dept-contact i {
  margin-right: 8px;
  color: #409EFF;
  width: 20px;
}

.rating-distribution-card {
  margin-bottom: 20px;
}

.distribution-list {
  padding: 10px 0;
}

.distribution-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.star-label {
  width: 40px;
  font-size: 14px;
  color: #666;
}

.el-progress {
  flex: 1;
  margin: 0 10px;
}

.star-count {
  width: 50px;
  font-size: 14px;
  color: #909399;
}

.review-list-card {
  margin-bottom: 20px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.review-item {
  padding: 20px 0;
  border-bottom: 1px solid #EBEEF5;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.username {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.review-time {
  color: #909399;
  font-size: 14px;
}

.review-content {
  margin-left: 52px;
}

.review-content p {
  margin: 0 0 15px 0;
  color: #666;
  line-height: 1.6;
}

.review-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.review-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  cursor: pointer;
  object-fit: cover;
}

.merchant-reply {
  margin-top: 15px;
  margin-left: 52px;
  padding: 12px;
  background: #F5F7FA;
  border-radius: 4px;
  display: flex;
  gap: 8px;
}

.merchant-reply i {
  color: #409EFF;
  font-size: 16px;
}

.reply-content {
  flex: 1;
  color: #666;
  font-size: 14px;
}

.reply-label {
  color: #409EFF;
  font-weight: 500;
}

.empty-review {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.empty-review i {
  font-size: 48px;
  margin-bottom: 10px;
}

.pagination {
  text-align: center;
  margin-top: 20px;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 200px;
  right: 0;
  background: white;
  padding: 15px 24px;
  text-align: center;
  box-shadow: 0 -2px 8px rgba(0,0,0,0.1);
  z-index: 100;
}

.bottom-bar .el-button {
  width: 300px;
}
</style>
