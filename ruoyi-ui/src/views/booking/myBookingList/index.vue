<template>
  <el-card class="box-card" style="margin-top: 20px">
    <div slot="header" class="clearfix">
      <span>我的预约（进行中）</span>
    </div>
    <el-table :data="myBookList" style="width: 100%" empty-text="暂无进行中的预约">
      <el-table-column prop="workDate" label="日期" width="120"/>
      <el-table-column prop="spaceNo" label="车位" width="80"/>
      <el-table-column label="时段" width="150">
        <template slot-scope="scope">
          {{ scope.row.slot }} - {{ scope.row.endSlot }}
        </template>
      </el-table-column>
      <el-table-column prop="carNumber" label="车牌" width="100"/>
      <el-table-column prop="comboName" label="套餐" width="120"/>
      <el-table-column prop="code" label="验证码" width="90"/>
      <el-table-column label="状态" width="90">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 0 ? 'warning' : 'success'" size="small">
            {{ statusMap[scope.row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template slot-scope="scope">
          <el-button v-if="scope.row.status === 0" type="text" style="color: #f56c6c" @click="cancelBook(scope.row)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script>
import { listMyBooking, cancelBooking } from '@/api/booking/booking'

export default {
  name: 'MyBookingList',
  data() {
    return {
      myBookList: [],
      statusMap: {0: '待到店', 1: '服务中', 2: '已完成', 3: '已取消'}
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    // 暴露给父组件的方法：加载数据
    loadData() {
      listMyBooking({statusList: [0, 1], onlyNotExpired: true}).then(res => {
        this.myBookList = res.data.map(b => ({
          ...b,
          slot: b.startTime ? this.formatTime(b.startTime) : '',
          endSlot: b.endTime ? this.formatTime(b.endTime) : '',
          comboName: this.getComboName(b.comboMinutes)
        }))
      })
    },

    cancelBook(row) {
      this.$confirm('确认取消该预约？', '提示', {type: 'warning'}).then(() => {
        cancelBooking(row.id).then(() => {
          this.$message.success('预约已取消')
          this.loadData()
          this.$emit('cancel-success')
        })
      })
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
