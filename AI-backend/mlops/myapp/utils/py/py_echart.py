from flask import request, Markup
from myapp import app

conf = app.config

class Echart():

  # 获取可视化内容
  @staticmethod
  def get_visualization_content(key):
    # if '127.0.0.1' in request.host:
    #   return
    from minio import Minio
    try:
      minioClient = Minio(
        # endpoint=conf.get('MINIO_HOST','minio.kubeflow:9000'),
        endpoint='192.168.224.251:10341',
        access_key='minio',
        secret_key='minio123',
        secure=False
      )
      response = minioClient.get_object('mlpipeline', key)
      content = response.data
      response.close()
      response.release_conn()
      return content
    except Exception as e:
      print(f"Error getting visualization content from {key}: {e}")
      return None
  # 解析csv文件
  @staticmethod
  def parse_csv_from_minio(workflow_name, pod_full, task_name, file_path):
    csv_key = f"{workflow_name}/{pod_full}/{task_name}/{file_path}"
    csv_content = Echart.get_visualization_content(csv_key)
    if not csv_content:
      return None
    if isinstance(csv_content, bytes):
      csv_content = csv_content.decode('utf-8')
    lines = [
      line.strip()
      for line in csv_content.strip().split('\n')
      if line.strip()
    ]
    if len(lines) < 2:
      return None
    headers = [h.strip() for h in lines[0].split(',')]
    rows = [
      [c.strip() for c in line.split(',')]
      for line in lines[1:]
    ]

    return {
      "headers": headers,
      "rows": rows
    }
  
  # 获取x轴
  @staticmethod
  def get_x_axis(rows):
    return [row[0] for row in rows]
  
  # 获取数字列
  @staticmethod
  def get_numeric_columns(headers, rows):
    numeric_cols = []
    for col_idx in range(len(headers)):
      is_numeric = True
      for row in rows:
        try:
          float(row[col_idx])
        except:
          is_numeric = False
          break
      if is_numeric:
        numeric_cols.append(col_idx)
    return numeric_cols
  

  @staticmethod
  def build_content(group_name, value, content_type='html'):
    return {
      "groupName": group_name,
      "groupContent": {
        "value": value,
        "type": content_type
      }
    }
  
  # 图片展示
  @staticmethod
  def build_image_content(
    workflow_name,
    pod_full,
    task_name,
    image_name,
    describe
  ):
    import base64

    image_key = f"{workflow_name}/{pod_full}/{task_name}/{image_name}"

    image_content = Echart.get_visualization_content(
      image_key
    )

    if not isinstance(image_content, bytes):
      return None

    image_base64 = base64.b64encode(
      image_content
    ).decode('utf-8')

    image_ext = (
      image_name.split('.')[-1].lower()
      if '.' in image_name
      else 'png'
    )

    image_html = f"""
    <img
      src="data:image/{image_ext};base64,{image_base64}"
      style="max-width:100%;max-height:500px;"
    />
    """

    return Echart.build_content(
      describe,
      Markup(image_html),
      'html'
    )
  # 构建折线图系列
  @staticmethod
  def build_line_series(headers, rows):
    series = []
    for col_idx in range(1, len(headers)):
      try:
        values = [
          float(row[col_idx])
          for row in rows
        ]
      except:
        continue
      series.append({
        "name": headers[col_idx],
        "data": values
      })
    return series
  
  # 文本展示
  @staticmethod
  def build_text_content(
    text_content,
    describe
  ):
    return Echart.build_content(
      describe,
      Markup(
        f'<pre style="white-space: pre-wrap;">'
        f'{text_content}'
        f'</pre>'
      ),
      'html'
    )

  # 表格展示
  @staticmethod
  def build_table_content(
    workflow_name,
    pod_full,
    task_name,
    file_path,
    describe
  ):

    csv_data = Echart.parse_csv_from_minio(
      workflow_name,
      pod_full,
      task_name,
      file_path
    )

    if not csv_data:
      return None

    headers = csv_data["headers"]
    rows = csv_data["rows"]

    table_html = """
    <table border="1"
           style="border-collapse:collapse;width:100%;">
    """

    table_html += "<thead><tr>"

    for header in headers:
      table_html += (
        f'<th style="padding:8px;">'
        f'{header}'
        f'</th>'
      )
    table_html += "</tr></thead><tbody>"
    for row in rows:
      table_html += "<tr>"
      for cell in row:
        table_html += (
          f'<td style="padding:8px;">'
          f'{cell}'
          f'</td>'
        )
      table_html += "</tr>"
    table_html += "</tbody></table>"
    return Echart.build_content(
      describe,
      Markup(table_html),
      'html'
    )
  # 构建折线图
  @staticmethod
  def build_multiple_lines_option(
    title,
    headers,
    rows
  ):
    x_data = Echart.get_x_axis(rows)
    series = []
    for item in Echart.build_line_series(headers, rows):
      series.append({
        "name": item["name"],
        "type": "line",
        "smooth": True,
        "data": item["data"]
      })
    return {
      "title": {"text": title},
      "tooltip": {"trigger": "axis"},
      "legend": {
        "data": [s["name"] for s in series]
      },
      "xAxis": {
        "type": "category",
        "data": x_data
      },
      "yAxis": {
        "type": "value"
      },
      "series": series
    }

  # 构建面积叠图
  @staticmethod
  def build_area_stack_option(
    title,
    headers,
    rows
  ):
    x_data = Echart.get_x_axis(rows)
    series = []
    for item in Echart.build_line_series(headers, rows):
      series.append({
        "name": item["name"],
        "type": "line",
        "stack": "Total",
        "areaStyle": {},
        "data": item["data"]
      })

    return {
      "title": {"text": title},
      "tooltip": {"trigger": "axis"},
      "legend": {
        "data": [s["name"] for s in series]
      },
      "xAxis": {
        "type": "category",
        "boundaryGap": False,
        "data": x_data
      },
      "yAxis": {
        "type": "value"
      },
      "series": series
    }
  
  # 构建散点图
  @staticmethod
  def build_scatter_effect_option(
    title,
    headers,
    rows
  ):
    numeric_cols = Echart.get_numeric_columns(
      headers,
      rows
    )
    if len(numeric_cols) < 2:
      return None
    x_col = numeric_cols[0]
    y_col = numeric_cols[1]
    scatter_data = []
    for row in rows:
      scatter_data.append([
          float(row[x_col]),
          float(row[y_col])
      ])
    max_point = max(
      scatter_data,
      key=lambda x: x[1]
    )
    min_point = min(
      scatter_data,
      key=lambda x: x[1]
    )
    return {
      "title": {"text": title},
      "tooltip": {},
      "xAxis": {
        "type": "value",
        "name": headers[x_col]
      },
      "yAxis": {
        "type": "value",
        "name": headers[y_col]
      },
      "series": [
        {
          "type": "scatter",
          "data": scatter_data
        },
        {
          "type": "effectScatter",
          "data": [
            max_point,
            min_point
          ]
        }
      ]
    }
  
  # 构建平行坐标图
  @staticmethod
  def build_parallel_option(
      title,
      headers,
      rows
  ):
    if not headers or not rows:
      return None

    parallel_axis = []

    # 第一列分类数据
    categories = [
      row[0]
      for row in rows
    ]

    # 构造 series 数据
    data = []

    for row in rows:

      row_data = [row[0]]

      for value in row[1:]:

        try:
          row_data.append(float(value))
        except Exception:
          row_data.append(None)

      data.append(row_data)

    # 第一列(category)
    parallel_axis.append({
      "dim": 0,
      "name": headers[0],
      "type": "category",
      "data": categories
    })

    # 后续列(value)
    for col_idx in range(1, len(headers)):

      values = []

      for row in rows:
        try:
          values.append(float(row[col_idx]))
        except Exception:
          pass

      axis = {
        "dim": col_idx,
        "name": headers[col_idx]
      }

      if values:
        axis["min"] = min(values)
        axis["max"] = max(values)

      parallel_axis.append(axis)

    return {
      "title": {
        "text": title
      },
      "tooltip": {
        "trigger": "item"
      },
      "visualMap": {
        "show": True,
        "min": 0,
        "max": max(len(data) - 1, 0),
        "dimension": 0,
        "orient": "vertical",
        "left": 10,
        "bottom": 20
      },
      "parallelAxis": parallel_axis,
      "parallel": {
        "left": "8%",
        "right": "10%",
        "top": 50,
        "bottom": 60,
        "parallelAxisDefault": {
          "type": "value",
          "nameLocation": "end",
          "nameGap": 20
        }
      },
      "series": [{
        "name": title,
        "type": "parallel",
        "lineStyle": {
          "width": 2,
          "opacity": 0.8
        },
        "data": data
      }]
    }
  
  # 构建柱状图 —— 特征重要性
  @staticmethod
  def build_bar_option(title, headers, rows):
    if not headers or not rows:
      return None

    if len(headers) < 2:
      return None

    # 优先使用综合特征重要性
    importance_col = None

    if "composite_score" in headers:
      importance_col = headers.index("composite_score")
    else:
      numeric_cols = Echart.get_numeric_columns(headers, rows)

      if len(numeric_cols) < 1:
        return None

      importance_col = numeric_cols[0]

    bar_data = []

    for row in rows:
      try:
        feature = row[0]
        importance = float(row[importance_col])
        bar_data.append({"feature": feature, "importance": importance})
      except Exception:
        continue

    if not bar_data:
      return None

    bar_data.sort(key=lambda x: x["importance"], reverse=True)
    features = [item["feature"] for item in bar_data]
    importance = [item["importance"] for item in bar_data]

    return {
      "title": {
        "text": title
      },
      "tooltip": {
        "trigger": "axis",
        "axisPointer": {
          "type": "shadow"
        }
      },
      "grid": {
        "left": "15%",
        "right": "10%",
        "top": 60,
        "bottom": 60
      },
      "xAxis": {
        "type": "value",
        "name": headers[importance_col]
      },
      "yAxis": {
        "type": "category",
        "data": features,
        "inverse": True
      },
      "series": [
        {
          "name": headers[importance_col],
          "type": "bar",
          "data": importance
        }
      ]
    }

  @staticmethod
  def build_option(
      chart_type,
      title,
      headers,
      rows
  ):

    builders = {
      "echart-line":
        Echart.build_multiple_lines_option,

      "echart-area-stack":
        Echart.build_area_stack_option,

      "echart-scatter-effect":
        Echart.build_scatter_effect_option,

      "echart-parallel":
        Echart.build_parallel_option,

      "echart-bar":
        Echart.build_bar_option,
    }

    builder = builders.get(chart_type)

    if not builder:
      return None

    return builder(
      title,
      headers,
      rows
    )
  
  @staticmethod
  def build_echart_content(
    chart_type,
    describe,
    headers,
    rows
  ):
    option = Echart.build_option(
      chart_type,
      describe,
      headers,
      rows
    )
    if not option:
        return None
    import json
    return Echart.build_content(
      describe,
      json.dumps(option),
      'echart'
    )
  
  @staticmethod
  def build_metric_content(
    metric_type,
    metric,
    workflow_name,
    pod_full,
    task_name,
    describe
  ):
    #
    # image
    #
    if metric_type == 'image':
      image_name = metric.get('image', '')
      if not image_name:
        return None
      return Echart.build_image_content(
        workflow_name,
        pod_full,
        task_name,
        image_name,
        describe
      )
    #
    # text
    #
    elif metric_type == 'text':
      text_content = metric.get('text', '')
      if not text_content:
        return None
      return Echart.build_text_content(
        text_content,
        describe
      )
    #
    # table
    #
    elif metric_type == 'table':
      file_path = metric.get('file_path', '')
      if not file_path:
        return None

      return Echart.build_table_content(
        workflow_name,
        pod_full,
        task_name,
        file_path,
        describe
      )

    #
    # echart
    #
    elif metric_type.startswith('echart-'):
      file_path = metric.get('file_path', '')
      if not file_path:
          return None
      csv_data = Echart.parse_csv_from_minio(
        workflow_name,
        pod_full,
        task_name,
        file_path
      )

      if not csv_data:
        return None

      return Echart.build_echart_content(
        metric_type,
        describe,
        csv_data["headers"],
        csv_data["rows"]
      )

    return None