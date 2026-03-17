<template>
  <el-card class="box-card" style="margin-top: 20px">
    <div slot="header" class="clearfix">
      <span>历史记录</span>
    </div>
    <el-table :data="myHistoryList" style="width: 100%" empty-text="暂无历史记录">
      <el-table-column prop="workDate" label="日期" width="120"/>
      <el-table-column prop="spaceNo" label="车位" width="80"/>
      <el-table-column label="时段" width="150">
        <template slot-scope="scope">
          {{ scope.row.slot }} - {{ scope.row.endSlot }}
        </template>
      </el-table-column>
      <el-table-column prop="carNumber" label="车牌" width="100"/>
      <el-table-column prop="comboName" label="套餐" width="120"/>
      <el-table-column label="状态" width="90">
        <template slot-scope="scope">
          <el-tag :type="getHistoryStatusType(scope.row.status)" size="small">
            {{ statusMap[scope.row.status] }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
        v-if="historyTotal > 0"
        :current-page="historyPageNum"
        :page-size="historyPageSize"
        :total="historyTotal"
        layout="total, prev, pager, next"
        @current-change="handleHistoryPageChange"
        style="margin-top: 10px; text-align: right"
    />
  </el-card>
</template>

<script>
import { listMyHistory } from '@/api/booking/booking'

export default {
  name: 'BookingHistory',
  data() {
    return {
      myHistoryList: [],
      historyPageNum: 1,
      historyPageSize: 10,
      historyTotal: 0,
      statusMap: {0: '待到店', 1: '服务中', 2: '已完成', 3: '已取消'}
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    // 暴露给父组件的方法：加载数据
    loadData() {
      listMyHistory({pageNum: this.historyPageNum, pageSize: this.historyPageSize}).then(res => {
        this.myHistoryList = res.data.list.map(b => ({
          ...b,
          slot: b.startTime ? this.formatTime(b.startTime) : '',
          endSlot: b.endTime ? this.formatTime(b.endTime) : '',
          comboName: this.getComboName(b.comboMinutes)
        }))
        this.historyTotal = res.data.total
      })
    },

    handleHistoryPageChange(val) {
      this.historyPageNum = val
      this.loadData()
    },

    getHistoryStatusType(status) {
      const map = {0: 'info', 1: 'success', 2: 'success', 3: 'danger'}
      return map[status] || 'info'
    },

    formatTime(dateStr) {
      const date = new Date(dateStr)
      return date.toLocaleTimeString('zh-CN', {hour: '2-digit', minute: '2-digit', hour12: false})
    },

    getComboName(minutes) {
      const map = {30: '标准洗车', 60: '精洗+打蜡', 90: '全套护理'}
      return map[minutes] || '未知套餐'
    }
  }
}
</script>

<style scoped>
.box-card {
  margin-top: 20px;
}

.clearfix {
  font-weight: bold;
}
</style>
