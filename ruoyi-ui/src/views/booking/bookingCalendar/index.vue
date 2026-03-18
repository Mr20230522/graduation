<template>
  <div>
    <!-- 原有日历内容保持不变 -->
    <el-row :gutter="20">
      <!-- 左侧 7 天日期栏 -->
      <el-col :span="4">
        <div class="date-bar">
          <div v-for="d in dateList" :key="d.date"
               :class="{active: selectedDate === d.date, expired: d.isExpired}"
               class="date-item" @click="!d.isExpired && selectDate(d.date)">
            <div class="day">{{ d.day }}</div>
            <div class="date">{{ d.dateStr }}</div>
            <div v-if="d.isExpired" class="expired-tag">已过期</div>
          </div>
        </div>
      </el-col>

      <!-- 右侧车位-时段矩阵 -->
      <el-col :span="20">
        <div class="matrix-wrapper">
          <div v-if="spaces.length === 0" class="empty-tip">
            该日期暂无可用车位或排班信息
          </div>
          <div v-for="space in spaces" :key="space" class="space-row">
            <div class="space-title">{{ space }}</div>
            <div class="slot-list">
              <div v-for="(slot, idx) in slots" :key="idx"
                   :class="getSlotClass(space, slot)"
                   class="slot-item"
                   @click="handleSlotClick(space, slot)">
                {{ formatSlotDisplay(slot) }}
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 第一步：填写预约信息弹窗 -->
    <el-dialog title="填写预约信息" :visible.sync="bookVisible" width="420px" @close="resetBookForm">
      <el-form :model="bookForm" label-width="90px">
        <el-form-item label="预约日期">
          <div class="display-text date-text">{{ formatDisplayDate(bookForm.workDate) }}</div>
        </el-form-item>
        <el-form-item label="车位编号">
          <div class="display-text space-text">{{ bookForm.spaceNo }}</div>
        </el-form-item>
        <el-form-item label="预约时段">
          <div class="display-text time-text">
            {{ bookForm.slot }}
          </div>
        </el-form-item>
        <el-form-item label="服务套餐">
          <el-select v-model="bookForm.comboMinutes" placeholder="请选择服务套餐" style="width: 100%">
            <el-option label="标准洗车 30分钟" :value="30"/>
            <el-option label="精洗+打蜡 60分钟" :value="60"/>
            <el-option label="全套护理 90分钟" :value="90"/>
          </el-select>
        </el-form-item>
        <el-form-item label="结束时间">
          <div class="display-text end-time-text">{{ calculateEndTime(bookForm.slot, bookForm.comboMinutes) }}</div>
        </el-form-item>
        <el-form-item label="车牌号码">
          <el-input v-model="bookForm.carNumber" placeholder="例：京A12345" maxlength="10"/>
        </el-form-item>
        <el-form-item label="车辆型号">
          <el-input v-model="bookForm.carModel" placeholder="例：宝马3系" maxlength="20"/>
        </el-form-item>
        <el-form-item label="车辆颜色">
          <el-input v-model="bookForm.carColor" placeholder="例：白色" maxlength="10"/>
        </el-form-item>
        <el-form-item label="金额">
          <div class="display-text amount-text">¥ {{ getComboPrice(bookForm.comboMinutes) }}</div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="bookVisible = false">取 消</el-button>
        <el-button type="primary" @click="createOrder" :loading="orderLoading">下一步：去支付</el-button>
      </div>
    </el-dialog>

    <!-- 第二步：支付弹窗 -->
    <el-dialog title="订单支付" :visible.sync="payVisible" width="500px" @close="handlePayClose" :close-on-click-modal="false">
      <div class="pay-container">
        <!-- 订单信息 -->
        <div class="order-info" v-if="currentOrder.orderNo">
          <h4>订单信息</h4>
          <div class="info-item">
            <span>订单号：</span>
            <span class="order-no">{{ currentOrder.orderNo }}</span>
          </div>
          <div class="info-item">
            <span>预约时间：</span>
            <span>{{ bookForm.workDate }} {{ bookForm.slot }}</span>
          </div>
          <div class="info-item">
            <span>车位：</span>
            <span>{{ bookForm.spaceNo }}</span>
          </div>
          <div class="info-item">
            <span>套餐：</span>
            <span>{{ getComboName(bookForm.comboMinutes) }}</span>
          </div>
          <div class="info-item">
            <span>车牌：</span>
            <span>{{ bookForm.carNumber }}</span>
          </div>
        </div>

        <!-- 金额 -->
        <div class="amount-info">
          <span>应付金额：</span>
          <span class="amount">¥ {{ currentOrder.amount || getComboPrice(bookForm.comboMinutes) }}</span>
        </div>

        <!-- 过期时间倒计时 -->
        <div class="expire-info" v-if="expireTime">
          <el-alert
            :title="'请在 ' + expireTime + ' 内完成支付'"
            type="warning"
            :closable="false">
          </el-alert>
        </div>

        <!-- 支付方式选择 -->
        <div class="pay-methods" v-if="!showQrCode">
          <h4>选择支付方式</h4>
          <div class="method-list">
            <div class="method-item"
                 :class="{ active: payMethod === 'alipay' }"
                 @click="payMethod = 'alipay'">
              <i class="el-icon-s-finance"></i>
              <span>支付宝</span>
            </div>
            <div class="method-item"
                 :class="{ active: payMethod === 'wechat' }"
                 @click="payMethod = 'wechat'">
              <i class="el-icon-wechat"></i>
              <span>微信支付</span>
            </div>
          </div>
        </div>

        <!-- 二维码区域（扫码支付） -->
        <div v-if="showQrCode" class="qr-code">
          <img :src="qrCodeUrl" alt="支付二维码">
          <p>请使用{{ payMethod === 'alipay' ? '支付宝' : '微信' }}扫码支付</p>
          <p class="qr-tip">支付完成后请点击"我已支付"</p>
        </div>

        <!-- 支付状态提示 -->
        <div v-if="payStatus === 'paying'" class="pay-status paying">
          <i class="el-icon-loading"></i>
          <span>支付处理中，请稍候...</span>
        </div>
        <div v-if="payStatus === 'success'" class="pay-status success">
          <i class="el-icon-success"></i>
          <span>支付成功！正在创建预约...</span>
        </div>
        <div v-if="payStatus === 'fail'" class="pay-status fail">
          <i class="el-icon-error"></i>
          <span>支付失败，请重试</span>
        </div>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelPay" :disabled="payStatus === 'paying'">取 消</el-button>
        <el-button v-if="!showQrCode" type="success" @click="handlePay" :loading="payLoading">
          确认支付
        </el-button>
        <el-button v-else type="success" @click="queryPayResult" :loading="payLoading">
          我已支付
        </el-button>
      </div>
    </el-dialog>

    <!-- 第三步：支付结果弹窗 -->
    <el-dialog title="支付结果" :visible.sync="resultVisible" width="400px">
      <div class="pay-result" :class="paySuccess ? 'success' : 'fail'">
        <i :class="paySuccess ? 'el-icon-success' : 'el-icon-error'"></i>
        <h3>{{ paySuccess ? '支付成功' : '支付失败' }}</h3>
        <p v-if="paySuccess">预约验证码：<span class="code">{{ bookingCode }}</span></p>
        <p v-else>{{ payErrorMsg || '支付失败，请重新预约' }}</p>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="finishPay">完成</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getCalendar, createBooking } from '@/api/booking/booking'
