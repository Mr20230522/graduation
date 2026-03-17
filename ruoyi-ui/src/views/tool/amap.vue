<template>
  <div class="app-container">
    <!-- 搜索框区域 -->
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>高德地图 - 路径规划 (修复版)</span>
      </div>

      <div style="margin-bottom: 20px; display: flex; gap: 10px; flex-wrap: wrap;">
        <!-- 起点输入 -->
        <el-input
          v-model="origin"
          placeholder="起点（例如：西南林业大学）"
          style="width: 300px"
          clearable
        />

        <!-- 终点输入 -->
        <el-input
          v-model="destination"
          placeholder="终点（例如：昆明火车站）"
          style="width: 300px"
          clearable
        />

        <el-button type="primary" @click="searchAndRoute" :loading="searching">搜索路径</el-button>
        <el-button @click="swapOriginDestination">交换起点终点</el-button>
        <el-button @click="clearRoute">清除路径</el-button>
      </div>

      <!-- 路径类型选择 -->
      <el-radio-group v-model="routeType" style="margin-bottom: 20px">
        <el-radio-button label="driving">驾车</el-radio-button>
        <el-radio-button label="walking">步行</el-radio-button>
        <el-radio-button label="riding">骑行</el-radio-button>
        <el-radio-button label="transit">公交</el-radio-button>
      </el-radio-group>

      <!-- 路径信息显示 -->
      <el-row :gutter="20" style="margin-bottom: 20px" v-if="routeInfo">
        <el-col :span="8">
          <el-card shadow="hover">
            <div slot="header">起点</div>
            <div>{{ originName }}</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover">
            <div slot="header">终点</div>
            <div>{{ destinationName }}</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover">
            <div slot="header">路径信息</div>
            <div>距离：{{ routeInfo.distance }}</div>
            <div>时间：{{ routeInfo.time }}</div>
            <div>费用：{{ routeInfo.tolls }}</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 地图容器 -->
      <div id="container" style="width: 100%; height: 600px; border: 1px solid #ddd; border-radius: 4px;"></div>

      <!-- 错误信息提示 -->
      <el-alert
        v-if="errorMsg"
        :title="errorMsg"
        type="error"
        show-icon
        style="margin-top: 20px"
        closable
        @close="errorMsg = ''">
      </el-alert>

      <!-- 详细步骤 -->
      <el-card v-if="steps.length > 0" style="margin-top: 20px">
        <div slot="header">
          <span>详细步骤 ({{ steps.length }})</span>
        </div>
        <el-timeline>
          <el-timeline-item
            v-for="(step, index) in steps"
            :key="index"
            :color="step.color"
          >
            <p v-html="step.instruction"></p>
            <small v-if="step.distance">距离: {{ step.distance }}，时间: {{ step.time }}</small>
          </el-timeline-item>
        </el-timeline>
      </el-card>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "Amap",
  data() {
    return {
      origin: "西南林业大学",
      destination: "昆明火车站",
      originName: "",
      destinationName: "",
      map: null,
      routeType: "driving",
      routeInfo: null,
      steps: [],
      searching: false,
      errorMsg: "",
      // 存储起点和终点的坐标
      originLocation: null,
      destLocation: null,
      // 路径规划对象
      driving: null,
      walking: null,
      riding: null,
      transfer: null
    };
  },
  mounted() {
    this.initMap();
  },
  methods: {
    // 初始化地图
    initMap() {
      // 先设置安全密钥（重要！）
      window._AMapSecurityConfig = {
        securityJsCode: 'ea3b9d3607682004e7808ab457dd572b' // 你的安全密钥
      };

      // 动态加载高德地图脚本
      const script = document.createElement('script');
      script.src = 'https://webapi.amap.com/maps?v=2.0&key=a4de16979867b9c8882e9301a5ebeebe';

      script.onload = () => {
        // 创建地图
        this.map = new AMap.Map('container', {
          zoom: 12,
          center: [102.7146, 25.0602], // 昆明中心
          viewMode: '2D',
          resizeEnable: true
        });

        // 使用 AMap.service 加载插件（重要！不用 plugin）
        AMap.service(['AMap.PlaceSearch', 'AMap.Driving', 'AMap.Walking', 'AMap.Riding', 'AMap.Transfer'], () => {
          // 初始化各种路径规划
          this.driving = new AMap.Driving({
            map: this.map,
            panel: false,
            hideMarkers: true,
            policy: AMap.DrivingPolicy.LEAST_TIME // 最快捷
          });

          this.walking = new AMap.Walking({
            map: this.map,
            panel: false,
            hideMarkers: true
          });

          this.riding = new AMap.Riding({
            map: this.map,
            panel: false,
            hideMarkers: true
          });

          this.transfer = new AMap.Transfer({
            map: this.map,
            panel: false,
            hideMarkers: true,
            city: '昆明'
          });

          this.$message.success('地图加载成功，可以开始搜索路径');

          // 测试一下直接搜索西南林业大学
          setTimeout(() => {
            this.testSearch();
          }, 1000);
        });
      };

      script.onerror = () => {
        this.$message.error('地图加载失败，请检查Key或网络');
      };

      document.head.appendChild(script);
    },

    // 测试搜索功能
    testSearch() {
      const placeSearch = new AMap.PlaceSearch({
        city: '昆明',
        pageSize: 1
      });

      placeSearch.search('西南林业大学', (status, result) => {
        if (status === 'complete' && result.poiList && result.poiList.pois.length > 0) {
          const poi = result.poiList.pois[0];
          this.$message.success(`找到：${poi.name}`);
          console.log('测试搜索结果：', poi);
        } else {
          this.$message.warning('测试搜索未找到结果，但地图加载正常');
        }
      });
    },

    // 搜索并规划路径
    searchAndRoute() {
      if (!this.origin || !this.destination) {
        this.$message.warning('请输入起点和终点');
        return;
      }

      this.searching = true;
      this.errorMsg = "";

      // 先搜索起点
      this.searchPlace(this.origin).then(originResult => {
        if (!originResult) {
          throw new Error(`未找到起点：${this.origin}`);
        }
        this.originLocation = originResult.location;
        this.originName = originResult.name;

        // 再搜索终点
        return this.searchPlace(this.destination).then(destResult => {
          if (!destResult) {
            throw new Error(`未找到终点：${this.destination}`);
          }
          this.destLocation = destResult.location;
          this.destinationName = destResult.name;

          return { originResult, destResult };
        });
      }).then(({ originResult, destResult }) => {
        // 清除之前的路径和标记
        this.clearRoute();

        // 添加起点标记
        new AMap.Marker({
          map: this.map,
          position: originResult.location,
          title: '起点：' + originResult.name,
          icon: 'https://webapi.amap.com/theme/v1.3/markers/n/start.png'
        });

        // 添加终点标记
        new AMap.Marker({
          map: this.map,
          position: destResult.location,
          title: '终点：' + destResult.name,
          icon: 'https://webapi.amap.com/theme/v1.3/markers/n/end.png'
        });

        // 规划路径
        this.planRoute();

      }).catch(error => {
        this.errorMsg = error.message;
        this.$message.error(error.message);
      }).finally(() => {
        this.searching = false;
      });
    },

    // 搜索地点
    searchPlace(keyword) {
      return new Promise((resolve) => {
        const placeSearch = new AMap.PlaceSearch({
          city: '昆明',
          pageSize: 5
        });

        placeSearch.search(keyword, (status, result) => {
          if (status === 'complete' && result.poiList && result.poiList.pois.length > 0) {
            // 返回最匹配的结果
            const poi = result.poiList.pois[0];
            const [lng, lat] = poi.location.split(',').map(Number);
            resolve({
              name: poi.name,
              address: poi.address,
              location: [lng, lat]
            });
          } else {
            resolve(null);
          }
        });
      });
    },

    // 规划路径
    planRoute() {
      if (!this.originLocation || !this.destLocation) return;

      let router;
      switch(this.routeType) {
        case 'driving':
          router = this.driving;
          break;
        case 'walking':
          router = this.walking;
          break;
        case 'riding':
          router = this.riding;
          break;
        case 'transit':
          router = this.transfer;
          break;
      }

      if (!router) {
        this.$message.error('路径规划插件未初始化');
        return;
      }

      // 搜索路径
      router.search(this.originLocation, this.destLocation, (status, result) => {
        if (status === 'complete') {
          this.handleRouteResult(result);
        } else {
          this.errorMsg = '路径规划失败，请尝试其他交通方式';
          this.$message.error(this.errorMsg);
        }
      });
    },

    // 处理路径结果
    handleRouteResult(result) {
      this.routeInfo = {};
      this.steps = [];

      if (this.routeType === 'driving' && result.routes) {
        const route = result.routes[0];
        this.routeInfo = {
          distance: (route.distance / 1000).toFixed(1) + '公里',
          time: Math.ceil(route.time / 60) + '分钟',
          tolls: route.tolls ? '¥' + route.tolls : '免费'
        };

        this.steps = route.steps.map(step => ({
          instruction: step.instruction,
          distance: (step.distance / 1000).toFixed(2) + '公里',
          time: Math.ceil(step.time / 60) + '分钟',
          color: '#409EFF'
        }));

      } else if (this.routeType === 'transit' && result.plans) {
        const plan = result.plans[0];
        this.routeInfo = {
          distance: (plan.distance / 1000).toFixed(1) + '公里',
          time: Math.ceil(plan.time / 60) + '分钟',
          tolls: '¥' + plan.cost
        };

        this.steps = plan.segments.map(segment => {
          if (segment.transit_mode) {
            return {
              instruction: `乘坐${segment.transit_mode}`,
              distance: (segment.distance / 1000).toFixed(2) + '公里',
              time: Math.ceil(segment.time / 60) + '分钟',
              color: '#67C23A'
            };
          } else if (segment.walking) {
            return {
              instruction: segment.walking.instruction,
              distance: (segment.walking.distance / 1000).toFixed(2) + '公里',
              time: Math.ceil(segment.walking.time / 60) + '分钟',
              color: '#E6A23C'
            };
          }
        });

      } else if (result.routes) {
        const route = result.routes[0];
        this.routeInfo = {
          distance: (route.distance / 1000).toFixed(1) + '公里',
          time: Math.ceil(route.time / 60) + '分钟',
          tolls: '免费'
        };

        this.steps = route.steps.map(step => ({
          instruction: step.instruction,
          distance: (step.distance / 1000).toFixed(2) + '公里',
          time: Math.ceil(step.time / 60) + '分钟',
          color: '#409EFF'
        }));
      }

      // 调整视野
      if (this.originLocation && this.destLocation) {
        const bounds = new AMap.Bounds(
          new AMap.LngLat(this.originLocation[0], this.originLocation[1]),
          new AMap.LngLat(this.destLocation[0], this.destLocation[1])
        );
        this.map.setBounds(bounds);
      }

      this.$message.success('路径规划成功');
    },

    // 交换起点终点
    swapOriginDestination() {
      [this.origin, this.destination] = [this.destination, this.origin];
      [this.originName, this.destinationName] = [this.destinationName, this.originName];
      [this.originLocation, this.destLocation] = [this.destLocation, this.originLocation];

      if (this.originLocation && this.destLocation) {
        this.clearRoute();
        this.planRoute();
      }
    },

    // 清除路径
    clearRoute() {
      if (this.map) {
        this.map.clearMap();
      }
      this.routeInfo = null;
      this.steps = [];
    }
  },
  watch: {
    // 当路线类型改变时重新规划
    routeType() {
      if (this.originLocation && this.destLocation) {
        this.clearRoute();
        this.planRoute();
      }
    }
  },
  beforeDestroy() {
    if (this.map) {
      this.map.destroy();
    }
  }
};
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.el-card {
  margin-bottom: 20px;
}

#container {
  width: 100%;
  height: 600px;
}

.el-radio-group {
  margin-bottom: 20px;
}
</style>
