<template>
  <div class="consumption-page">
    <!-- 头部统计卡片 -->
    <div class="stats-wrapper">
      <div class="stat-card" v-for="stat in stats" :key="stat.label">
        <div class="stat-icon" :style="{ background: stat.gradient }">
          <i :class="stat.icon"></i>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
          <div class="stat-trend" v-if="stat.trend">
            <i :class="stat.trendIcon"></i> {{ stat.trend }}
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <el-card class="filter-card" shadow="never">
      <el-row :gutter="20" align="middle">
        <el-col :span="6">
          <div class="filter-item">
            <label><i class="el-icon-date"></i> 时间范围</label>
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="yyyy-MM-dd"
              @change="handleDateChange"
              class="filter-date">
            </el-date-picker>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="filter-item">
            <label><i class="el-icon-search"></i> 订单号/车牌</label>
            <el-input
              v-model="queryParams.keyword"
              placeholder="输入订单号或车牌号"
              clearable
              @clear="loadData"
              @keyup.enter="loadData">
              <el-button slot="append" icon="el-icon-search" @click="loadData"></el-button>
            </el-input>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="filter-item">
            <label><i class="el-icon-tickets"></i> 订单状态</label>
            <el-select v-model="queryParams.status" placeholder="全部状态" clearable @change="loadData">
              <el-option label="已完成" :value="2"></el-option>
              <el-option label="已取消" :value="3"></el-option>
            </el-select>
          </div>
        </el-col>
        <el-col :span="6" class="filter-actions">
          <el-button type="primary" icon="el-icon-search" @click="loadData">查询</el-button>
          <el-button icon="el-icon-refresh-right" @click="resetQuery">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 消费记录列表 -->
    <el-card class="list-card" shadow="never" body-style="padding: 0">
      <div class="list-header">
        <div class="header-title">
          <i class="el-icon-document-copy"></i>
          <span>消费记录</span>
        </div>
        <div class="header-tip">共 {{ total }} 条记录</div>
      </div>

      <div v-loading="loading" class="list-content">
        <!-- 卡片列表模式 - 更美观 -->
        <div v-if="consumptionList.length > 0" class="record-list">
          <div v-for="item in consumptionList" :key="item.id" class="record-card" @click="viewDetail(item)">
            <div class="card-header">
              <div class="order-info">
                <span class="order-no">{{ item.bookingNo }}</span>
                <el-tag :type="item.status === 2 ? 'success' : 'info'" size="mini" effect="plain">
                  {{ statusMap[item.status] }}
                </el-tag>
              </div>
              <div class="order-time">{{ formatDate(item.startTime || item.createTime) }}</div>
            </div>

            <div class="card-body">
              <div class="service-info">
                <div class="service-icon">
                  <i class="el-icon-car"></i>
                </div>
                <div class="service-detail">
                  <div class="service-name">{{ item.comboName || getComboName(item.comboMinutes) }}</div>
                  <div class="service-car">{{ item.carNumber }}</div>
                </div>
              </div>

              <div class="amount-info">
                <div class="amount-value">¥{{ formatMoney(item.amount) }}</div>
                <div class="amount-label">实付金额</div>
              </div>
            </div>

            <div class="card-footer">
              <div class="dept-info">
                <i class="el-icon-location"></i>
                {{ item.deptName || getDeptName(item.deptId) }}
              </div>
              <div class="action-btn" v-if="item.status === 2 && !item.hasReview">
                <el-button type="text" size="small" @click.stop="goReview(item)">去评价</el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else-if="!loading" class="empty-state">
          <div class="empty-icon">
            <i class="el-icon-shopping-cart"></i>
          </div>
          <p>暂无消费记录</p>
          <span>去预约洗车，开启第一次消费吧~</span>
          <el-button type="primary" size="small" @click="goBooking">立即预约</el-button>
        </div>
      </div>

      <div class="pagination-wrapper" v-if="total > 0">
        <pagination
          :total="total"
          :page.sync="queryParams.pageNum"
          :limit.sync="queryParams.pageSize"
          @pagination="loadData" />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog title="订单详情" :visible.sync="detailVisible" width="500px" class="detail-dialog">
      <div v-if="currentDetail" class="detail-content">
        <div class="detail-section">
          <div class="section-title">订单信息</div>
          <div class="detail-row">
            <span class="label">订单号：</span>
            <span class="value">{{ currentDetail.bookingNo }}</span>
          </div>
          <div class="detail-row">
            <span class="label">订单状态：</span>
            <el-tag :type="currentDetail.status === 2 ? 'success' : 'info'" size="small">
              {{ statusMap[currentDetail.status] }}
            </el-tag>
          </div>
          <div class="detail-row">
            <span class="label">消费时间：</span>
            <span class="value">{{ formatDate(currentDetail.startTime || currentDetail.createTime) }}</span>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">服务信息</div>
          <div class="detail-row">
            <span class="label">服务套餐：</span>
            <span class="value">{{ currentDetail.comboName || getComboName(currentDetail.comboMinutes) }}</span>
          </div>
          <div class="detail-row">
            <span class="label">车牌号码：</span>
            <span class="value">{{ currentDetail.carNumber }}</span>
          </div>
          <div class="detail-row">
            <span class="label">服务门店：</span>
            <span class="value">{{ currentDetail.deptName || getDeptName(currentDetail.deptId) }}</span>
          </div>
          <div class="detail-row">
            <span class="label">预约车位：</span>
            <span class="value">{{ currentDetail.spaceNo || '—' }}</span>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">支付信息</div>
          <div class="detail-row amount-row">
            <span class="label">实付金额：</span>
            <span class="value amount">¥{{ formatMoney(currentDetail.amount) }}</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listMyHistory } from '@/api/booking/booking'