import { createOrder, payOrder, getPayStatus, cancelOrder, getComboPrice, getComboName } from '@/api/booking/booking'
import request from '@/utils/request'

export default {
  name: 'BookingCalendar',
  data() {
    return {
      // 原有数据
      refreshTimer: null,
      lastRefreshDate: '',
      deptId: 1,
      dateList: [],
      slots: [],
      spaces: [],
      bookingList: [],
      selectedDate: '',

      // 第一步：预约表单
      bookVisible: false,
      bookForm: {
        workDate: '',
        spaceNo: '',
        slot: '',
        carNumber: '',
        carModel: '',
        carColor: '',
        comboMinutes: 30
      },
      orderLoading: false,

      // 第二步：支付
      payVisible: false,
      currentOrder: {
        orderNo: '',
        amount: 0,
        expireTime: null
      },
      payMethod: 'alipay',
      payLoading: false,
      showQrCode: false,
      qrCodeUrl: '',
      payStatus: '', // 'paying', 'success', 'fail'
      expireTime: '',
      timer: null,
      payTimer: null,

      // 第三步：结果
      resultVisible: false,
      paySuccess: false,
      bookingCode: '',
      payErrorMsg: ''
    }
  },
  created() {
    this.initDeptId()
    this.startDailyRefresh()
  },
  beforeDestroy() {
    this.stopDailyRefresh()
    this.clearTimers()
  },
  methods: {
    // 初始化门店ID
    initDeptId() {
      const userInfo = this.$store.state.user.userInfo || {}
      if (userInfo.deptId && userInfo.deptId !== 0) {
        this.deptId = userInfo.deptId
        this.loadCalendar()
      } else {
        this.loadDeptList()
      }
    },

    // 加载门店列表
    loadDeptList() {
      request({
        url: '/system/dept/list',
        method: 'get',
        params: { status: 0 }
      }).then(res => {
        if (res.data && res.data.length > 0) {
          this.deptId = res.data[0].deptId
          this.loadCalendar()
        }
      }).catch(err => {
        console.error('加载门店列表失败', err)
        this.$message.error('加载门店列表失败')
      })
    },

    // 加载日历
    loadCalendar() {
      const now = new Date()
      const todayStr = this.getLocalDateStr(now)
      this.lastRefreshDate = todayStr

      getCalendar(this.deptId, todayStr).then(res => {
        const data = res.data

        // 生成7天日期列表
        this.dateList = []
        for (let i = 0; i < 7; i++) {
          const d = new Date(now)
          d.setDate(now.getDate() + i)
          d.setHours(0, 0, 0, 0)

          const dateStr = this.getLocalDateStr(d)

          let dayLabel
          if (i === 0) dayLabel = '今天'
          else if (i === 1) dayLabel = '明天'
          else dayLabel = this.getDayDesc(d)

          this.dateList.push({
            date: dateStr,
            dateStr: dateStr.substr(5),
            day: dayLabel,
            isExpired: dateStr < todayStr,
            isToday: i === 0
          })
        }

        // 默认选中今天
        this.selectedDate = this.dateList[0].date
        const todayData = data.schedule[this.selectedDate]
        this.renderDayData(todayData)
        this.loadDayBookings(this.selectedDate)
      }).catch(err => {
        this.$message.error('加载日历失败')
        console.error('加载日历失败:', err)
      })
    },

    // 切换日期
    selectDate(date) {
      this.selectedDate = date
      getCalendar(this.deptId, date).then(res => {
        const dayData = res.data.schedule[date]
        this.renderDayData(dayData)
        this.loadDayBookings(date)
      })
    },

    // 加载预约列表
    loadDayBookings(date) {
      request({
        url: '/booking/list',
        method: 'get',
        params: {deptId: this.deptId, workDate: date}
      }).then(res => {
        this.bookingList = res.data || []
      }).catch(() => {
        this.bookingList = []
      })
    },

    // 渲染单日数据
    renderDayData(dayData) {
      if (!dayData) {
        this.slots = []
        this.spaces = []
        return
      }
      this.slots = dayData.slots || []
      this.spaces = dayData.spaces || []
    },

    // 判断时段状态
    getSlotClass(space, slot) {
      const isBooked = this.isSlotBooked(space, slot)
      if (isBooked) return 'busy'
      if (this.isSlotExpired(slot)) return 'expired'
      return 'free'
    },

    // 检查时段是否已被预约
    isSlotBooked(space, slot) {
      if (!this.bookingList || this.bookingList.length === 0) return false

      const slotStart = new Date(this.selectedDate + ' ' + slot + ':00')
      const slotEnd = new Date(slotStart.getTime() + 30 * 60000)

      return this.bookingList.some(booking => {
        if (booking.spaceNo !== space) return false
        if (booking.status !== 0 && booking.status !== 1) return false

        const bookingStart = new Date(booking.startTime)
        const bookingEnd = new Date(booking.endTime)

        return bookingStart < slotEnd && bookingEnd > slotStart
      })
    },

    // 检查时段是否已过期
    isSlotExpired(slot) {
      const now = new Date()
      const slotTime = new Date(this.selectedDate + ' ' + slot + ':00')
      return slotTime < now
    },

    // 点击时段
    handleSlotClick(space, slot) {
      const slotClass = this.getSlotClass(space, slot)
      if (slotClass === 'free') {
        this.openBook(space, this.formatSlotDisplay(slot))
      } else if (slotClass === 'busy') {
        this.$message.info('该时段已被预约')
      } else if (slotClass === 'expired') {
        this.$message.warning('该时段已过期')
      }
    },

    // 打开预约弹窗
    openBook(space, slot) {
      if (!this.selectedDate) {
        this.$message.error('请先选择日期')
        return
      }
      this.bookForm = {
        workDate: this.selectedDate,
        spaceNo: space,
        slot: slot,
        carNumber: '',
        carModel: '',
        carColor: '',
        comboMinutes: 30
      }
      this.bookVisible = true
    },

    // 重置预约表单
    resetBookForm() {
      // 不清空，只是关闭时不做特殊处理
    },

    // ==================== 支付相关方法 ====================

    // 第一步：创建订单
    async createOrder() {
      if (!this.bookForm.carNumber.trim()) {
        this.$message.error('请输入车牌号码')
        return
      }

      this.orderLoading = true

      try {
        // 计算开始时间
        const timeStr = this.bookForm.slot + ':00'
        const startTimeStr = this.bookForm.workDate + ' ' + timeStr

        const res = await createOrder({
          deptId: this.deptId,
          workDate: this.bookForm.workDate,
          spaceNo: this.bookForm.spaceNo,
          slot: this.bookForm.slot,
          comboMinutes: this.bookForm.comboMinutes,
          carNumber: this.bookForm.carNumber.trim(),
          carModel: this.bookForm.carModel.trim(),
          carColor: this.bookForm.carColor.trim()
        })

        // 保存订单信息
        this.currentOrder = res.data

        // 设置过期时间倒计时
        if (this.currentOrder.expireTime) {
          this.startExpireCountdown(this.currentOrder.expireTime)
        }

        // 关闭预约弹窗，打开支付弹窗
        this.bookVisible = false
        this.payVisible = true
        this.payStatus = ''
        this.showQrCode = false

      } catch (error) {
        this.$message.error(error.msg || '创建订单失败')
      } finally {
        this.orderLoading = false
      }
    },

    // 第二步：发起支付
    async handlePay() {
      this.payLoading = true

      try {
        const res = await payOrder({
          orderNo: this.currentOrder.orderNo,
          payMethod: this.payMethod
        })

        if (res.data.qrCode) {
          // 显示二维码
          this.showQrCode = true
          this.qrCodeUrl = res.data.qrCode
          // 开始轮询查询支付结果
          this.startPayPolling()
        } else if (res.data.payUrl) {
          // 跳转到支付页面
          window.location.href = res.data.payUrl
          // 开始轮询（跳转后返回时继续查询）
          this.startPayPolling()
        }

      } catch (error) {
        this.$message.error(error.msg || '支付发起失败')
        this.payStatus = 'fail'
      } finally {
        this.payLoading = false
      }
    },

    // 开始轮询支付结果
    startPayPolling() {
      this.payStatus = 'paying'
      let count = 0
      this.payTimer = setInterval(async () => {
        try {
          const res = await getPayStatus(this.currentOrder.orderNo)

          if (res.data.status === 2) { // 已支付
            this.payStatus = 'success'
            clearInterval(this.payTimer)
            // 支付成功，创建预约
            await this.createBookingAfterPay()

          } else if (res.data.status === 3) { // 已取消/过期
            this.payStatus = 'fail'
            this.payErrorMsg = '订单已过期'
            clearInterval(this.payTimer)

          }

          // 轮询30次（约90秒）后停止
          count++
          if (count > 30) {
            clearInterval(this.payTimer)
            if (this.payStatus !== 'success') {
              this.payStatus = 'fail'
              this.payErrorMsg = '支付超时'
            }
          }

        } catch (error) {
          console.error('查询支付状态失败', error)
        }
      }, 3000) // 每3秒查询一次
    },

    // 手动查询支付结果（点击"我已支付"）
    async queryPayResult() {
      this.payLoading = true
      try {
        const res = await getPayStatus(this.currentOrder.orderNo)

        if (res.data.status === 2) {
          // 支付成功
          clearInterval(this.payTimer)
          this.payStatus = 'success'
          await this.createBookingAfterPay()
        } else if (res.data.status === 3) {
          this.$message.error('订单已过期')
          this.payVisible = false
          this.bookVisible = true
        } else {
          this.$message.info('暂未检测到支付，请确认是否已完成支付')
        }
      } catch (error) {
        this.$message.error('查询失败')
      } finally {
        this.payLoading = false
      }
    },

    // 第三步：支付成功后创建预约
    async createBookingAfterPay() {
      try {
        const startTime = this.bookForm.workDate + ' ' + this.bookForm.slot + ':00'

        const bookingData = {
          deptId: this.deptId,
          spaceNo: this.bookForm.spaceNo,
          workDate: this.bookForm.workDate,
          startTime: startTime,
          comboMinutes: this.bookForm.comboMinutes,
          comboName: getComboName(this.bookForm.comboMinutes),
          carNumber: this.bookForm.carNumber.trim(),
          carModel: this.bookForm.carModel.trim(),
          carColor: this.bookForm.carColor.trim()
        }

        const res = await createBooking(bookingData, this.currentOrder.orderNo)

        // 保存预约信息
        this.bookingCode = res.data.code
        this.paySuccess = true

        // 关闭支付弹窗，打开结果弹窗
        this.payVisible = false
        this.resultVisible = true

        // 刷新日历数据
        this.loadDayBookings(this.selectedDate)
        this.$emit('booking-success')

      } catch (error) {
        this.paySuccess = false
        this.payErrorMsg = error.msg || '创建预约失败'
        this.payVisible = false
        this.resultVisible = true
      }
    },

    // 取消支付
    async cancelPay() {
      if (this.currentOrder.orderNo) {
        try {
          await cancelOrder(this.currentOrder.orderNo)
        } catch (error) {
          console.error('取消订单失败', error)
        }
      }
      this.payVisible = false
      this.bookVisible = true
      this.clearTimers()
    },

    // 支付弹窗关闭时
    handlePayClose() {
      this.clearTimers()
      this.showQrCode = false
      this.qrCodeUrl = ''
      this.payStatus = ''
    },

    // 完成支付
    finishPay() {
      this.resultVisible = false
      if (this.paySuccess) {
        // 可以跳转到我的预约页面
        // this.$router.push('/myBooking')
      }
    },

    // 开始过期倒计时
    startExpireCountdown(expireTime) {
      const end = new Date(expireTime).getTime()
      this.timer = setInterval(() => {
        const now = new Date().getTime()
        const distance = end - now

        if (distance < 0) {
          this.expireTime = '已过期'
          clearInterval(this.timer)
          return
        }

        const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60))
        const seconds = Math.floor((distance % (1000 * 60)) / 1000)
        this.expireTime = `${minutes}分${seconds}秒`
      }, 1000)
    },

    // 清除所有定时器
    clearTimers() {
      if (this.timer) {
        clearInterval(this.timer)
        this.timer = null
      }
      if (this.payTimer) {
        clearInterval(this.payTimer)
        this.payTimer = null
      }
    },

    // ==================== 工具方法 ====================

    getComboName(minutes) {
      return getComboName(minutes)
    },

    getComboPrice(minutes) {
      return getComboPrice(minutes)
    },

    // 计算结束时间
    calculateEndTime(startSlot, minutes) {
      if (!startSlot || !minutes) return '--:--'
      const [hours, mins] = startSlot.split(':').map(Number)
      const startDate = new Date()
      startDate.setHours(hours, mins, 0, 0)
      const endDate = new Date(startDate.getTime() + minutes * 60000)
      return endDate.toLocaleTimeString('zh-CN', {hour: '2-digit', minute: '2-digit', hour12: false})
    },

    // 格式化显示日期
    formatDisplayDate(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      const weekDays = ['日', '一', '二', '三', '四', '五', '六']
      return `${dateStr}（星期${weekDays[date.getDay()]}）`
    },

    // 获取星期描述
    getDayDesc(date) {
      const days = ['日', '一', '二', '三', '四', '五', '六']
      return '周' + days[date.getDay()]
    },

    // 格式化时段显示
    formatSlotDisplay(slot) {
      return typeof slot === 'string' ? slot : slot.time || slot.label
    },

    // 获取本地日期字符串
    getLocalDateStr(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },

    // 刷新当前日期
    refreshCurrentDay() {
      if (this.selectedDate) {
        this.loadDayBookings(this.selectedDate)
      }
    },

    // 每日刷新相关
    scheduleNextDayRefresh() {
      const now = new Date()
      const tomorrow = new Date(now)
      tomorrow.setDate(tomorrow.getDate() + 1)
      tomorrow.setHours(0, 0, 0, 0)

      const msUntilMidnight = tomorrow - now

      setTimeout(() => {
        console.log('凌晨0点，自动刷新日历')
        this.loadCalendar()
        this.scheduleNextDayRefresh()
      }, msUntilMidnight)
    },

    stopDailyRefresh() {
      if (this.refreshTimer) {
        clearInterval(this.refreshTimer)
      }
    },

    startDailyRefresh() {
      this.refreshTimer = setInterval(() => {
        const today = this.getLocalDateStr(new Date())
        if (this.lastRefreshDate && this.lastRefreshDate !== today) {
          console.log('日期变了，重新加载日历')
          this.loadCalendar()
        }
      }, 60000)

      this.scheduleNextDayRefresh()
    }
  }
}
</script>

