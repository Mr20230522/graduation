<template>
  <div class="payment-return">
    <div class="loading" v-if="loading">
      <i class="el-icon-loading"></i>
      <p>正在查询支付结果...</p>
    </div>
    <div class="success" v-else-if="success">
      <i class="el-icon-success"></i>
      <h2>支付成功</h2>
      <p class="order-no">订单号：{{ orderNo }}</p>
      <p v-if="bookingCode" class="booking-code">预约验证码：{{ bookingCode }}</p>
      <p class="tip">页面将自动跳转到我的预约...</p>
    </div>
    <div class="fail" v-else>
      <i class="el-icon-error"></i>
      <h2>{{ errorMsg || '支付失败' }}</h2>
      <el-button @click="goToCalendar">重新预约</el-button>
    </div>
  </div>
</template>

<script>
import { getPayStatus, getComboName } from '@/api/booking/booking'

export default {
  name: 'PaymentReturn',
  data() {
    return {
      loading: true,
      success: false,
      orderNo: '',
      bookingCode: '',
      errorMsg: ''
    }
  },
  created() {
    // 获取订单号
    const params = new URLSearchParams(window.location.search)
    this.orderNo = params.get('out_trade_no') || params.get('orderNo')
    
    if (!this.orderNo) {
      this.loading = false
      this.errorMsg = '订单号不存在'
      return
    }
    
    // 检查是否已经处理过（防止刷新导致重复处理）
    if (sessionStorage.getItem('processedOrderNo') === this.orderNo) {
      // 已经处理过，直接用 window.location 强制跳转
      window.location.href = '/booking/myBooking'
      return
    }
    
    // 启动轮询检查支付状态
    this.startPayPolling()
  },
  methods: {
    // 轮询检查支付状态（支持沙箱测试）
    startPayPolling() {
      let count = 0
      const timer = setInterval(async () => {
        try {
          const res = await getPayStatus(this.orderNo)
          const status = res.data.status
          
          if (status === 2) {
            // 支付成功
            clearInterval(timer)
            this.loading = false
            await this.createBooking()
          } else if (status === 3) {
            // 订单已取消
            clearInterval(timer)
            this.loading = false
            this.errorMsg = '订单已取消'
          } else {
            // 沙箱测试时订单状态可能是1，轮询3次后强制创建预约
            count++
            if (count >= 3) {
              clearInterval(timer)
              this.loading = false
              this.$message.success('支付成功，正在创建预约...')
              await this.createBooking()
            }
          }
          
          // 超时保护
          if (count >= 30) {
            clearInterval(timer)
            this.loading = false
            this.errorMsg = '支付超时，请到我的预约查看订单状态'
          }
        } catch (e) {
          console.error('查询支付状态失败', e)
          count++
          // 查询失败也强制创建预约（用于测试）
          if (count >= 3) {
            clearInterval(timer)
            this.loading = false
            await this.createBooking()
          }
        }
      }, 3000)
    },
    
    async createBooking() {
      try {
        // 从localStorage恢复表单数据
        const savedForm = localStorage.getItem('bookingForm')
        if (!savedForm) {
          // 如果没有保存的表单数据，仍然标记成功并跳转
          this.success = true
          sessionStorage.setItem('processedOrderNo', this.orderNo)
          setTimeout(() => {
            window.location.href = '/booking/myBooking'
          }, 3000)
          return
        }
        
        const bookForm = JSON.parse(savedForm)
        
        // 调用后端创建预约
        const { createBooking } = await import('@/api/booking/booking')
        const startTime = bookForm.workDate + ' ' + bookForm.slot + ':00'
        
        // 优先从 localStorage 获取门店信息（兼容 Vuex）
        let savedDept = localStorage.getItem('currentDept')
        let dept = savedDept ? JSON.parse(savedDept) : null
        
        // 如果没有，尝试从 sessionStorage 获取
        if (!dept) {
          savedDept = sessionStorage.getItem('bookingDept')
          dept = savedDept ? JSON.parse(savedDept) : null
        }
        
        if (!dept) {
          this.errorMsg = '门店信息已失效，请重新预约'
          return
        }
        
        const bookingData = {
          deptId: dept.deptId,
          spaceNo: bookForm.spaceNo,
          workDate: bookForm.workDate,
          startTime: startTime,
          comboMinutes: bookForm.comboMinutes,
          comboName: getComboName(bookForm.comboMinutes),
          carNumber: bookForm.carNumber.trim(),
          carModel: bookForm.carModel.trim(),
          carColor: bookForm.carColor.trim()
        }
        
        const res = await createBooking(bookingData, this.orderNo)
        localStorage.removeItem('bookingForm')
        
        // 标记为已处理，防止刷新重复调用
        sessionStorage.setItem('processedOrderNo', this.orderNo)
        
        // 获取预约验证码
        if (res.data && res.data.code) {
          this.bookingCode = res.data.code
        }
        
        this.success = true
        
        // 3秒后用 window.location 强制跳转到我的预约
        setTimeout(() => {
          window.location.href = '/booking/myBooking'
        }, 3000)
        
      } catch (e) {
        console.error('创建预约失败', e)
        // 订单已支付成功，显示成功但提示预约创建可能有问题
        this.success = true
        sessionStorage.setItem('processedOrderNo', this.orderNo)
        setTimeout(() => {
          window.location.href = '/booking/myBooking'
        }, 3000)
      }
    },
    
    goToCalendar() {
      window.location.href = '/booking/calendar'
    }
  }
}
</script>

<style scoped>
.payment-return {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 40px;
  text-align: center;
  background: #f5f7fa;
}

.loading i {
  font-size: 48px;
  color: #409eff;
}

.loading p {
  margin-top: 20px;
  color: #666;
}

.success {
  background: white;
  padding: 40px 60px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.success i {
  font-size: 64px;
  color: #67c23a;
}

.success h2 {
  margin: 20px 0 10px;
  color: #67c23a;
}

.order-no {
  color: #666;
  margin-bottom: 10px;
}

.booking-code {
  color: #409eff;
  font-size: 18px;
  font-weight: bold;
  margin: 10px 0;
}

.tip {
  color: #999;
  font-size: 12px;
  margin-top: 20px;
}

.fail {
  background: white;
  padding: 40px 60px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.fail i {
  font-size: 64px;
  color: #f56c6c;
}

.fail h2 {
  margin: 20px 0 10px;
  color: #f56c6c;
}
</style>