import { getDeptList } from '@/api/booking/booking'

export default {
  name: 'MyConsumption',
  data() {
    return {
      loading: false,
      consumptionList: [],
      total: 0,
      totalAmount: 0,
      totalOrders: 0,
      avgAmount: 0,
      dateRange: [],
      detailVisible: false,
      currentDetail: null,
      deptMap: {},
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        startTime: '',
        endTime: '',
        keyword: '',
        status: ''
      },
      statusMap: { 0: '待到店', 1: '服务中', 2: '已完成', 3: '已取消' }
    }
  },
  computed: {
    stats() {
      return [
        {
          label: '累计消费',
          value: `¥${this.formatMoney(this.totalAmount)}`,
          icon: 'el-icon-money',
          gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
        },
        {
          label: '累计订单',
          value: this.totalOrders,
          icon: 'el-icon-receiving',
          gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
        },
        {
          label: '平均消费',
          value: `¥${this.avgAmount}`,
          icon: 'el-icon-star-on',
          gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
        },
        {
          label: '服务车次',
          value: this.totalOrders,
          icon: 'el-icon-car',
          gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
        }
      ]
    }
  },
  created() {
    this.loadData()
    this.loadDepts()
  },
  methods: {
    loadData() {
      this.loading = true
      listMyHistory(this.queryParams).then(res => {
        this.consumptionList = res.rows || []
        this.total = res.total || 0
        this.calcStats()
        this.loading = false
      }).catch(() => {
        this.loading = false
        this.$message.error('获取消费记录失败')
      })
    },
    calcStats() {
      let totalAmount = 0
      let totalOrders = this.consumptionList.length
      this.consumptionList.forEach(item => {
        totalAmount += parseFloat(item.amount || 0)
      })
      this.totalAmount = totalAmount
      this.totalOrders = totalOrders
      this.avgAmount = totalOrders > 0 ? (totalAmount / totalOrders).toFixed(2) : 0
    },
    loadDepts() {
      getDeptList().then(res => {
        if (res.rows) {
          res.rows.forEach(dept => {
            this.deptMap[dept.deptId] = dept.deptName
          })
        }
      })
    },
    getDeptName(deptId) {
      return this.deptMap[deptId] || '未知门店'
    },
    getComboName(minutes) {
      const map = { 30: '标准洗车', 60: '精洗+打蜡', 90: '全套护理' }
      return map[minutes] || '未知套餐'
    },
    handleDateChange(val) {
      if (val) {
        this.queryParams.startTime = val[0]
        this.queryParams.endTime = val[1]
      } else {
        this.queryParams.startTime = ''
        this.queryParams.endTime = ''
      }
      this.queryParams.pageNum = 1
      this.loadData()
    },
    resetQuery() {
      this.dateRange = []
      this.queryParams.startTime = ''
      this.queryParams.endTime = ''
      this.queryParams.keyword = ''
      this.queryParams.status = ''
      this.queryParams.pageNum = 1
      this.loadData()
    },
    formatMoney(value) {
      if (!value) return '0.00'
      return parseFloat(value).toFixed(2)
    },
    formatDate(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
    },
    viewDetail(item) {
      this.currentDetail = item
      this.detailVisible = true
    },
    goReview(item) {
      this.$router.push({
        path: '/booking/createReview',
        query: { bookingId: item.id, bookingNo: item.bookingNo }
      })
    },
    goBooking() {
      this.$router.push('/booking/deptSelect')
    }
  }
}
</script>