<style scoped>
/* 原有样式保持不变，新增支付相关样式 */

.pay-container {
  padding: 10px;
}

.order-info {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 15px;
}

.order-info h4 {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 16px;
  color: #333;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin: 8px 0;
  font-size: 14px;
}

.order-no {
  color: #409eff;
  font-weight: bold;
}

.amount-info {
  text-align: right;
  padding: 15px;
  font-size: 16px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 15px;
}

.amount {
  color: #f56c6c;
  font-size: 28px;
  font-weight: bold;
  margin-left: 10px;
}

.amount-text {
  color: #f56c6c;
  font-weight: bold;
  font-size: 18px;
}

.expire-info {
  margin-bottom: 20px;
}

.pay-methods h4 {
  margin-bottom: 15px;
  font-size: 15px;
}

.method-list {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.method-item {
  flex: 1;
  padding: 15px;
  border: 2px solid #dcdfe6;
  border-radius: 4px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.method-item:hover {
  border-color: #409eff;
}

.method-item.active {
  border-color: #67c23a;
  background: #f0f9eb;
}

.method-item i {
  font-size: 32px;
  display: block;
  margin-bottom: 8px;
}

.qr-code {
  text-align: center;
  padding: 20px;
}

.qr-code img {
  width: 200px;
  height: 200px;
  margin-bottom: 10px;
  border: 1px solid #dcdfe6;
}

.qr-tip {
  color: #e6a23c;
  font-size: 14px;
}

.pay-status {
  text-align: center;
  padding: 15px;
  margin: 10px 0;
  border-radius: 4px;
}

.pay-status.paying {
  background: #e6f7ff;
  color: #1890ff;
}

.pay-status.success {
  background: #f6ffed;
  color: #52c41a;
}

.pay-status.fail {
  background: #fff2f0;
  color: #f5222d;
}

.pay-status i {
  font-size: 24px;
  margin-right: 8px;
  vertical-align: middle;
}

.pay-result {
  text-align: center;
  padding: 30px;
}

.pay-result i {
  font-size: 64px;
}

.pay-result.success i {
  color: #67c23a;
}

.pay-result.fail i {
  color: #f56c6c;
}

.pay-result h3 {
  margin: 15px 0;
  font-size: 20px;
}

.pay-result .code {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  letter-spacing: 2px;
}

/* 原有样式继续保留 */
.date-bar {
  border-right: 1px solid #ebeef5;
  padding-right: 10px;
  max-height: 600px;
  overflow-y: auto;
}

.date-item {
  padding: 12px;
  margin-bottom: 8px;
  border-radius: 4px;
  cursor: pointer;
  text-align: center;
  background: #f5f7fa;
  transition: all 0.3s;
  position: relative;
}

.date-item:hover {
  background: #e4e7ed;
}

.date-item.active {
  background: #409eff;
  color: #fff;
}

.date-item.expired {
  opacity: 0.6;
  cursor: not-allowed;
  background: #dcdcdc;
}

.date-item .day {
  font-size: 12px;
  margin-bottom: 4px;
}

.date-item .date {
  font-weight: bold;
  font-size: 14px;
}

.expired-tag {
  font-size: 10px;
  color: #f56c6c;
  margin-top: 2px;
}

.matrix-wrapper {
  padding-left: 10px;
}

.empty-tip {
  text-align: center;
  color: #909399;
  padding: 40px;
}

.space-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  padding: 5px 0;
  border-bottom: 1px solid #f0f0f0;
}

.space-title {
  width: 60px;
  font-weight: bold;
  text-align: center;
  background: #409eff;
  color: #fff;
  padding: 10px 0;
  margin-right: 15px;
  border-radius: 4px;
  font-size: 14px;
}

.slot-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  flex: 1;
}

.slot-item {
  min-width: 70px;
  padding: 8px 12px;
  text-align: center;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
  font-weight: 500;
}

.slot-item:hover {
  transform: scale(1.05);
}

.slot-item.free {
  background: #67c23a;
  color: #fff;
}

.slot-item.busy {
  background: #f56c6c;
  color: #fff;
  cursor: not-allowed;
  opacity: 0.8;
}

.slot-item.expired {
  background: #909399;
  color: #fff;
  cursor: not-allowed;
  opacity: 0.6;
}

.display-text {
  padding: 0 15px;
  height: 40px;
  line-height: 40px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: bold;
  border: 1px solid #dcdfe6;
  background: #f5f7fa;
}

.date-text {
  color: #409eff;
}

.space-text {
  color: #67c23a;
}

.time-text {
  color: #e6a23c;
}

.end-time-text {
  color: #909399;
}

.dialog-footer {
  text-align: right;
}
</style>
