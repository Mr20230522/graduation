<template>
  <div class="review-management">
    <!-- 头部统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon">
            <i class="el-icon-star-on"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ ratingStats.avgRating || 0 }}</div>
            <div class="stat-label">综合评分</div>
            <el-rate v-model="ratingStats.avgRating" disabled text-color="#ff9900" class="stat-rate"></el-rate>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon">
            <i class="el-icon-chat-line-round"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ ratingStats.totalReviews || 0 }}</div>
            <div class="stat-label">总评价数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon">
            <i class="el-icon-picture-outline"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ ratingStats.imageReviews || 0 }}</div>
            <div class="stat-label">带图评价</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon">
            <i class="el-icon-chat-dot-round"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ ratingStats.replyRate || 0 }}%</div>
            <div class="stat-label">回复率</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 评分分布 -->
    <el-row :gutter="20" class="rating-distribution">
      <el-col :span="24">
        <el-card class="distribution-card" shadow="never">
          <div slot="header" class="card-header">
            <span><i class="el-icon-data-analysis"></i> 评分分布</span>
          </div>
          <div class="distribution-content">
            <div v-for="star in [5,4,3,2,1]" :key="star" class="distribution-item">
              <div class="star-label">{{ star }}星</div>
              <el-progress
                :percentage="getStarPercentage(star)"
                :color="getStarColor(star)"
                :stroke-width="12"
                :show-text="false">
              </el-progress>
              <div class="star-count">{{ getStarCount(star) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 评价列表 -->
    <el-card class="review-list-card" shadow="never">
      <div slot="header" class="card-header">
        <span><i class="el-icon-message"></i> 评价列表</span>
        <div class="header-actions">
          <el-select v-model="reviewQuery.rating" placeholder="全部评分" clearable size="small" @change="loadReviews" style="width: 100px">
            <el-option label="5星" value="5"></el-option>
            <el-option label="4星" value="4"></el-option>
            <el-option label="3星" value="3"></el-option>
            <el-option label="2星" value="2"></el-option>
            <el-option label="1星" value="1"></el-option>
          </el-select>
          <el-select v-model="reviewQuery.hasImage" placeholder="全部评价" clearable size="small" @change="loadReviews" style="width: 100px; margin-left: 10px">
            <el-option label="带图评价" value="1"></el-option>
            <el-option label="纯文字" value="0"></el-option>
          </el-select>
          <el-button type="primary" size="small" icon="el-icon-search" @click="loadReviews" style="margin-left: 10px">查询</el-button>
        </div>
      </div>

      <div v-loading="reviewLoading" class="review-list">
        <div v-for="item in reviewList" :key="item.reviewId" class="review-item">
          <div class="review-header">
            <div class="user-info">
              <el-avatar :src="item.avatar" :size="40">
                <img src="https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png"/>
              </el-avatar>
              <div class="user-detail">
                <div class="user-name">{{ item.isAnonymous == 1 ? '匿名用户' : item.userName }}</div>
                <div class="review-time">{{ item.createTime }}</div>
              </div>
            </div>
            <div class="rating-info">
              <el-rate v-model="item.rating" disabled text-color="#ff9900"></el-rate>
              <span class="car-number" v-if="item.carNumber">{{ item.carNumber }}</span>
            </div>
          </div>

          <div class="review-content">
            {{ item.content }}
          </div>

          <div v-if="item.images" class="review-images">
            <el-image
              v-for="(img, idx) in item.images.split(',')"
              :key="idx"
              :src="img"
              :preview-src-list="item.images.split(',')"
              fit="cover"
              class="review-image">
            </el-image>
          </div>

          <div class="review-footer">
            <div v-if="item.replyContent" class="reply-box">
              <div class="reply-header">
                <i class="el-icon-s-promotion"></i>
                <span>商家回复</span>
              </div>
              <div class="reply-content">{{ item.replyContent }}</div>
              <div class="reply-time">{{ item.replyTime }}</div>
            </div>
            <div v-else class="reply-btn-box">
              <el-button type="text" size="small" @click="showReplyDialog(item)">
                <i class="el-icon-edit"></i> 回复
              </el-button>
            </div>
          </div>
        </div>

        <div v-if="reviewList.length === 0 && !reviewLoading" class="empty-state">
          <i class="el-icon-chat-line-round"></i>
          <p>暂无评价</p>
        </div>

        <pagination
          v-show="reviewTotal > 0"
          :total="reviewTotal"
          :page.sync="reviewQuery.pageNum"
          :limit.sync="reviewQuery.pageSize"
          @pagination="loadReviews"
          class="pagination" />
      </div>
    </el-card>

    <!-- 回复评价对话框 -->
    <el-dialog title="回复评价" :visible.sync="replyDialogVisible" width="500px" class="reply-dialog">
      <el-form :model="replyForm">
        <el-form-item label="评价内容">
          <div class="original-review">{{ replyForm.originalContent }}</div>
        </el-form-item>
        <el-form-item label="回复内容">
          <el-input type="textarea" v-model="replyForm.replyContent" rows="5" placeholder="请输入回复内容..."></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply">确定回复</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getStatistics,
  getReviewList,
  bossReplyReview
} from '@/api/booking/booking'

export default {
  name: "BossReview",
  data() {
    return {
      ratingStats: {
        avgRating: 0,
        totalReviews: 0,
        imageReviews: 0,
        replyRate: 0,
        fiveStarCount: 0,
        fourStarCount: 0,
        threeStarCount: 0,
        twoStarCount: 0,
        oneStarCount: 0
      },
      reviewList: [],
      reviewTotal: 0,
      reviewLoading: false,
      reviewQuery: {
        rating: '',
        hasImage: '',
        pageNum: 1,
        pageSize: 10
      },
      replyDialogVisible: false,
      replyForm: {
        reviewId: null,
        replyContent: '',
        originalContent: ''
      }
    }
  },
  created() {
    this.loadRatingStats()
    this.loadReviews()
  },
  methods: {
    loadRatingStats() {
      getStatistics().then(res => {
        this.ratingStats = {
          ...res.data,
          imageReviews: res.data.imageReviews || 0,
          replyRate: res.data.replyRate || 0
        }
      }).catch(() => {
        this.$message.error('获取统计数据失败')
      })
    },
    loadReviews() {
      this.reviewLoading = true
      getReviewList(this.reviewQuery).then(res => {
        this.reviewList = res.rows || []
        this.reviewTotal = res.total || 0
        this.reviewLoading = false
      }).catch(() => {
        this.reviewLoading = false
        this.$message.error('获取评价列表失败')
      })
    },
    getStarPercentage(star) {
      if (!this.ratingStats.totalReviews) return 0
      const count = this.getStarCount(star)
      return Math.round((count / this.ratingStats.totalReviews) * 100)
    },
    getStarCount(star) {
      switch(star) {
        case 5: return this.ratingStats.fiveStarCount || 0
        case 4: return this.ratingStats.fourStarCount || 0
        case 3: return this.ratingStats.threeStarCount || 0
        case 2: return this.ratingStats.twoStarCount || 0
        case 1: return this.ratingStats.oneStarCount || 0
        default: return 0
      }
    },
    getStarColor(star) {
      const colors = {
        5: '#67C23A',
        4: '#85CE36',
        3: '#E6A23C',
        2: '#F56C6C',
        1: '#F56C6C'
      }
      return colors[star]
    },
    showReplyDialog(item) {
      this.replyForm.reviewId = item.reviewId
      this.replyForm.replyContent = ''
      this.replyForm.originalContent = item.content
      this.replyDialogVisible = true
    },
    submitReply() {
      if (!this.replyForm.replyContent) {
        this.$message.warning('请输入回复内容')
        return
      }
      bossReplyReview(this.replyForm).then(() => {
        this.$message.success('回复成功')
        this.replyDialogVisible = false
        this.loadReviews()
        this.loadRatingStats()
      }).catch(() => {
        this.$message.error('回复失败')
      })
    }
  }
}
</script>

<style scoped lang="scss">
.review-management {
  padding: 20px;
  background: #f0f2f6;
  min-height: 100vh;

  .stats-row {
    margin-bottom: 20px;

    .stat-card {
      background: white;
      border-radius: 12px;
      padding: 20px;
      display: flex;
      align-items: center;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      transition: all 0.3s;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
      }

      .stat-icon {
        width: 50px;
        height: 50px;
        border-radius: 12px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 15px;

        i {
          font-size: 28px;
          color: white;
        }
      }

      .stat-info {
        flex: 1;

        .stat-value {
          font-size: 28px;
          font-weight: bold;
          color: #2c3e50;
          line-height: 1.2;
        }

        .stat-label {
          font-size: 13px;
          color: #909399;
          margin-top: 4px;
        }

        .stat-rate {
          margin-top: 5px;
        }
      }
    }

    .el-col:nth-child(2) .stat-icon {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    }
    .el-col:nth-child(3) .stat-icon {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    }
    .el-col:nth-child(4) .stat-icon {
      background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
    }
  }

  .rating-distribution {
    margin-bottom: 20px;

    .distribution-card {
      border-radius: 12px;

      .card-header {
        font-weight: 600;
        color: #2c3e50;

        i {
          margin-right: 8px;
          color: #409EFF;
        }
      }

      .distribution-content {
        padding: 10px 0;

        .distribution-item {
          display: flex;
          align-items: center;
          margin-bottom: 15px;

          .star-label {
            width: 50px;
            font-size: 13px;
            color: #606266;
          }

          ::v-deep .el-progress {
            flex: 1;
            margin: 0 15px;

            .el-progress-bar__outer {
              background-color: #edf2f7;
            }
          }

          .star-count {
            width: 40px;
            font-size: 13px;
            color: #909399;
            text-align: right;
          }
        }
      }
    }
  }

  .review-list-card {
    border-radius: 12px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-weight: 600;
      color: #2c3e50;

      i {
        margin-right: 8px;
        color: #409EFF;
      }

      .header-actions {
        display: flex;
        align-items: center;
      }
    }

    .review-list {
      .review-item {
        border-bottom: 1px solid #ebeef5;
        padding: 20px 0;

        &:last-child {
          border-bottom: none;
        }

        .review-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          margin-bottom: 15px;

          .user-info {
            display: flex;
            align-items: center;

            .user-detail {
              margin-left: 12px;

              .user-name {
                font-weight: 600;
                color: #2c3e50;
                margin-bottom: 4px;
              }

              .review-time {
                font-size: 12px;
                color: #909399;
              }
            }
          }

          .rating-info {
            text-align: right;

            .car-number {
              display: block;
              font-size: 12px;
              color: #909399;
              margin-top: 5px;
            }
          }
        }

        .review-content {
          color: #606266;
          line-height: 1.6;
          margin-bottom: 12px;
          font-size: 14px;
        }

        .review-images {
          display: flex;
          gap: 10px;
          margin-bottom: 12px;
          flex-wrap: wrap;

          .review-image {
            width: 80px;
            height: 80px;
            border-radius: 8px;
            cursor: pointer;
            transition: transform 0.2s;

            &:hover {
              transform: scale(1.05);
            }
          }
        }

        .review-footer {
          .reply-box {
            background: #f5f7fa;
            border-radius: 8px;
            padding: 12px;
            margin-top: 12px;

            .reply-header {
              font-size: 13px;
              font-weight: 500;
              color: #67C23A;
              margin-bottom: 8px;

              i {
                margin-right: 5px;
              }
            }

            .reply-content {
              color: #606266;
              font-size: 13px;
              line-height: 1.5;
              margin-bottom: 6px;
            }

            .reply-time {
              font-size: 11px;
              color: #909399;
            }
          }

          .reply-btn-box {
            margin-top: 12px;
            text-align: right;

            .el-button {
              color: #409EFF;
            }
          }
        }
      }

      .empty-state {
        text-align: center;
        padding: 60px 20px;

        i {
          font-size: 64px;
          color: #dcdfe6;
        }

        p {
          margin-top: 16px;
          color: #909399;
        }
      }

      .pagination {
        margin-top: 20px;
        text-align: center;
      }
    }
  }

  ::v-deep .reply-dialog {
    .original-review {
      background: #f5f7fa;
      padding: 12px;
      border-radius: 8px;
      color: #606266;
      line-height: 1.5;
      max-height: 100px;
      overflow-y: auto;
    }
  }
}
</style>
