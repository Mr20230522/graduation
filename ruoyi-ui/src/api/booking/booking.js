import request from '@/utils/request'

// 获取日历数据（7天排班 + 车位占用矩阵）
export function getCalendar(deptId, startDate) {
  return request({
    url: '/booking/calendar',
    method: 'get',
    params: {
      deptId,
      startDate: startDate || new Date()
    }
  })
}

// 创建预约
export function createBooking(data) {
  return request({
    url: '/booking/create',
    method: 'post',
    data: data
  })
}

// 核销预约
export function verifyBooking(code) {
  return request({
    url: '/booking/verify',
    method: 'post',
    params: { code }
  })
}

// 取消预约
export function cancelBooking(bookingId) {
  return request({
    url: '/booking/cancel/' + bookingId,
    method: 'post'
  })
}

// 查询我的预约（进行中）
export function listMyBooking(params) {
  return request({
    url: '/booking/my/list',
    method: 'get',
    params: params
  })
}

// 查询历史记录
export function listMyHistory(params) {
  return request({
    url: '/booking/my/history',
    method: 'get',
    params: params
  })
}