<style scoped lang="scss">
.consumption-page {
  padding: 24px;
  background: #f5f7fb;
  min-height: 100vh;

  .stats-wrapper {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 24px;

    .stat-card {
      background: white;
      border-radius: 20px;
      padding: 20px;
      display: flex;
      align-items: center;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
      transition: all 0.3s;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
      }

      .stat-icon {
        width: 56px;
        height: 56px;
        border-radius: 18px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 16px;

        i {
          font-size: 28px;
          color: white;
        }
      }

      .stat-content {
        flex: 1;

        .stat-value {
          font-size: 26px;
          font-weight: bold;
          color: #1f2f3d;
          line-height: 1.2;
        }

        .stat-label {
          font-size: 13px;
          color: #8a8f9d;
          margin-top: 4px;
        }

        .stat-trend {
          font-size: 12px;
          margin-top: 6px;
          color: #67c23a;
        }
      }
    }
  }

  .filter-card {
    border-radius: 16px;
    margin-bottom: 24px;
    border: none;

    ::v-deep .el-card__body {
      padding: 20px 24px;
    }

    .filter-item {
      label {
        display: block;
        font-size: 12px;
        color: #8a8f9d;
        margin-bottom: 8px;

        i {
          margin-right: 4px;
        }
      }

      .filter-date {
        width: 100%;
      }
    }

    .filter-actions {
      display: flex;
      align-items: flex-end;
      gap: 12px;

      .el-button {
        margin-left: 0;
      }
    }
  }

  .list-card {
    border-radius: 16px;
    border: none;
    overflow: hidden;

    .list-header {
      padding: 18px 24px;
      border-bottom: 1px solid #f0f0f0;
      display: flex;
      justify-content: space-between;
      align-items: center;
      background: white;

      .header-title {
        font-size: 16px;
        font-weight: 600;
        color: #1f2f3d;

        i {
          margin-right: 8px;
          color: #409eff;
        }
      }

      .header-tip {
        font-size: 13px;
        color: #8a8f9d;
      }
    }

    .list-content {
      min-height: 400px;
    }

    .record-list {
      padding: 20px 24px;
      display: flex;
      flex-direction: column;
      gap: 16px;

      .record-card {
        background: #fafbfc;
        border-radius: 16px;
        padding: 16px 20px;
        cursor: pointer;
        transition: all 0.2s;
        border: 1px solid #f0f0f0;

        &:hover {
          background: white;
          border-color: #e6e9f0;
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
        }

        .card-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 14px;

          .order-info {
            display: flex;
            align-items: center;
            gap: 12px;

            .order-no {
              font-family: monospace;
              font-size: 14px;
              font-weight: 500;
              color: #409eff;
              letter-spacing: 0.5px;
            }
          }

          .order-time {
            font-size: 12px;
            color: #8a8f9d;
          }
        }

        .card-body {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 14px;

          .service-info {
            display: flex;
            align-items: center;
            gap: 12px;

            .service-icon {
              width: 44px;
              height: 44px;
              background: #ecf5ff;
              border-radius: 14px;
              display: flex;
              align-items: center;
              justify-content: center;

              i {
                font-size: 22px;
                color: #409eff;
              }
            }

            .service-detail {
              .service-name {
                font-size: 15px;
                font-weight: 500;
                color: #2c3e50;
                margin-bottom: 4px;
              }

              .service-car {
                font-size: 12px;
                color: #8a8f9d;
              }
            }
          }

          .amount-info {
            text-align: right;

            .amount-value {
              font-size: 20px;
              font-weight: bold;
              color: #f56c6c;
            }

            .amount-label {
              font-size: 11px;
              color: #8a8f9d;
              margin-top: 2px;
            }
          }
        }

        .card-footer {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding-top: 12px;
          border-top: 1px dashed #ebeef5;

          .dept-info {
            font-size: 12px;
            color: #8a8f9d;

            i {
              margin-right: 4px;
              font-size: 12px;
            }
          }

          .action-btn {
            .el-button {
              padding: 0;
            }
          }
        }
      }
    }

    .empty-state {
      text-align: center;
      padding: 80px 20px;

      .empty-icon {
        width: 100px;
        height: 100px;
        margin: 0 auto 20px;
        background: #f5f7fa;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;

        i {
          font-size: 48px;
          color: #c0c4cc;
        }
      }

      p {
        font-size: 16px;
        color: #5a5e66;
        margin: 0 0 8px;
      }

      span {
        display: block;
        font-size: 13px;
        color: #8a8f9d;
        margin-bottom: 20px;
      }
    }

    .pagination-wrapper {
      padding: 16px 24px;
      border-top: 1px solid #f0f0f0;
      display: flex;
      justify-content: flex-end;
    }
  }
}

.detail-dialog {
  ::v-deep .el-dialog {
    border-radius: 20px;
  }

  .detail-content {
    .detail-section {
      margin-bottom: 20px;
      padding-bottom: 16px;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
        margin-bottom: 0;
        padding-bottom: 0;
      }

      .section-title {
        font-size: 14px;
        font-weight: 600;
        color: #2c3e50;
        margin-bottom: 12px;
        padding-left: 10px;
        border-left: 3px solid #409eff;
      }

      .detail-row {
        display: flex;
        margin-bottom: 10px;
        font-size: 14px;

        .label {
          width: 90px;
          color: #8a8f9d;
        }

        .value {
          color: #2c3e50;
          flex: 1;

          &.amount {
            font-size: 18px;
            font-weight: bold;
            color: #f56c6c;
          }
        }
      }
    }
  }
}
</style>
