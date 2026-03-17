import request from '@/utils/request'

// 地址转坐标
export function searchAddress(address) {
  return request({
    url: '/tool/amap/testAddress',
    method: 'get',
    params: { address }
  })
}

// IP定位
export function getCurrentIp() {
  return request({
    url: '/tool/amap/testIp',
    method: 'get'
  })
}

// 指定IP定位
export function getIpLocation(ip) {
  return request({
    url: '/tool/amap/testIp',
    method: 'get',
    params: { ip }
  })
}
