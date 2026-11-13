export const chartOptions = {
  areaStack: {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      }
    },
    legend: {
      top: '0',
      left: 'center',
      data: ['Email', 'Union Ads', 'Video Ads', 'Direct', 'Search Engine']
    },
    toolbox: {
      feature: {
        saveAsImage: {}
      }
    },
    grid: {
      top: '40',
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: [
      {
        type: 'category',
        boundaryGap: false,
        data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
      }
    ],
    yAxis: [
      {
        type: 'value'
      }
    ],
    series: [
      {
        name: 'Email',
        type: 'line',
        stack: 'Total',
        areaStyle: {},
        emphasis: {
          focus: 'series'
        },
        data: [120, 132, 101, 134, 90, 230, 210]
      },
      {
        name: 'Union Ads',
        type: 'line',
        stack: 'Total',
        areaStyle: {},
        emphasis: {
          focus: 'series'
        },
        data: [220, 182, 191, 234, 290, 330, 310]
      },
      {
        name: 'Video Ads',
        type: 'line',
        stack: 'Total',
        areaStyle: {},
        emphasis: {
          focus: 'series'
        },
        data: [150, 232, 201, 154, 190, 330, 410]
      },
      {
        name: 'Direct',
        type: 'line',
        stack: 'Total',
        areaStyle: {},
        emphasis: {
          focus: 'series'
        },
        data: [320, 332, 301, 334, 390, 330, 320]
      },
      {
        name: 'Search Engine',
        type: 'line',
        stack: 'Total',
        label: {
          show: true,
          position: 'top'
        },
        areaStyle: {},
        emphasis: {
          focus: 'series'
        },
        data: [820, 932, 901, 934, 1290, 1330, 1320]
      }
    ]
  },
  barStack: {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      top: '0',
      left: 'center',
      data: ['Direct', 'Email', 'Union Ads', 'Video Ads', 'Search Engine', 'Baidu', 'Google', 'Bing', 'Others']
    },
    grid: {
      top: '60',
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: [
      {
        type: 'category',
        data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
      }
    ],
    yAxis: [
      {
        type: 'value'
      }
    ],
    series: [
      {
        name: 'Direct',
        type: 'bar',
        emphasis: {
          focus: 'series'
        },
        data: [320, 332, 301, 334, 390, 330, 320]
      },
      {
        name: 'Email',
        type: 'bar',
        stack: 'Ad',
        emphasis: {
          focus: 'series'
        },
        data: [120, 132, 101, 134, 90, 230, 210]
      },
      {
        name: 'Union Ads',
        type: 'bar',
        stack: 'Ad',
        emphasis: {
          focus: 'series'
        },
        data: [220, 182, 191, 234, 290, 330, 310]
      },
      {
        name: 'Video Ads',
        type: 'bar',
        stack: 'Ad',
        emphasis: {
          focus: 'series'
        },
        data: [150, 232, 201, 154, 190, 330, 410]
      },
      {
        name: 'Search Engine',
        type: 'bar',
        data: [862, 1018, 964, 1026, 1679, 1600, 1570],
        emphasis: {
          focus: 'series'
        },
        markLine: {
          lineStyle: {
            type: 'dashed'
          },
          data: [[{ type: 'min' }, { type: 'max' }]]
        }
      },
      {
        name: 'Baidu',
        type: 'bar',
        barWidth: 5,
        stack: 'Search Engine',
        emphasis: {
          focus: 'series'
        },
        data: [620, 732, 701, 734, 1090, 1130, 1120]
      },
      {
        name: 'Google',
        type: 'bar',
        stack: 'Search Engine',
        emphasis: {
          focus: 'series'
        },
        data: [120, 132, 101, 134, 290, 230, 220]
      },
      {
        name: 'Bing',
        type: 'bar',
        stack: 'Search Engine',
        emphasis: {
          focus: 'series'
        },
        data: [60, 72, 71, 74, 190, 130, 110]
      },
      {
        name: 'Others',
        type: 'bar',
        stack: 'Search Engine',
        emphasis: {
          focus: 'series'
        },
        data: [62, 82, 91, 84, 109, 110, 120]
      }
    ]
  },
  candlestick: {
    legend: {
      top: '0',
      left: 'center',
      data: ['K-Line']
    },
    grid: {
      top: '40',
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      data: ['2017-10-24', '2017-10-25', '2017-10-26', '2017-10-27', '2017-10-28', '2017-10-29', '2017-10-30', '2017-10-31']
    },
    yAxis: {},
    series: [
      {
        name: 'K-Line',
        type: 'candlestick',
        data: [
          [20, 34, 10, 38],
          [34, 40, 30, 50],
          [31, 38, 33, 44],
          [38, 15, 5, 42],
          [20, 34, 10, 38],
          [34, 40, 30, 50],
          [31, 38, 33, 44],
          [38, 15, 5, 42]
        ]
      }
    ]
  },
  funnel: {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b} : {c}%'
    },
    legend: {
      top: '0',
      left: 'center',
      data: ['Show', 'Click', 'Visit', 'Inquiry', 'Order']
    },
    series: [
      {
        name: 'Funnel',
        type: 'funnel',
        left: '10%',
        top: 60,
        bottom: 60,
        width: '80%',
        min: 0,
        max: 100,
        minSize: '0%',
        maxSize: '100%',
        sort: 'descending',
        gap: 2,
        label: {
          show: true,
          position: 'inside'
        },
        labelLine: {
          length: 10,
          lineStyle: {
            width: 1,
            type: 'solid'
          }
        },
        itemStyle: {
          borderColor: '#fff',
          borderWidth: 1
        },
        emphasis: {
          label: {
            fontSize: 20
          }
        },
        data: [
          { value: 60, name: 'Visit' },
          { value: 40, name: 'Inquiry' },
          { value: 20, name: 'Order' },
          { value: 80, name: 'Click' },
          { value: 100, name: 'Show' }
        ]
      }
    ]
  },
  graph: {
    tooltip: {},
    animationDurationUpdate: 1500,
    animationEasingUpdate: 'quinticInOut',
    series: [
      {
        type: 'graph',
        layout: 'none',
        symbolSize: 50,
        roam: true,
        label: {
          show: true
        },
        edgeSymbol: ['circle', 'arrow'],
        edgeSymbolSize: [4, 10],
        edgeLabel: {
          fontSize: 20
        },
        data: [
          {
            name: 'Node 1',
            x: 300,
            y: 300
          },
          {
            name: 'Node 2',
            x: 800,
            y: 300
          },
          {
            name: 'Node 3',
            x: 550,
            y: 100
          },
          {
            name: 'Node 4',
            x: 550,
            y: 500
          }
        ],
        links: [
          {
            source: 0,
            target: 1,
            symbolSize: [5, 20],
            label: {
              show: true
            },
            lineStyle: {
              width: 5,
              curveness: 0.2
            }
          },
          {
            source: 'Node 2',
            target: 'Node 1',
            label: {
              show: true
            },
            lineStyle: {
              curveness: 0.2
            }
          },
          {
            source: 'Node 1',
            target: 'Node 3'
          },
          {
            source: 'Node 2',
            target: 'Node 3'
          },
          {
            source: 'Node 2',
            target: 'Node 4'
          },
          {
            source: 'Node 1',
            target: 'Node 4'
          }
        ],
        lineStyle: {
          opacity: 0.9,
          width: 2,
          curveness: 0
        }
      }
    ]
  },
  mixLineBar: {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        crossStyle: {
          color: '#999'
        }
      }
    },
    toolbox: {
      feature: {
        dataView: { show: true, readOnly: false },
        magicType: { show: true, type: ['line', 'bar'] },
        restore: { show: true },
        saveAsImage: { show: true }
      }
    },
    legend: {
      top: '0',
      left: 'center',
      data: ['Evaporation', 'Precipitation', 'Temperature']
    },
    grid: {
      top: '60',
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: [
      {
        type: 'category',
        data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
        axisPointer: {
          type: 'shadow'
        }
      }
    ],
    yAxis: [
      {
        type: 'value',
        name: 'Precipitation',
        min: 0,
        max: 250,
        interval: 50,
        axisLabel: {
          formatter: '{value} ml'
        }
      },
      {
        type: 'value',
        name: 'Temperature',
        min: 0,
        max: 25,
        interval: 5,
        axisLabel: {
          formatter: '{value} °C'
        }
      }
    ],
    series: [
      {
        name: 'Evaporation',
        type: 'bar',
        tooltip: {
          valueFormatter: function (value) {
            return value + ' ml';
          }
        },
        data: [
          2.0, 4.9, 7.0, 23.2, 25.6, 76.7, 135.6
        ]
      },
      {
        name: 'Precipitation',
        type: 'bar',
        tooltip: {
          valueFormatter: function (value) {
            return value + ' ml';
          }
        },
        data: [
          2.6, 5.9, 9.0, 26.4, 28.7, 70.7, 175.6
        ]
      },
      {
        name: 'Temperature',
        type: 'line',
        yAxisIndex: 1,
        tooltip: {
          valueFormatter: function (value) {
            return value + ' °C';
          }
        },
        data: [2.0, 2.2, 3.3, 4.5, 6.3, 10.2, 20.3]
      }
    ]
  },
  multipleLines: {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      top: '0',
      left: 'center',
      data: ['Series 1', 'Series 2']
    },
    grid: {
      top: '40',
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: 'Series 1',
        type: 'line',
        data: [150, 230, 224, 218, 135, 147, 260]
      },
      {
        name: 'Series 2',
        type: 'line',
        data: [250, 330, 324, 418, 535, 447, 360],
        itemStyle: {
          color: '#91cc75'
        }
      }
    ]
  },
  parallelAqi: {
      legend: {
      bottom: '0',
      left: 'center',
      data: ['Beijing', 'Shanghai', 'Guangzhou'],
      textStyle: {
        color: '#eee'
      }
    },
    backgroundColor: '#333',
    parallelAxis: [
      { dim: 0, name: 'Date' },
      { dim: 1, name: 'AQI' },
      { dim: 2, name: 'PM2.5' },
      { dim: 3, name: 'PM10' },
      { dim: 4, name: 'CO' },
      { dim: 5, name: 'NO2' },
      { dim: 6, name: 'SO2' },
      {
        dim: 7,
        name: 'Level',
        type: 'category',
        data: ['Excellent', 'Good', 'Lightly Polluted', 'Moderately Polluted', 'Heavily Polluted', 'Severely Polluted']
      }
    ],
    parallel: {
      left: '5%',
      right: '18%',
      bottom: 100,
      parallelAxisDefault: {
        type: 'value',
        name: 'AQI',
        nameLocation: 'end',
        nameGap: 20,
        nameTextStyle: {
          color: '#fff',
          fontSize: 12
        },
        axisLine: {
          lineStyle: {
            color: '#aaa'
          }
        },
        axisTick: {
          lineStyle: {
            color: '#777'
          }
        },
        splitLine: {
          show: false
        },
        axisLabel: {
          color: '#fff'
        }
      }
    },
    series: [
      {
        name: 'Beijing',
        type: 'parallel',
        lineStyle: {
          width: 1,
          opacity: 0.5
        },
        data: [
          [1, 55, 9, 56, 0.46, 18, 6, 'Good'],
          [2, 25, 11, 21, 0.65, 34, 9, 'Excellent'],
          [3, 56, 7, 63, 0.3, 14, 5, 'Good'],
          [4, 33, 7, 29, 0.33, 16, 6, 'Excellent'],
          [5, 42, 24, 44, 0.76, 40, 16, 'Excellent']
        ]
      },
      {
        name: 'Shanghai',
        type: 'parallel',
        lineStyle: {
          width: 1,
          opacity: 0.5
        },
        data: [
          [1, 26, 37, 27, 1.1, 21, 13, 'Excellent'],
          [2, 85, 62, 71, 1.3, 62, 25, 'Good'],
          [3, 78, 38, 74, 1.3, 63, 26, 'Good'],
          [4, 21, 21, 36, 0.7, 40, 15, 'Excellent'],
          [5, 41, 42, 46, 1, 45, 19, 'Excellent']
        ]
      },
      {
        name: 'Guangzhou',
        type: 'parallel',
        lineStyle: {
          width: 1,
          opacity: 0.5
        },
        data: [
          [1, 91, 45, 125, 0.82, 34, 23, 'Good'],
          [2, 65, 27, 78, 0.86, 45, 29, 'Good'],
          [3, 83, 60, 84, 1.09, 73, 27, 'Good'],
          [4, 109, 81, 121, 1.28, 68, 51, 'Lightly Polluted'],
          [5, 106, 77, 114, 1.07, 55, 51, 'Lightly Polluted']
        ]
      }
    ]
  },
  pieNest: {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      top: '0',
      left: 'center',
      data: [
        'Direct',
        'Marketing',
        'Search Engine',
        'Email',
        'Union Ads',
        'Video Ads',
        'Baidu',
        'Google',
        'Bing',
        'Others'
      ]
    },
    series: [
      {
        name: 'Access From',
        type: 'pie',
        selectedMode: 'single',
        radius: [0, '30%'],
        label: {
          position: 'inner',
          fontSize: 14
        },
        labelLine: {
          show: false
        },
        data: [
          { value: 1548, name: 'Search Engine' },
          { value: 775, name: 'Marketing' },
          { value: 679, name: 'Direct', selected: true }
        ]
      },
      {
        name: 'Access From',
        type: 'pie',
        radius: ['45%', '60%'],
        labelLine: {
          length: 30
        },
        label: {
          formatter: '{a|{a}}{abg|}\n{hr|}\n  {b|{b}：}{c}  {per|{d}%}  ',
          backgroundColor: '#F6F8FC',
          borderColor: '#8C8C8C',
          borderWidth: 1,
          borderRadius: 4,
          rich: {
            a: {
              color: '#6E7079',
              lineHeight: 22,
              align: 'center'
            },
            hr: {
              borderColor: '#8C8C8C',
              width: '100%',
              borderWidth: 1,
              height: 0
            },
            b: {
              color: '#4C5058',
              fontSize: 14,
              fontWeight: 'bold',
              lineHeight: 33
            },
            per: {
              color: '#fff',
              backgroundColor: '#4C5058',
              padding: [3, 4],
              borderRadius: 4
            }
          }
        },
        data: [
          { value: 1048, name: 'Baidu' },
          { value: 335, name: 'Direct' },
          { value: 310, name: 'Email' },
          { value: 251, name: 'Google' },
          { value: 234, name: 'Union Ads' },
          { value: 147, name: 'Bing' },
          { value: 135, name: 'Video Ads' },
          { value: 102, name: 'Others' }
        ]
      }
    ]
  },
  radar: {
    legend: {
      top: '0',
      left: 'center',
      data: ['Allocated Budget', 'Actual Spending']
    },
    radar: {
      indicator: [
        { name: 'Sales', max: 6500 },
        { name: 'Administration', max: 16000 },
        { name: 'Information Technology', max: 30000 },
        { name: 'Customer Support', max: 38000 },
        { name: 'Development', max: 52000 },
        { name: 'Marketing', max: 25000 }
      ]
    },
    series: [
      {
        name: 'Budget vs spending',
        type: 'radar',
        data: [
          {
            value: [4200, 3000, 20000, 35000, 50000, 18000],
            name: 'Allocated Budget'
          },
          {
            value: [5000, 14000, 28000, 26000, 42000, 21000],
            name: 'Actual Spending'
          }
        ]
      }
    ]
  },
  rose: {
    legend: {
      bottom: '0',
      left: 'center'
    },
    toolbox: {
      show: true,
      feature: {
        mark: { show: true },
        dataView: { show: true, readOnly: false },
        restore: { show: true },
        saveAsImage: { show: true }
      }
    },
    series: [
      {
        name: 'Nightingale Chart',
        type: 'pie',
        radius: ['10%', '70%'],
        center: ['50%', '50%'],
        roseType: 'area',
        itemStyle: {
          borderRadius: 8
        },
        data: [
          { value: 40, name: 'rose 1' },
          { value: 38, name: 'rose 2' },
          { value: 32, name: 'rose 3' },
          { value: 30, name: 'rose 4' },
          { value: 28, name: 'rose 5' },
          { value: 26, name: 'rose 6' },
          { value: 22, name: 'rose 7' },
          { value: 18, name: 'rose 8' }
        ]
      }
    ]
  },
  sankey: {
    series: [
      {
        type: 'sankey',
        layout: 'none',
        emphasis: {
          focus: 'adjacency'
        },
        data: [
          { name: 'a' },
          { name: 'b' },
          { name: 'a1' },
          { name: 'a2' },
          { name: 'b1' },
          { name: 'c' }
        ],
        links: [
          { source: 'a', target: 'a1', value: 5 },
          { source: 'a', target: 'a2', value: 3 },
          { source: 'b', target: 'b1', value: 8 },
          { source: 'a', target: 'b1', value: 3 },
          { source: 'b1', target: 'a1', value: 1 },
          { source: 'b1', target: 'c', value: 2 }
        ]
      }
    ]
  },
  scatterEffect: {
    xAxis: {
      scale: true
    },
    yAxis: {
      scale: true
    },
    series: [
      {
        type: 'effectScatter',
        symbolSize: 20,
        data: [
          [172.7, 105.2],
          [153.4, 42]
        ]
      },
      {
        type: 'scatter',
        data: [
          [161.2, 51.6], [167.5, 59.0], [159.5, 49.2], [157.0, 63.0], [155.8, 53.6],
          [170.0, 59.0], [159.1, 47.6], [166.0, 69.8], [176.2, 66.8], [160.2, 75.2],
          [172.5, 55.2], [170.9, 54.2], [172.9, 62.5], [153.4, 42.0], [160.0, 50.0],
          [147.2, 49.8], [168.2, 49.2], [175.0, 73.2], [157.0, 47.8], [167.6, 68.8],
          [159.5, 50.6], [175.0, 82.2], [166.8, 57.2], [176.5, 87.8], [170.2, 72.8],
          [174.0, 54.5], [173.0, 59.8], [179.9, 67.3], [170.5, 67.8], [160.0, 47.0],
          [154.4, 46.2], [162.0, 55.0], [176.5, 83.0], [160.0, 54.4], [152.0, 45.8],
          [162.1, 53.6], [170.0, 73.2], [160.2, 52.1], [161.3, 67.9], [166.4, 56.6],
          [168.9, 62.3], [163.8, 58.5], [167.6, 54.5], [160.0, 50.2], [161.3, 60.3],
          [167.6, 58.3], [165.1, 56.2], [160.0, 50.2], [170.0, 72.9], [157.5, 59.8],
          [167.6, 61.0], [160.7, 69.1], [163.2, 55.9], [152.4, 46.5], [157.5, 54.3],
          [168.3, 54.8], [180.3, 60.7], [165.5, 60.0], [165.0, 62.0], [164.5, 60.3],
          [156.0, 52.7], [160.0, 74.3], [163.0, 62.0], [165.7, 73.1], [161.0, 80.0],
          [162.0, 54.7], [166.0, 53.2], [174.0, 75.7], [172.7, 61.1], [167.6, 55.7],
          [151.1, 48.7], [164.5, 52.3], [163.5, 50.0], [152.0, 59.3], [164.5, 61.2],
          [155.0, 47.0], [164.5, 65.9], [163.0, 66.3], [168.9, 63.3], [163.2, 58.2],
          [167.6, 91.0], [160.0, 52.9], [161.3, 63.1], [175.7, 71.8], [161.3, 61.3],
          [170.2, 55.1], [172.4, 57.2], [170.2, 55.8], [170.2, 63.6], [172.3, 71.8],
          [165.9, 61.9], [170.2, 61.4], [170.2, 57.9], [172.7, 72.7], [172.7, 51.6],
          [172.7, 59.1], [170.2, 59.5], [170.2, 54.1], [170.2, 61.4], [170.2, 64.6],
          [170.2, 62.9], [170.2, 68.8], [167.6, 64.5], [167.6, 61.4], [167.6, 65.4],
          [162.6, 61.4], [167.6, 55.2], [166.3, 59.8], [168.3, 62.3], [166.3, 59.5],
          [166.3, 51.2], [170.2, 64.5], [170.2, 59.1], [170.2, 53.5], [170.2, 56.6],
          [172.7, 64.5], [170.2, 59.1], [170.2, 53.6], [170.2, 56.5], [172.7, 64.5],
          [170.2, 59.1], [170.2, 53.5], [170.2, 56.6], [177.8, 60.0], [175.3, 66.8],
          [175.3, 65.5], [175.3, 71.2], [177.8, 71.8], [180.3, 75.0], [177.8, 66.2],
          [177.8, 65.9], [177.8, 61.3], [177.8, 61.3], [175.3, 63.1], [182.9, 71.8],
          [182.9, 81.3], [182.9, 77.5], [175.3, 65.7], [175.3, 61.8], [175.3, 59.1],
          [172.7, 56.1], [172.7, 59.1], [172.7, 54.1], [170.2, 59.1], [170.2, 53.5],
          [170.2, 56.6], [172.7, 64.5], [170.2, 59.1], [170.2, 53.6], [170.2, 56.5],
          [172.7, 64.5], [170.2, 59.1], [170.2, 53.5], [170.2, 56.6], [177.8, 60.0],
          [175.3, 66.8], [175.3, 65.5], [175.3, 71.2], [177.8, 71.8], [180.3, 75.0],
          [177.8, 66.2], [177.8, 65.9], [177.8, 61.3], [177.8, 61.3], [175.3, 63.1],
          [182.9, 71.8], [182.9, 81.3], [182.9, 77.5], [175.3, 65.7], [175.3, 61.8],
          [175.3, 59.1], [172.7, 56.1], [172.7, 59.1], [172.7, 54.1], [170.2, 59.1],
          [170.2, 53.5], [170.2, 56.6], [172.7, 64.5], [170.2, 59.1], [170.2, 53.6],
          [170.2, 56.5], [172.7, 64.5], [170.2, 59.1], [170.2, 53.5], [170.2, 56.6],
          [177.8, 60.0], [175.3, 66.8], [175.3, 65.5], [175.3, 71.2], [177.8, 71.8],
          [180.3, 75.0], [177.8, 66.2], [177.8, 65.9], [177.8, 61.3], [177.8, 61.3],
          [175.3, 63.1], [182.9, 71.8], [182.9, 81.3], [182.9, 77.5], [175.3, 65.7],
          [175.3, 61.8], [175.3, 59.1], [172.7, 56.1], [172.7, 59.1], [172.7, 54.1],
          [170.2, 59.1], [170.2, 53.5], [170.2, 56.6], [172.7, 64.5], [170.2, 59.1],
          [170.2, 53.6], [170.2, 56.5], [172.7, 64.5], [170.2, 59.1], [170.2, 53.5],
          [170.2, 56.6], [177.8, 60.0], [175.3, 66.8], [175.3, 65.5], [175.3, 71.2],
          [177.8, 71.8], [180.3, 75.0], [177.8, 66.2], [177.8, 65.9], [177.8, 61.3],
          [177.8, 61.3], [175.3, 63.1], [182.9, 71.8], [182.9, 81.3], [182.9, 77.5],
          [175.3, 65.7], [175.3, 61.8], [175.3, 59.1], [172.7, 56.1], [172.7, 59.1],
          [172.7, 54.1]
        ]
      }
    ]
  },
  sunburst: {
    visualMap: {
      type: 'continuous',
      min: 0,
      max: 10,
      inRange: {
        color: ['#2D5F73', '#538EA6', '#F2D1B3', '#F2B8A2', '#F28C8C']
      }
    },
    series: {
      type: 'sunburst',
      data: [
        {
          name: 'Grandpa',
          children: [
            {
              name: 'Uncle Leo',
              value: 15,
              children: [
                { name: 'Cousin Jack', value: 2 },
                { name: 'Cousin Mary', value: 5, children: [{ name: 'Jackson', value: 2 }] },
                { name: 'Cousin Ben', value: 4 }
              ]
            },
            {
              name: 'Father',
              value: 10,
              children: [
                { name: 'Me', value: 5 },
                { name: 'Brother Peter', value: 1 }
              ]
            }
          ]
        },
        {
          name: 'Grandma',
          children: [
            {
              name: 'Uncle Can',
              children: [
                { name: 'Cousin Jane', value: 3 },
                { name: 'Cousin Tony', value: 3 },
                { name: 'Cousin Lily', value: 1 }
              ]
            },
            {
              name: 'Aunt Mary',
              children: [
                { name: 'Cousin Kai', value: 5 }
              ]
            }
          ]
        }
      ],
      radius: [0, '90%'],
      label: {
        rotate: 'radial'
      }
    }
  },
  tree: {
    tooltip: {
      trigger: 'item',
      triggerOn: 'mousemove'
    },
    series: [
      {
        type: 'tree',
        data: [
          {
            name: 'flare',
            children: [
              {
                name: 'data',
                children: [
                  { name: 'converters', children: [{ name: 'DataUtil', value: 3322 }] }
                ]
              },
              {
                name: 'display',
                children: [{ name: 'DirtySprite', value: 8833 }]
              },
              {
                name: 'flex',
                children: [{ name: 'FlareVis', value: 4116 }]
              },
              {
                name: 'query',
                children: [
                  { name: 'methods', children: [{ name: 'add', value: 593 }] }
                ]
              }
            ]
          }
        ],
        top: '1%',
        left: '7%',
        bottom: '1%',
        right: '20%',
        symbolSize: 7,
        edgeShape: 'polyline',
        edgeForkPosition: '63%',
        initialTreeDepth: 3,
        label: {
          position: 'left',
          verticalAlign: 'middle',
          align: 'right',
          fontSize: 9
        },
        leaves: {
          label: {
            position: 'right',
            verticalAlign: 'middle',
            align: 'left'
          }
        },
        emphasis: {
          focus: 'descendant'
        },
        expandAndCollapse: true,
        animationDuration: 550,
        animationDurationUpdate: 750
      }
    ]
  }
};
