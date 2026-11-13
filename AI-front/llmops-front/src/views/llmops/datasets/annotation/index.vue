<template>
  <div class="annotation-container">
    <div class="annotation-content">
      <!-- 头部 -->
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">数据标注</h1>
          <p class="page-subtitle">管理数据标注项目，高效完成数据标注任务</p>
        </div>
        <div class="header-actions">
          <el-button class="view-btn" @click="viewMode = 'grid'" :class="{ active: viewMode === 'grid' }">
            <i class="el-icon-menu"></i>
          </el-button>
          <el-button class="view-btn" @click="viewMode = 'list'" :class="{ active: viewMode === 'list' }">
            <i class="el-icon-s-operation"></i>
          </el-button>
          <el-button type="primary" class="create-btn" @click="handleCreate">
            <i class="el-icon-plus"></i> 创建
          </el-button>
        </div>
      </div>

      <!-- 项目统计 -->
      <!-- <div class="metric-grid">
        <div class="metric-card blue">
          <div class="metric-main">
            <span class="metric-label">项目总数</span>
            <strong>{{ stats.total }}</strong>
          </div>
          <div class="metric-icon"><i class="el-icon-folder-opened"></i></div>
        </div>
        <div class="metric-card green">
          <div class="metric-main">
            <span class="metric-label">已标注</span>
            <strong>{{ stats.completed }}</strong>
          </div>
          <div class="metric-icon"><i class="el-icon-check"></i></div>
        </div>
        <div class="metric-card orange">
          <div class="metric-main">
            <span class="metric-label">进行中</span>
            <strong>{{ stats.inProgress }}</strong>
          </div>
          <div class="metric-icon"><i class="el-icon-time"></i></div>
        </div>
        <div class="metric-card purple">
          <div class="metric-main">
            <span class="metric-label">标注人员</span>
            <strong>{{ stats.annotators }}</strong>
          </div>
          <div class="metric-icon"><i class="el-icon-user-solid"></i></div>
        </div>
      </div> -->

      <!-- 项目卡片列表 -->
      <div class="project-section" v-loading="loading" element-loading-text="加载中...">
        <div v-if="!loading && !projects.length" class="project-empty">
          <i class="el-icon-folder-opened"></i>
          <p>暂无标注项目</p>
          <el-button size="small" type="primary" plain @click="loadDashboard">刷新</el-button>
        </div>

        <div v-else-if="viewMode === 'grid'" class="project-grid">
          <!-- <div v-for="project in projects" :key="project.id" class="project-card card-hover">
            <div class="card-header">
              <div class="project-title-row">
                <div class="project-name" @click="handleOpen(project)">{{ project.name }}</div>
                <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, project)">
                  <i class="el-icon-more card-more"></i>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item command="settings">Settings</el-dropdown-item>
                    <el-dropdown-item command="label">Label</el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </div>
              <div class="project-subtitle">{{ project.subtitle }}</div>
            </div>

            <div class="card-stats">
              <div class="stat-item" title="已完成">
                <i class="el-icon-check stat-icon completed"></i>
                <span class="stat-value">{{ project.completed }}</span>
              </div>
              <div class="stat-item" title="已跳过">
                <i class="el-icon-minus stat-icon skipped"></i>
                <span class="stat-value">{{ project.skipped }}</span>
              </div>
              <div class="stat-item" title="预测">
                <i class="el-icon-magic-stick stat-icon predicted"></i>
                <span class="stat-value">{{ project.predicted }}</span>
              </div>
            </div>

            <div class="card-progress">
              <el-progress :percentage="project.progress" :show-text="false" :stroke-width="4"
                :color="progressColor(project.progress)"></el-progress>
            </div>

            <div class="card-footer">
              <span class="project-date">
                <i class="el-icon-date"></i> {{ project.date }}
              </span>
              <div class="project-avatar" :style="{ backgroundColor: project.avatarColor }">
                {{ project.avatar }}
              </div>
            </div>
          </div> -->
                    <div v-for="project in projects" :key="project.id" class="project-card card-hover">
            <div class="card-header">
              <div class="project-title-row">
                <div class="project-name" @click="handleOpen(project)">{{ project.name }}</div>
                <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, project)">
                  <i class="el-icon-more card-more"></i>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item command="settings">Settings</el-dropdown-item>
                    <el-dropdown-item command="label">Label</el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </div>
            </div>

            <div class="card-count-row">
              <div class="card-count">
                <span class="count-completed">{{ project.completed }}</span>
                <span class="count-sep"> / </span>
                <span class="count-total">{{ project.total }}</span>
              </div>
              <div class="card-stats">
                <div class="stat-item" title="已完成">
                  <i class="el-icon-check stat-icon completed"></i>
                  <span class="stat-value">{{ project.completed }}</span>
                </div>
                <div class="stat-item" title="已跳过">
                  <i class="el-icon-minus stat-icon skipped"></i>
                  <span class="stat-value">{{ project.skipped }}</span>
                </div>
                <div class="stat-item" title="预测">
                  <i class="el-icon-magic-stick stat-icon predicted"></i>
                  <span class="stat-value">{{ project.predicted }}</span>
                </div>
              </div>
            </div>

            <!-- <div class="project-subtitle">{{ project.subtitle }}</div> -->

            <div class="card-footer">
              <span class="project-date">
                <i class="el-icon-date"></i> {{ project.date }}
              </span>
              <div class="project-avatar" :style="{ backgroundColor: project.avatarColor }">
                {{ project.avatar }}
              </div>
            </div>
          </div>
        </div>

        <!-- 列表视图 -->
        <div v-else class="table-card card-hover">
          <el-table :data="projects" style="width: 100%" stripe
            :header-cell-style="{ backgroundColor: '#f9fafb', color: '#374151' }">
            <el-table-column prop="name" label="项目名称" min-width="180">
              <template slot-scope="scope">
                <span class="link-text" @click="handleOpen(scope.row)">{{ scope.row.name }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="subtitle" label="描述" min-width="160" show-overflow-tooltip></el-table-column>
            <el-table-column label="已完成" width="100">
              <template slot-scope="scope">
                <span class="status-completed"><i class="el-icon-check"></i> {{ scope.row.completed }}</span>
              </template>
            </el-table-column>
            <el-table-column label="已跳过" width="100">
              <template slot-scope="scope">
                <span class="status-skipped"><i class="el-icon-minus"></i> {{ scope.row.skipped }}</span>
              </template>
            </el-table-column>
            <el-table-column label="预测" width="100">
              <template slot-scope="scope">
                <span class="status-predicted"><i class="el-icon-magic-stick"></i> {{ scope.row.predicted }}</span>
              </template>
            </el-table-column>
            <el-table-column label="进度" min-width="180">
              <template slot-scope="scope">
                <el-progress :percentage="scope.row.progress" :stroke-width="8"
                  :color="progressColor(scope.row.progress)"></el-progress>
              </template>
            </el-table-column>
            <el-table-column prop="date" label="更新时间" min-width="160"></el-table-column>
            <el-table-column label="操作" width="160" fixed="right">
              <template slot-scope="scope">
                <el-button type="text" size="small" @click="handleCommand('settings', scope.row)">Settings</el-button>
                <el-button type="text" size="small" @click="handleCommand('label', scope.row)">Label</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!-- 创建标注项目弹框 -->
    <el-dialog
      :visible.sync="createDialogVisible"
      width="1200px"
      :close-on-click-modal="false"
      :show-close="false"
      class="create-project-dialog"
      custom-class="create-project-dialog"
      :before-close="handleCreateDialogClose"
      @closed="resetCreateForm">
      <span slot="title" class="dialog-title-slot">
        <span class="dialog-title-text">创建标注项目</span>
        <span class="dialog-title-actions">
          <el-button class="header-cancel-btn" @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" class="header-save-btn" :loading="uploadLoading" @click="handleSaveProject">保存</el-button>
        </span>
      </span>
      <div class="create-project-body">
        <el-tabs v-model="activeCreateTab" class="create-project-tabs">
          <!-- 步骤一：项目名称 -->
          <el-tab-pane label="项目名称" name="name">
            <div class="tab-step-content">
              <el-form :model="projectForm" label-width="0px" class="project-form">
                <el-form-item>
                  <label class="form-label">项目名称 <span class="required">*</span></label>
                  <el-input v-model="projectForm.name" placeholder="请输入项目名称"></el-input>
                </el-form-item>
                <el-form-item>
                  <label class="form-label">工作空间</label>
                  <el-input v-model="projectForm.workspace" disabled></el-input>
                  <div class="form-hint">通过将项目组织到工作空间中来简化项目管理。</div>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>

          <!-- 步骤二：数据导入 -->
          <el-tab-pane label="数据导入" name="import">
            <div class="tab-step-content">
              <div class="import-toolbar">
                <div class="url-input-group">
                  <el-input
                    v-model="projectForm.datasetUrl"
                    placeholder="Dataset URL"
                    class="url-input"></el-input>
                  <el-button type="primary" plain @click="handleAddUrl">添加 URL</el-button>
                </div>
                <span class="or-text">或</span>
                <el-button type="primary" @click="triggerFileUpload">
                  <i class="el-icon-upload2"></i> 上传文件
                </el-button>
              </div>

              <div class="url-list" v-if="projectForm.urls.length">
                <div v-for="(url, idx) in projectForm.urls" :key="idx" class="url-item">
                  <i class="el-icon-link"></i>
                  <span class="url-text">{{ url }}</span>
                  <i class="el-icon-close url-remove" @click="removeUrl(idx)"></i>
                </div>
              </div>

              <div
                class="upload-dropzone"
                :class="{ 'is-dragover': isDragover }"
                @click="triggerFileUpload"
                @dragover.prevent="isDragover = true"
                @dragleave.prevent="isDragover = false"
                @drop.prevent="handleDrop">
                <div class="dropzone-icon">
                  <i class="el-icon-upload"></i>
                </div>
                <div class="dropzone-title">将文件拖拽到此处</div>
                <div class="dropzone-subtitle">或点击浏览</div>

                <div class="file-types">
                  <div class="file-type-row" v-for="type in fileTypes" :key="type.label">
                    <span class="file-type-label">{{ type.label }}</span>
                    <span class="file-type-exts">{{ type.exts }}</span>
                  </div>
                </div>

                <div class="dropzone-notes">
                  <div class="notes-title">重要提示：</div>
                  <ul class="notes-list">
                    <li>由于上传限制，建议使用云存储而非直接上传。</li>
                    <li>对于 PDF 文件，请使用多图标注。</li>
                    <li>请查看文档以了解如何导入预标注数据。</li>
                  </ul>
                </div>
              </div>

              <div class="selected-files-list" v-if="selectedFiles.length">
                <div class="selected-files-title">
                  已选择 {{ selectedFiles.length }} 个文件
                </div>
                <div class="selected-files-items">
                  <div
                    v-for="(item, idx) in selectedFiles"
                    :key="idx"
                    class="selected-file-item">
                    <i class="el-icon-document"></i>
                    <span class="selected-file-name">{{ item.file.name }}</span>
                    <span class="selected-file-size">{{ formatFileSize(item.file.size) }}</span>
                    <i class="el-icon-close selected-file-remove" @click="removeSelectedFile(idx)"></i>
                  </div>
                </div>
              </div>

              <input
                ref="fileInput"
                type="file"
                multiple
                style="display: none"
                @change="handleFileChange" />
            </div>
          </el-tab-pane>

          <!-- 步骤三：标注设置 -->
          <el-tab-pane label="标注设置" name="setup">
            <!-- 模板选择视图 -->
            <div v-if="setupView === 'templates'" class="template-selector">
              <div class="template-sidebar">
                <div
                  v-for="cat in templateCategories"
                  :key="cat.key"
                  class="template-cat-item"
                  :class="{ active: activeCategory === cat.key }"
                  @click="activeCategory = cat.key">
                  {{ cat.label }}
                  <i class="el-icon-arrow-right"></i>
                </div>
              </div>
              <div class="template-main">
                <div class="template-grid">
                  <div
                    v-for="tpl in filteredTemplates"
                    :key="tpl.id"
                    class="template-card"
                    @click="handleSelectTemplate(tpl)">
                    <div class="template-preview" :style="{ background: tpl.bg }">
                      <img v-if="tpl.image" :src="tpl.image" class="template-img" @error="handleImgError(tpl)" />
                      <i v-else :class="tpl.icon"></i>
                    </div>
                    <div class="template-name">{{ tpl.name }}</div>
                  </div>
                  <div class="template-card custom-template-card" @click="handleSelectCustom">
                    <div class="template-preview custom-preview">
                      <i class="el-icon-edit-outline"></i>
                    </div>
                    <div class="template-name">自定义</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 标注接口配置视图 -->
            <div v-else-if="setupView === 'config'" class="labeling-interface">
              <!-- 左侧配置面板 -->
              <div class="li-config-panel">
                <div class="li-header">
                  <h3 class="li-title">Labeling Interface</h3>
                  <div class="li-header-actions">
                    <el-button size="small" class="browse-templates-btn" @click="backToTemplates">
                      Browse Templates
                    </el-button>
                    <div class="code-visual-toggle">
                      <!-- <span :class="{ active: liMode === 'code' }" @click="liMode = 'code'">Code</span>
                      <span :class="{ active: liMode === 'visual' }" @click="liMode = 'visual'">Visual</span> -->
                      <span :class="{ active: liMode === 'code' }" @click="switchMode('code')">Code</span>
                      <span :class="{ active: liMode === 'visual' }" @click="switchMode('visual')">Visual</span>
                    </div>
                  </div>
                </div>

                <div class="li-code-editor" v-if="liMode === 'code'">
                  <el-input
                    type="textarea"
                    :rows="22"
                    v-model="customConfig"
                    resize="none"
                    class="code-textarea">
                  </el-input>
                </div>

                                <div class="li-visual-editor" v-else>
                  <div class="li-section">
                    <div class="li-section-title">Configure data</div>
                    <div class="data-config-row">
                      <span class="data-config-label">{{ dataFieldLabel }}</span>
                      <el-select v-model="imageSource" class="data-config-select" size="small">
                        <el-option label="<set manually>" value="manual"></el-option>
                        <el-option label="从数据导入" value="data"></el-option>
                      </el-select>
                      <el-input v-model="imageVariable" class="data-config-input" size="small" :placeholder="imageVariable || '$' + dataType"></el-input>
                    </div>
                  </div>

                  <div class="li-section">
                    <div class="li-section-title">Add label names</div>
                    <div class="li-section-desc">Use new line as a separator to add multiple labels</div>
                    <el-input
                      type="textarea"
                      v-model="newLabelsInput"
                      :rows="3"
                      resize="vertical"
                      class="labels-textarea"
                      placeholder="Airplane&#10;Car"></el-input>
                    <el-button size="small" class="add-labels-btn" @click="addLabels">Add</el-button>
                  </div>

                  <div class="li-section" v-if="labels.length">
                    <div class="li-section-title">Labels ({{ labels.length }})</div>
                    <div class="labels-list">
                      <div
                        v-for="(label, idx) in labels"
                        :key="idx"
                        class="label-item"
                        :style="{ borderLeftColor: label.color, backgroundColor: label.color + '1a' }">
                        <span class="label-item-name">{{ label.name }}</span>
                        <i class="el-icon-close label-item-remove" @click="removeLabel(idx)"></i>
                      </div>
                    </div>
                  </div>

                  <div class="li-section">
                    <div class="li-section-title">Configure settings</div>
                    <div class="setting-row" v-if="dataType === 'image'">
                      <span class="setting-label">Width of region borders</span>
                      <el-input v-model.number="borderWidth" class="setting-input" size="small" type="number" min="1"></el-input>
                    </div>
                    <div class="setting-checkbox-row" v-if="dataType === 'image'">
                      <el-checkbox v-model="allowImageZoom">Allow image zoom (ctrl+wheel)</el-checkbox>
                    </div>
                    <div class="setting-checkbox-row" v-if="dataType === 'image'">
                      <el-checkbox v-model="showZoomControls">Show controls to zoom in and out</el-checkbox>
                    </div>
                    <div class="setting-checkbox-row" v-if="dataType === 'image'">
                      <el-checkbox v-model="showRotateControls">Show controls to rotate image</el-checkbox>
                    </div>
                  </div>
                </div>
              </div>

                            <!-- 右侧预览面板 -->
              <div class="li-preview-panel">
                <div class="preview-header">Preview</div>
                <div class="preview-body">
                  <!-- 空配置 -->
                  <div class="preview-empty" v-if="!previewConfig.type">
                    <i class="el-icon-document"></i>
                    <p>预览区域</p>
                    <span>在左侧编写或选择标注配置后，此处将显示预览效果</span>
                  </div>

                  <!-- 图片类型预览 -->
                  <div class="preview-type-wrapper" v-else-if="previewConfig.type === 'image'">
                    <div class="preview-canvas-wrapper">
                      <div class="preview-canvas">
                        <img
                          class="preview-image"
                          :src="previewImageSrc"
                          alt="preview"
                          @error="handlePreviewImgError"
                          v-show="!previewImgError" />
                        <div class="preview-image-placeholder" v-show="previewImgError">
                          <i class="el-icon-picture-outline"></i>
                          <span>图片预览</span>
                        </div>
                        <div
                          v-for="(box, idx) in previewBoxes"
                          :key="idx"
                          class="preview-bbox"
                          :style="{
                            left: box.left + '%',
                            top: box.top + '%',
                            width: box.width + '%',
                            height: box.height + '%',
                            borderColor: box.color,
                            borderWidth: (previewConfig.strokeWidth || borderWidth) + 'px'
                          }">
                          <span class="bbox-label" :style="{ backgroundColor: box.color }">{{ box.label }}</span>
                        </div>
                      </div>
                      <div class="preview-toolbar" v-if="previewConfig.allowZoom">
                        <div class="toolbar-btn active" title="选择"><i class="el-icon-top-left"></i></div>
                        <div class="toolbar-btn" title="拖动"><i class="el-icon-rank"></i></div>
                        <div class="toolbar-btn" title="放大"><i class="el-icon-zoom-in"></i></div>
                        <div class="toolbar-btn" title="网格"><i class="el-icon-menu"></i></div>
                        <div class="toolbar-btn" title="旋转"><i class="el-icon-refresh-right"></i></div>
                      </div>
                    </div>
                    <div class="preview-labels-bar" v-if="previewConfig.labels.length">
                      <span
                        v-for="(label, idx) in previewConfig.labels"
                        :key="idx"
                        class="preview-label-tag"
                        :style="{ backgroundColor: label.color + '26', color: label.color, borderColor: label.color }">
                        {{ label.value }}
                      </span>
                    </div>
                    <div class="preview-hint">Select label and click the image to start</div>
                  </div>

                  <!-- 文本类型预览 -->
                  <div class="preview-type-wrapper" v-else-if="previewConfig.type === 'text'">
                    <template v-for="(field, fIdx) in previewConfig.textFields">
                      <div class="preview-text-card" :key="'f' + fIdx">
                        <div class="preview-field-label" v-if="field.name">{{ field.name }}</div>
                        <div class="preview-text-content" v-if="!field.isHtml">{{ field.content }}</div>
                        <div class="preview-text-content" v-else v-html="field.content"></div>
                      </div>
                    </template>
                    <div class="preview-choices-area" v-if="previewConfig.choices.length">
                      <div class="preview-choices-title">请选择：</div>
                      <div class="preview-choices-list">
                        <span
                          v-for="(choice, idx) in previewConfig.choices"
                          :key="idx"
                          class="preview-choice-tag">
                          {{ choice }}
                        </span>
                      </div>
                    </div>
                    <div class="preview-choices-area" v-if="previewConfig.labels.length">
                      <div class="preview-choices-title">标签：</div>
                      <div class="preview-choices-list">
                        <span
                          v-for="(label, idx) in previewConfig.labels"
                          :key="idx"
                          class="preview-label-tag"
                          :style="{ backgroundColor: label.color + '26', color: label.color, borderColor: label.color }">
                          {{ label.value }}
                        </span>
                      </div>
                    </div>
                  </div>

                                    <!-- 音频类型预览 -->
                  <div class="preview-type-wrapper" v-else-if="previewConfig.type === 'audio'">
                    <div class="preview-audio-card">
                      <audio
                        ref="previewAudio"
                        :src="previewAudioSrc"
                        @timeupdate="onAudioTimeUpdate"
                        @loadedmetadata="onAudioLoadedMetadata"
                        @ended="onAudioEnded"
                        preload="metadata"></audio>
                      <div class="audio-player-main">
                        <div class="audio-wave-container" @click="seekAudio">
                          <div class="audio-wave-bars">
                            <span
                              v-for="n in 60"
                              :key="n"
                              class="audio-wave-bar"
                              :class="{ played: n <= audioPlayedBars }"
                              :style="{ height: getWaveBarHeight(n) + 'px' }"></span>
                          </div>
                          <div class="audio-progress-line">
                            <div class="audio-progress-played" :style="{ width: audioProgressPercent + '%' }"></div>
                            <div class="audio-progress-thumb" :style="{ left: audioProgressPercent + '%' }"></div>
                          </div>
                        </div>
                        <div class="audio-controls-row">
                          <button class="audio-play-btn" @click="toggleAudioPlay">
                            <i :class="audioPlaying ? 'el-icon-video-pause' : 'el-icon-video-play'"></i>
                          </button>
                          <div class="audio-time-display">
                            <span class="audio-current-time">{{ formatAudioTime(audioCurrentTime) }}</span>
                            <span class="audio-time-sep"> / </span>
                            <span class="audio-total-time">{{ formatAudioTime(audioDuration) }}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                    <div class="preview-choices-area" v-if="previewConfig.choices.length">
                      <div class="preview-choices-title">请选择：</div>
                      <div class="preview-choices-list">
                        <span
                          v-for="(choice, idx) in previewConfig.choices"
                          :key="idx"
                          class="preview-choice-tag">
                          {{ choice }}
                        </span>
                      </div>
                    </div>
                  </div>

                  <!-- 时间序列类型预览 -->
                  <div class="preview-type-wrapper" v-else-if="previewConfig.type === 'timeseries'">
                    <div class="ts-preview-card">
                      <div class="ts-labels-row" v-if="previewConfig.labels.length">
                        <span
                          v-for="(label, idx) in previewConfig.labels"
                          :key="idx"
                          class="ts-label-tag"
                          :style="{ borderLeftColor: label.color, backgroundColor: label.color + '22', color: label.color }">
                          {{ label.value }}
                        </span>
                      </div>
                      <div class="ts-chart-wrap">
                        <svg class="ts-chart" viewBox="0 0 100 100" preserveAspectRatio="none">
                          <defs>
                            <linearGradient id="tsAreaGrad" x1="0" y1="0" x2="0" y2="1">
                              <stop offset="0%" stop-color="#3b82f6" stop-opacity="0.25"/>
                              <stop offset="100%" stop-color="#3b82f6" stop-opacity="0"/>
                            </linearGradient>
                          </defs>
                          <g class="ts-grid">
                            <line v-for="(lbl, i) in tsChartData.yLabels" :key="'hy'+i"
                              x1="0" :y1="lbl.y" x2="100" :y2="lbl.y" stroke="#e5e7eb" stroke-width="0.2"/>
                          </g>
                          <path :d="tsChartData.areaPath" fill="url(#tsAreaGrad)"/>
                          <path :d="tsChartData.path" fill="none" stroke="#3b82f6" stroke-width="0.5" vector-effect="non-scaling-stroke"/>
                          <line x1="0" y1="50" x2="100" y2="50" stroke="#9ca3af" stroke-width="0.2" stroke-dasharray="1 1"/>
                        </svg>
                        <div class="ts-y-axis">
                          <span v-for="(lbl, i) in tsChartData.yLabels" :key="'yl'+i" class="ts-y-label" :style="{ top: lbl.y + '%' }">{{ lbl.value }}</span>
                        </div>
                        <div class="ts-top-axis">
                          <span v-for="(lbl, i) in tsChartData.topLabels" :key="'tl'+i" class="ts-top-label" :style="{ left: lbl.x + '%' }">{{ lbl.label }}</span>
                        </div>
                        <div class="ts-x-axis">
                          <span v-for="(lbl, i) in tsChartData.xLabels" :key="'xl'+i" class="ts-x-label" :style="{ left: lbl.x + '%' }">{{ lbl.label }}</span>
                        </div>
                        <div class="ts-channel-legend" v-if="previewConfig.timeseries && previewConfig.timeseries.channels.length">
                          <span class="ts-channel-name">{{ previewConfig.timeseries.channels[0].legend }}</span>
                        </div>
                        <div class="ts-selection-region"></div>
                      </div>
                    </div>
                  </div>

                  <!-- HyperText 类型预览 -->
                  <div class="preview-type-wrapper" v-else-if="previewConfig.type === 'hypertext'">
                    <div class="hypertext-preview-card">
                      <div class="hypertext-labels-bar" v-if="previewConfig.labels.length">
                        <span
                          v-for="(label, idx) in previewConfig.labels"
                          :key="idx"
                          class="hypertext-label-tag"
                          :style="{ borderLeftColor: label.color, backgroundColor: label.color + '22', color: label.color }">
                          {{ label.value }}
                          <span class="hypertext-label-count">{{ idx + 1 }}</span>
                        </span>
                      </div>
                      <div class="hypertext-content" v-html="previewConfig.hypertext.content"></div>
                    </div>
                  </div>

                  <!-- Pairwise 类型预览 -->
                  <div class="preview-type-wrapper" v-else-if="previewConfig.type === 'pairwise'">
                    <div class="pairwise-preview-card">
                      <template v-if="previewConfig.pairwise && previewConfig.pairwise.prompt">
                        <div class="pairwise-prompt">
                          <div class="pairwise-prompt-text">{{ previewConfig.pairwise.prompt.content }}</div>
                        </div>
                        <div class="pairwise-answers-row">
                          <div
                            v-for="(item, idx) in previewConfig.pairwise.items"
                            :key="idx"
                            class="pairwise-answer-card">
                            <div class="pairwise-answer-text">{{ item.content }}</div>
                          </div>
                        </div>
                      </template>
                      <template v-else>
                        <div class="pairwise-title">Select one of two items</div>
                        <div class="pairwise-items-list">
                          <div
                            v-for="(item, idx) in (previewConfig.pairwise ? previewConfig.pairwise.items : [])"
                            :key="idx"
                            class="pairwise-item-text">
                            {{ item.content }}
                          </div>
                        </div>
                      </template>
                    </div>
                  </div>

                  <!-- 视频类型预览 -->
                  <div class="preview-type-wrapper" v-else-if="previewConfig.type === 'video'">
                    <div class="preview-video-card">
                      <i class="el-icon-video-camera preview-video-icon"></i>
                      <span class="preview-video-hint">视频预览</span>
                    </div>
                  </div>

                  <!-- 其他类型 -->
                  <div class="preview-type-wrapper" v-else>
                    <div class="preview-empty">
                      <i class="el-icon-document"></i>
                      <p>预览区域</p>
                      <span>当前配置类型暂不支持预览</span>
                    </div>
                  </div>
                </div>
              </div>
              
              
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button v-if="activeCreateTab !== 'name'" @click="prevStep">上一步</el-button>
        <el-button
          v-if="activeCreateTab !== 'setup'"
          type="primary"
          @click="nextStep">下一步</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getProjects,
  getTemplates,
  createProject,
  setProjectLabelConfig,
  uploadDatasetFiles,
  uploadDatasetByUrl,
  generateSampleTask
} from '@/api/llmops/labelstudio'

export default {
  name: 'DatasetAnnotation',
  data() {
    return {
      viewMode: 'grid',
      loading: false,
      createDialogVisible: false,
      activeCreateTab: 'name',
      isDragover: false,
      activeCategory: 'cv',
      selectedTemplate: null,
      selectedTemplateObj: null,
      setupView: 'templates',
      liMode: 'code',
      customConfig: '<View></View>',
      visualDirty: false,
      isParsingVisual: false,
      sampleTaskData: {},
      sampleTaskLoading: false,
      _sampleTaskTimer: null,
      showTemplates: false,
      newQualityLabel: '',
      newSafetyLabel: '',
      selectedSafety: '',
      imageSource: 'manual',
      imageVariable: '$image',
      dataType: 'image',
      newLabelsInput: '',
      labels: [],
      borderWidth: 3,
      allowImageZoom: true,
      showZoomControls: false,
      showRotateControls: false,
      previewImgError: false,
      audioPlaying: false,
      audioCurrentTime: 0,
      audioDuration: 0,
      labelColors: [
        '#ef4444', '#3b82f6', '#10b981', '#f59e0b', '#8b5cf6',
        '#ec4899', '#14b8a6', '#f97316', '#6366f1', '#84cc16'
      ],
      dataFields: [
        { variable: '$instruction', source: 'manual' },
        { variable: '$input', source: 'manual' },
        { variable: '$output', source: 'manual' }
      ],
      qualityLabels: [],
      safetyLabels: ['安全', '存在风险'],
      qualityItems: [
        { name: '指令质量', value: 0 },
        { name: '回答正确性', value: 0 },
        { name: '回答相关性', value: 0 },
        { name: '回答完整性', value: 0 },
        { name: '回答流畅性', value: 0 }
      ],
      projectForm: {
        name: '',
        description: '',
        workspace: '',
        datasetUrl: '',
        urls: []
      },
      selectedFiles: [],
      uploadLoading: false,
      workspaceOptions: [
        { value: 'ws1', label: '默认工作空间' },
        { value: 'ws2', label: 'NLP 团队' },
        { value: 'ws3', label: 'CV 团队' },
        { value: 'ws4', label: '语音团队' }
      ],
      fileTypes: [
        { label: '图像', exts: 'bmp, gif, jpg, jpeg, png, svg, webp' },
        { label: '音频', exts: 'wav, mp3, flac, m4a, ogg' },
        { label: '视频', exts: 'mp4, webm' },
        { label: 'HTML / 超文本', exts: 'html, htm, xml' },
        { label: '文本', exts: 'txt' },
        { label: '结构化数据', exts: 'csv, tsv, json' },
        { label: 'PDF', exts: 'pdf' }
      ],
      dataTypeExtMap: {
        image: { label: '图像', exts: ['bmp', 'gif', 'jpg', 'jpeg', 'png', 'svg', 'webp'] },
        audio: { label: '音频', exts: ['wav', 'mp3', 'flac', 'm4a', 'ogg'] },
        video: { label: '视频', exts: ['mp4', 'webm'] },
        html: { label: 'HTML / 超文本', exts: ['html', 'htm', 'xml'] },
        text: { label: '文本', exts: ['txt', 'json', 'csv', 'tsv'] },
        paragraphs: { label: '段落文本', exts: ['txt', 'json', 'csv', 'tsv'] },
        pairwise: { label: '配对文本', exts: ['json', 'csv', 'tsv', 'txt'] },
        timeseries: { label: '时间序列', exts: ['json', 'csv', 'tsv'] }
      },
      templateCategories: [
        { key: 'cv', label: '计算机视觉', group: 'Computer Vision' },
        { key: 'nlp', label: '自然语言处理', group: 'Natural Language Processing' },
        { key: 'audio', label: '音频 / 语音处理', group: 'Audio/Speech Processing' },
        { key: 'conversational', label: '对话式 AI', group: 'Conversational AI' },
        { key: 'chat', label: '聊天', group: 'Chat' },
        { key: 'ranking', label: '排序与评分', group: 'Ranking & Scoring' },
        { key: 'structured', label: '结构化数据解析', group: 'Structured Data Parsing' },
        { key: 'timeseries', label: '时间序列分析', group: 'Time Series Analysis' },
        { key: 'videos', label: '视频', group: 'Videos' },
        { key: 'generative', label: '生成式 AI', group: 'Generative AI' },
        { key: 'community', label: '社区贡献', group: 'Community Contributions' },
      ],
      templates: [],
      stats: {
        total: 0,
        completed: 0,
        inProgress: 0,
        annotators: 0
      },
      projects: []
    }
  },
  computed: {
    previewBoxes() {
      const labels = this.previewConfig.labels
      if (!labels.length) return []
      const positions = [
        { left: 5, top: 12, width: 38, height: 28 },
        { left: 52, top: 18, width: 40, height: 30 },
        { left: 25, top: 58, width: 42, height: 32 },
        { left: 70, top: 60, width: 22, height: 14 },
        { left: 12, top: 72, width: 18, height: 12 }
      ]
      return labels.slice(0, 5).map((label, idx) => {
        const pos = positions[idx % positions.length]
        return { ...pos, label: label.value, color: label.color }
      })
    },
    filteredTemplates() {
      if (this.activeCategory === 'custom') return []
      return this.templates
        .filter(t => t.category === this.activeCategory)
        .sort((a, b) => {
          const oa = a.order == null ? Number.MAX_SAFE_INTEGER : a.order
          const ob = b.order == null ? Number.MAX_SAFE_INTEGER : b.order
          return oa - ob
        })
    },
    allTemplates() {
      return this.templates
    },
    dataFieldLabel() {
      const map = {
        image: 'Use image from',
        audio: 'Use audio from',
        paragraphs: 'Use paragraphs from',
        video: 'Use video from',
        text: 'Use text from',
        html: 'Use hypertext from',
        timeseries: 'Use timeseries from',
        pairwise: 'Use text from'
      }
      return map[this.dataType] || 'Use data from'
    },
    labelConfigCode() {
      const lines = []
      lines.push('<View>')
      const varName = (this.imageVariable || '$image').replace('$', '')
      const dataValue = this.imageVariable || '$image'
      const dataType = this.dataType || 'image'
      const dataTagMap = {
        image: 'Image',
        audio: 'Audio',
        paragraphs: 'Paragraphs',
        video: 'Video',
        text: 'Text',
        html: 'HyperText',
        timeseries: 'TimeSeries',
        pairwise: 'Text'
      }
      const dataTag = dataTagMap[dataType] || 'Image'
      if (dataType === 'pairwise') {
        lines.push(`  <${dataTag} name="${varName}" value="${dataValue}"/>`)
        lines.push(`  <Pairwise name="pairwise" toName="${varName}1 ${varName}2">`)
        lines.push(`    <Text name="${varName}1" value="$${varName}1"/>`)
        lines.push(`    <Text name="${varName}2" value="$${varName}2"/>`)
        lines.push('  </Pairwise>')
      } else if (dataType === 'image') {
        lines.push(`  <${dataTag} name="${varName}" value="${dataValue}" zoom="${this.allowImageZoom}"${this.showZoomControls ? ' zoomControls="true"' : ''}${this.showRotateControls ? ' rotateControls="true"' : ''}/>`)
      } else {
        lines.push(`  <${dataTag} name="${varName}" value="${dataValue}"/>`)
      }
      if (this.labels.length) {
        const labelsTagMap = {
          image: 'RectangleLabels',
          timeseries: 'TimeSeriesLabels'
        }
        const labelsTag = labelsTagMap[dataType] || 'Labels'
        lines.push('  <' + labelsTag + ' name="label" toName="' + varName + '" strokeWidth="' + this.borderWidth + '">')
        this.labels.forEach(label => {
          lines.push(`    <Label value="${label.name}" background="${label.color}"/>`)
        })
        lines.push('  </' + labelsTag + '>')
      }
      lines.push('</View>')
      return lines.join('\n')
    },
    previewConfig() {
      const xml = (this.liMode === 'visual' ? this.labelConfigCode : this.customConfig) || ''
      if (!xml.trim() || xml === '<View></View>') {
        return { type: '', labels: [], choices: [] }
      }
      let doc = null
      try {
        doc = new DOMParser().parseFromString(xml, 'text/xml')
      } catch (e) {
        return { type: '', labels: [], choices: [] }
      }
      if (!doc || !doc.documentElement || doc.querySelector('parsererror')) {
        return { type: '', labels: [], choices: [] }
      }

      const result = {
        type: '',
        labels: [],
        choices: [],
        allowZoom: false,
        strokeWidth: this.borderWidth,
        sampleText: '',
        sampleHtml: '',
        dataVariable: '',
        textFields: [],
        pairwise: null,
        hypertext: null,
        timeseries: null
      }
      let sampleData = null
      try {
        const commentMatch = xml.match(/<!--\s*([\s\S]*?)\s*-->/)
        if (commentMatch) {
          const parsed = JSON.parse(commentMatch[1].trim())
          if (parsed && parsed.data) {
            sampleData = parsed.data
          }
        }
      } catch (e) {
        sampleData = null
      }
      const resolveField = (fieldName) => {
        const fromApi = this.resolveSampleValue(fieldName)
        if (fromApi !== null) return fromApi
        return sampleData && sampleData[fieldName]
      }
      const colorMap = {}
      this.labelColors.forEach((c, i) => { colorMap[i] = c })

      const getLabels = (container) => {
        const labels = []
        if (!container) return labels
        container.querySelectorAll('Label').forEach((el, i) => {
          const value = el.getAttribute('value') || ''
          const bg = el.getAttribute('background') || colorMap[i % this.labelColors.length]
          labels.push({ value, color: bg })
        })
        return labels
      }

      const getChoices = (container) => {
        const choices = []
        if (!container) return choices
        container.querySelectorAll('Choice').forEach(el => {
          choices.push(el.getAttribute('value') || '')
        })
        return choices
      }

      const imageEl = doc.querySelector('Image')
      if (imageEl) {
        result.type = 'image'
        result.allowZoom = imageEl.getAttribute('zoom') !== 'false'
        const rectLabels = doc.querySelector('RectangleLabels')
        const polyLabels = doc.querySelector('PolygonLabels')
        const brushLabels = doc.querySelector('BrushLabels')
        const labelsContainer = rectLabels || polyLabels || brushLabels
        if (labelsContainer) {
          const sw = labelsContainer.getAttribute('strokeWidth')
          if (sw) result.strokeWidth = parseInt(sw) || this.borderWidth
        }
        result.labels = getLabels(labelsContainer)
        return result
      }

      const pairwiseEl = doc.querySelector('Pairwise')
      if (pairwiseEl) {
        result.type = 'pairwise'
        const toName = pairwiseEl.getAttribute('toName') || ''
        const targetNames = toName.split(/\s+/).filter(Boolean)
        const allTextEls = Array.from(doc.querySelectorAll('Text'))
        const innerTextEls = Array.from(pairwiseEl.querySelectorAll('Text'))
        const textElsByTarget = {}
        allTextEls.forEach(el => {
          const name = el.getAttribute('name') || ''
          textElsByTarget[name] = el
        })
        const items = []
        targetNames.forEach((name, i) => {
          const el = textElsByTarget[name] || innerTextEls[i]
          if (el) {
            const value = el.getAttribute('value') || ''
            const fieldName = value.replace(/^\$/, '')
            const content = resolveField(fieldName) || `Sample: Text #${i + 1}`
            items.push({ name, variable: value, content })
          }
        })
        const promptText = allTextEls.find(el => {
          const name = el.getAttribute('name') || ''
          return !targetNames.includes(name) && el.parentNode !== pairwiseEl
        })
        let prompt = null
        if (promptText) {
          const value = promptText.getAttribute('value') || ''
          const fieldName = value.replace(/^\$/, '')
          const content = resolveField(fieldName) || this.getSampleText(value)
          prompt = { name: promptText.getAttribute('name') || 'prompt', variable: value, content }
        }
        const choicesContainer = pairwiseEl.querySelector('Choices')
        result.choices = getChoices(choicesContainer)
        const labelsContainer = doc.querySelector('Labels')
        result.labels = getLabels(labelsContainer)
        result.pairwise = { prompt, items }
        return result
      }

            const textEls = doc.querySelectorAll('Text')
      const htmlEls = doc.querySelectorAll('HyperText')
      if (htmlEls.length && !textEls.length) {
        result.type = 'hypertext'
        result.textFields = []
        htmlEls.forEach(el => {
          const value = el.getAttribute('value') || ''
          const fieldName = value.replace(/^\$/, '')
          const content = resolveField(fieldName) || this.getSampleHtml(value)
          if (!result.dataVariable) result.dataVariable = value
          result.textFields.push({ variable: value, name: fieldName, content, isHtml: true })
        })
        const labelsContainer = doc.querySelector('Labels')
        result.labels = getLabels(labelsContainer)
        const choicesContainer = doc.querySelector('Choices')
        result.choices = getChoices(choicesContainer)
        result.hypertext = { content: result.textFields[0] ? result.textFields[0].content : '' }
        return result
      }
      if (textEls.length || htmlEls.length) {
        result.type = 'text'
        result.textFields = []
        textEls.forEach(el => {
          const value = el.getAttribute('value') || ''
          const fieldName = value.replace(/^\$/, '')
          const content = resolveField(fieldName) || this.getSampleText(value)
          if (!result.dataVariable) result.dataVariable = value
          result.textFields.push({ variable: value, name: fieldName, content, isHtml: false })
        })
        htmlEls.forEach(el => {
          const value = el.getAttribute('value') || ''
          const fieldName = value.replace(/^\$/, '')
          const content = resolveField(fieldName) || this.getSampleHtml(value)
          if (!result.dataVariable) result.dataVariable = value
          result.textFields.push({ variable: value, name: fieldName, content, isHtml: true })
        })
        const choicesContainer = doc.querySelector('Choices')
        const labelsContainer = doc.querySelector('Labels')
        result.choices = getChoices(choicesContainer)
        result.labels = getLabels(labelsContainer)
        return result
      }

      const audioEl = doc.querySelector('Audio') || doc.querySelector('AudioPlus')
      if (audioEl) {
        result.type = 'audio'
        result.dataVariable = audioEl.getAttribute('value') || '$audio'
        const choicesContainer = doc.querySelector('Choices')
        result.choices = getChoices(choicesContainer)
        return result
      }

      // const htmlEl = doc.querySelector('HyperText')
      // if (htmlEl) {
      //   result.type = 'html'
      //   const value = htmlEl.getAttribute('value') || ''
      //   result.dataVariable = value
      //   const fieldName = value.replace(/^\$/, '')
      //   result.sampleHtml = (sampleData && sampleData[fieldName]) || this.getSampleHtml(value)
      //   return result
      // }

      const videoEl = doc.querySelector('Video')
      if (videoEl) {
        result.type = 'video'
        return result
      }

      const timerSeries = doc.querySelector('TimeSeries')
      if (timerSeries) {
        result.type = 'timeseries'
        result.dataVariable = timerSeries.getAttribute('value') || '$csv'
        const labelsContainer = doc.querySelector('TimeSeriesLabels')
        result.labels = getLabels(labelsContainer)
        const channels = []
        timerSeries.querySelectorAll('Channel').forEach(el => {
          channels.push({
            column: el.getAttribute('column') || '',
            legend: el.getAttribute('legend') || el.getAttribute('column') || '',
            units: el.getAttribute('units') || ''
          })
        })
        result.timeseries = { channels }
        return result
      }

      result.type = 'other'
      return result
    },
    previewImageSrc() {
      const xml = (this.liMode === 'visual' ? this.labelConfigCode : this.customConfig) || ''
      try {
        const doc = new DOMParser().parseFromString(xml, 'text/xml')
        const imageEl = doc.querySelector('Image')
        if (imageEl) {
          const value = imageEl.getAttribute('value') || ''
          const fieldName = value.replace(/^\$/, '')
          const fromApi = this.resolveSampleValue(fieldName)
          if (fromApi) {
            return this.resolveStaticUrl(fromApi)
          }
        }
      } catch (e) {}
      return 'https://a0ai.marscode.cn/api/ide/v1/text_to_image?prompt=aerial%20top%20down%20view%20of%20airport%20tarmac%20with%20multiple%20airplanes%20parked%2C%20concrete%20ground%2C%20high%20resolution%20satellite%20photo&image_size=landscape_4_3'
    },
    previewAudioSrc() {
      const xml = (this.liMode === 'visual' ? this.labelConfigCode : this.customConfig) || ''
      try {
        const doc = new DOMParser().parseFromString(xml, 'text/xml')
        const audioEl = doc.querySelector('Audio') || doc.querySelector('AudioPlus')
        if (audioEl) {
          const value = audioEl.getAttribute('value') || ''
          const fieldName = value.replace(/^\$/, '')
          const fromApi = this.resolveSampleValue(fieldName)
          if (fromApi) {
            return this.resolveStaticUrl(fromApi)
          }
        }
      } catch (e) {}
      const varName = (this.previewConfig.dataVariable || '$audio').replace(/^\$/, '')
      const samples = {
        audio: 'https://www.w3schools.com/html/horse.mp3',
        sound: 'https://www.w3schools.com/html/horse.mp3',
        voice: 'https://www.w3schools.com/html/horse.mp3',
        speech: 'https://www.w3schools.com/html/horse.mp3'
      }
      return samples[varName.toLowerCase()] || samples.audio
    },
    audioProgressPercent() {
      if (!this.audioDuration) return 0
      return (this.audioCurrentTime / this.audioDuration) * 100
    },
    audioPlayedBars() {
      return Math.floor((this.audioProgressPercent / 100) * 60)
    },
    tsChartData() {
      const count = 60
      const width = 100
      const height = 100
      const points = []
      for (let i = 0; i < count; i++) {
        const x = (i / (count - 1)) * width
        const t = i / count
        const y = 50
          + Math.sin(t * Math.PI * 6) * 18
          + Math.sin(t * Math.PI * 13) * 8
          + Math.cos(t * Math.PI * 4) * 6
          + (Math.random() - 0.5) * 4
        points.push({ x: x.toFixed(2), y: Math.max(8, Math.min(92, y)).toFixed(2) })
      }
      const path = points.map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x} ${p.y}`).join(' ')
      const areaPath = `${path} L ${width} ${height} L 0 ${height} Z`
      const yLabels = []
      for (let v = 2.0; v >= -2.0; v -= 0.5) {
        const yPct = 50 - (v / 2.5) * 42
        yLabels.push({ value: v.toFixed(1), y: yPct.toFixed(1) })
      }
      const xLabels = []
      const startTime = new Date(2020, 0, 1, 8, 0)
      for (let i = 0; i < count; i += 5) {
        const d = new Date(startTime.getTime() + i * 60000)
        const hh = String(d.getHours()).padStart(2, '0')
        const mm = String(d.getMinutes()).padStart(2, '0')
        xLabels.push({ x: (i / (count - 1) * width).toFixed(2), label: `${hh}:${mm}` })
      }
      const topLabels = []
      for (let i = 0; i <= 25; i++) {
        const x = (i / 25 * width).toFixed(2)
        topLabels.push({ x, label: String(i).padStart(2, '0') })
      }
      return { path, areaPath, points, yLabels, xLabels, topLabels }
    },
    currentDataType() {
      return this.getCurrentDataType()
    },
    currentDataTypeLabel() {
      const entry = this.dataTypeExtMap[this.currentDataType]
      return entry ? entry.label : this.currentDataType
    },
    currentAllowedExts() {
      return this.getAllowedExts(this.currentDataType)
    },
    currentAllowedExtsText() {
      return this.currentAllowedExts.join(', ')
    },
    labelConfigConfigured() {
      if (this.selectedTemplateObj) return true
      if (this.labels && this.labels.length) return true
      const config = (this.customConfig || '').trim()
      return config && config !== '<View></View>'
    }

  },
  watch: {
    labelConfigCode() {
      if (this.liMode === 'visual' && !this.isParsingVisual) {
        this.visualDirty = true
      }
      this.debounceFetchSampleTask()
    },
    customConfig() {
      if (this.liMode === 'code') {
        this.debounceFetchSampleTask()
      }
    },
    liMode() {
      this.debounceFetchSampleTask()
    },
    projects: {
      handler() {
        this.debounceFetchSampleTask()
      },
      deep: false
    }
  },
  methods: {
    switchMode(mode) {
      if (mode === 'code' && this.liMode === 'visual') {
        if (this.visualDirty) {
          this.customConfig = this.labelConfigCode
        }
        this.liMode = mode
      }
      if (mode === 'visual' && this.liMode === 'code') {
        this.isParsingVisual = true
        this.parseXmlToVisualForm()
        this.visualDirty = false
        this.liMode = mode
        this.$nextTick(() => {
          this.isParsingVisual = false
        })
      }
    },
    parseXmlToVisualForm() {
      const xml = (this.customConfig || '').trim()
      if (!xml || xml === '<View></View>') {
        this.labels = []
        this.dataType = 'image'
        this.imageVariable = '$image'
        return
      }
      let doc
      try {
        doc = new DOMParser().parseFromString(xml, 'text/xml')
      } catch (e) {
        return
      }
      if (!doc || !doc.documentElement || doc.querySelector('parsererror')) {
        return
      }

      const colorMap = {}
      this.labelColors.forEach((c, i) => { colorMap[i] = c })

      const imageEl = doc.querySelector('Image')
      const audioEl = doc.querySelector('Audio') || doc.querySelector('AudioPlus')
      const paragraphsEl = doc.querySelector('Paragraphs')
      const videoEl = doc.querySelector('Video')
      const pairwiseEl = doc.querySelector('Pairwise')
      const textEls = doc.querySelectorAll('Text')
      const hyperTextEls = doc.querySelectorAll('HyperText')
      const timeSeriesEl = doc.querySelector('TimeSeries')

      if (imageEl) {
        this.dataType = 'image'
        this.imageVariable = imageEl.getAttribute('value') || '$image'
        this.allowImageZoom = imageEl.getAttribute('zoom') !== 'false'
        this.showZoomControls = imageEl.getAttribute('zoomControls') === 'true'
        this.showRotateControls = imageEl.getAttribute('rotateControls') === 'true'
      } else if (audioEl) {
        this.dataType = 'audio'
        this.imageVariable = audioEl.getAttribute('value') || '$audio'
      } else if (paragraphsEl) {
        this.dataType = 'paragraphs'
        this.imageVariable = paragraphsEl.getAttribute('value') || '$paragraphs'
      } else if (videoEl) {
        this.dataType = 'video'
        this.imageVariable = videoEl.getAttribute('value') || '$video'
      } else if (timeSeriesEl) {
        this.dataType = 'timeseries'
        this.imageVariable = timeSeriesEl.getAttribute('value') || '$timeseries'
      } else if (pairwiseEl) {
        this.dataType = 'pairwise'
        const firstText = doc.querySelector('Text')
        this.imageVariable = firstText ? (firstText.getAttribute('value') || '$text') : '$text'
      } else if (textEls.length) {
        this.dataType = 'text'
        this.imageVariable = textEls[0].getAttribute('value') || '$text'
      } else if (hyperTextEls.length) {
        this.dataType = 'html'
        this.imageVariable = hyperTextEls[0].getAttribute('value') || '$html'
      } else {
        this.dataType = 'image'
        this.imageVariable = '$image'
      }

      const labelContainer = doc.querySelector('RectangleLabels, PolygonLabels, BrushLabels, KeyPointLabels, TimeSeriesLabels, Labels')
      const labels = []
      if (labelContainer) {
        const sw = labelContainer.getAttribute('strokeWidth')
        if (sw) this.borderWidth = parseInt(sw) || this.borderWidth
        labelContainer.querySelectorAll('Label').forEach((el, i) => {
          const value = el.getAttribute('value') || ''
          const bg = el.getAttribute('background') || colorMap[i % this.labelColors.length]
          if (value) labels.push({ name: value, color: bg })
        })
      }
      this.labels = labels
    },
    getSampleTaskProjectId() {
      if (this.projects && this.projects.length) {
        return this.projects[0].id
      }
      return null
    },
    debounceFetchSampleTask() {
      if (this._sampleTaskTimer) {
        clearTimeout(this._sampleTaskTimer)
      }
      this._sampleTaskTimer = setTimeout(() => {
        this.fetchSampleTask()
      }, 500)
    },
    async fetchSampleTask() {
      const xml = (this.liMode === 'visual' ? this.labelConfigCode : this.customConfig) || ''
      if (!xml.trim() || xml === '<View></View>') {
        this.sampleTaskData = {}
        return
      }
      const projectId = this.getSampleTaskProjectId()
      if (!projectId) {
        return
      }
      this.sampleTaskLoading = true
      try {
        const res = await generateSampleTask(projectId, xml)
        if (res && res.sample_task) {
          this.sampleTaskData = res.sample_task
        }
      } catch (e) {
        console.warn('[sample-task] 获取示例数据失败:', e)
      } finally {
        this.sampleTaskLoading = false
      }
    },
    resolveSampleValue(fieldName) {
      if (this.sampleTaskData && this.sampleTaskData[fieldName] !== undefined) {
        return this.sampleTaskData[fieldName]
      }
      return null
    },
    resolveStaticUrl(path) {
      if (!path) return path
      if (/^https?:\/\//i.test(path)) return path
      if (path.startsWith('/')) return `/labelstudio${path}`
      return `/labelstudio/${path}`
    },
    getSampleText(variable) {
      const name = (variable || '').replace(/^\$/, '').toLowerCase()
      const samples = {
        instruction: '请根据以下问题，给出详细且准确的回答。要求回答条理清晰，逻辑严谨，并尽可能提供相关的背景信息和实用建议。',
        input: '今天天气晴朗，气温适宜，非常适合户外活动。许多市民选择来到公园散步、野餐，享受美好的周末时光。',
        output: '这段文字主要描述了周末的好天气以及人们在公园进行户外活动的场景，整体基调积极向上。',
        question: '什么是机器学习？它与传统编程有什么区别？请举例说明。',
        answer: '机器学习是人工智能的一个分支，它使计算机能够从数据中学习规律，而不是通过显式编程。与传统编程不同，机器学习模型通过训练数据自动优化参数。',
        prompt: '请将以下英文句子翻译成中文，并保持原意和语气：The quick brown fox jumps over the lazy dog.',
        response: '那只敏捷的棕色狐狸跳过了那只懒惰的狗。',
        context: '在自然语言处理中，词向量是将词语映射为连续向量空间中的点的技术。常见的词向量模型包括 Word2Vec、GloVe 和 FastText 等。',
        sentence: '人工智能正在深刻地改变着我们的生活方式和工作模式。',
        text: '这是一段示例文本，用于展示当前标注配置的预览效果。在实际标注时，这里将显示待标注的数据内容。',
        dialogue: '用户：你好，请帮我查一下明天的天气。\n助手：您好！明天预计晴转多云，气温在 18 到 26 摄氏度之间，适合外出。',
        conversation: '用户：今天有什么新闻吗？\n助手：今日要闻包括科技、财经和体育等多个领域的最新动态。',
        content: '这是一段待标注的文本内容，请根据标注要求进行处理。',
        passage: '在一个遥远的小镇上，有一座古老的图书馆。图书馆里收藏着世界各地的珍贵书籍，每天都有许多读者前来阅读。',
        document: '本文件旨在说明项目的整体架构和技术选型，帮助团队成员快速了解项目背景和开发规范。',
        review: '这家餐厅的服务态度非常好，菜品味道也很不错，但是价格稍微有点贵，总体来说还是值得推荐的。',
        news: '据新华社报道，我国自主研发的新型动车组今日正式投入运营，标志着我国高铁技术又迈出了重要一步。',
        pairtext1: 'Sample: Text #1',
        pairtext2: 'Sample: Text #2',
        answer1: 'Reinforcement Learning from Human Feedback (RLHF) for dataset collection in Large Language Model (LLM) generation provides key benefits such as improved model performance through direct optimization, better alignment with human values by incorporating human feedback, and the ability to iteratively refine the model based on user interactions, resulting in a more user-friendly and efficient language model.',
        answer2: 'Using Reinforcement Learning from Human Feedback (RLHF) for dataset collection in Large Language Model (LLM) generation offers advantages such as enhanced model capabilities by optimizing for desired outcomes, greater adaptability to human preferences through the inclusion of human feedback, and the opportunity to continuously improve the model based on user experiences, ultimately leading to a more effective and responsive language model.'
      }
      return samples[name] || samples.text
    },
    getSampleHtml(variable) {
      const name = (variable || '').replace(/^\$/, '').toLowerCase()
      const samples = {
        html: '<div style="font-family: sans-serif; padding: 12px;"><h2 style="color:#3b82f6;">示例 HTML 内容</h2><p>这是一段示例 <strong>HTML</strong> 内容，包含<em>富文本</em>格式。</p><ul><li>列表项一</li><li>列表项二</li><li>列表项三</li></ul><p>以及一个<a href="#" style="color:#3b82f6;">链接</a>。</p></div>',
        content: '<article><h3>文章标题</h3><p>这是文章的正文内容，用于展示超文本标注的预览效果。</p><blockquote>引用部分内容</blockquote></article>',
        page: '<div class="page"><header><h1>页面标题</h1></header><main><p>页面主要内容区域</p></main><footer><p>页脚信息</p></footer></div>',
        body: '<div style="padding: 16px;"><h1>欢迎使用</h1><p>这是一个示例页面，用于展示 HTML 标注的预览效果。</p></div>',
        article: '<div style="font-family:Arial,sans-serif;padding:14px;border:1px solid #ddd;border-radius:6px;"><div style="text-align:center;padding:8px;background:#f3f4f6;border-radius:4px;"><strong>Jules:</strong> No no, Mr. Wolfe, it\'s not like that. Your help is definitely appreciated.</div><div style="margin-left:60px;padding:8px;background:#f3f4f6;border-radius:4px;margin-top:8px;"><strong>Vincent:</strong> Look, Mr. Wolfe, I respect you. I just don\'t like people barking orders at me, that\'s all.</div><div style="padding:10px;background:#dcfce7;border-radius:4px;margin-top:8px;"><strong>The Wolf:</strong> If I\'m curt with you, it\'s because time is a factor. I think fast, I talk fast, and I need you two guys to act fast if you want to get out of this. So pretty please, with sugar on top, clean the car.</div></div>'
      }
      return samples[name] || samples.article
    },
    progressColor(percent) {
      if (percent >= 80) return '#10b981'
      if (percent >= 50) return '#3b82f6'
      if (percent >= 30) return '#f59e0b'
      return '#ef4444'
    },
    handleCreate() {
      const namespace = (this.$store.state.project.currentProject && this.$store.state.project.currentProject.name) || ''
      this.projectForm.workspace = namespace
      this.projectForm.description = namespace
      this.createDialogVisible = true
    },
    handleSelectTemplate(tpl) {
      this.selectedTemplate = tpl.id
      this.selectedTemplateObj = tpl
      this.customConfig = (tpl && tpl.config) || '<View></View>'
      this.liMode = 'code'
      this.visualDirty = false
      this.isParsingVisual = false
      this.setupView = 'config'
    },
    handleSelectCustom() {
      this.selectedTemplate = null
      this.selectedTemplateObj = null
      this.customConfig = '<View></View>'
      this.labels = []
      this.dataType = 'image'
      this.imageVariable = '$image'
      this.liMode = 'visual'
      this.visualDirty = false
      this.isParsingVisual = false
      this.setupView = 'config'
    },
    backToTemplates() {
      this.setupView = 'templates'
    },
    handleCreateDialogClose(done) {
      this.$confirm('确定要关闭吗？未保存的内容将丢失。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        done()
      }).catch(() => {})
    },
    resetCreateForm() {
      this.activeCreateTab = 'name'
      this.activeCategory = 'cv'
      this.selectedTemplate = null
      this.selectedTemplateObj = null
      this.setupView = 'templates'
      this.isDragover = false
      this.liMode = 'code'
      this.customConfig = '<View></View>'
      this.visualDirty = false
      this.isParsingVisual = false
      this.showTemplates = false
      this.newQualityLabel = ''
      this.newSafetyLabel = ''
      this.selectedSafety = ''
            this.imageSource = 'manual'
      this.imageVariable = '$image'
      this.dataType = 'image'
      this.newLabelsInput = ''
      this.labels = []
      this.borderWidth = 3
      this.allowImageZoom = true
      this.showZoomControls = false
      this.showRotateControls = false
      this.previewImgError = false
      this.resetAudioPlayer()
      this.dataFields = [
        { variable: '$instruction', source: 'manual' },
        { variable: '$input', source: 'manual' },
        { variable: '$output', source: 'manual' }
      ]
      this.qualityLabels = []
      this.safetyLabels = ['安全', '存在风险']
      this.qualityItems = [
        { name: '指令质量', value: 0 },
        { name: '回答正确性', value: 0 },
        { name: '回答相关性', value: 0 },
        { name: '回答完整性', value: 0 },
        { name: '回答流畅性', value: 0 }
      ]
      this.projectForm = {
        name: '',
        description: '',
        workspace: '',
        datasetUrl: '',
        urls: []
      }
      this.selectedFiles = []
      this.uploadLoading = false
    },
    nextStep() {
      if (this.activeCreateTab === 'name' && !this.projectForm.name.trim()) {
        this.$message.warning('请输入项目名称')
        return
      }
      const steps = ['name', 'import', 'setup']
      const idx = steps.indexOf(this.activeCreateTab)
      if (idx < steps.length - 1) {
        this.activeCreateTab = steps[idx + 1]
      }
    },
    prevStep() {
      const steps = ['name', 'import', 'setup']
      const idx = steps.indexOf(this.activeCreateTab)
      if (idx > 0) {
        this.activeCreateTab = steps[idx - 1]
      }
    },
    handleAddUrl() {
      const url = this.projectForm.datasetUrl.trim()
      if (!url) {
        this.$message.warning('请输入 Dataset URL')
        return
      }
      this.projectForm.urls.push(url)
      this.projectForm.datasetUrl = ''
    },
    removeUrl(idx) {
      this.projectForm.urls.splice(idx, 1)
    },
    triggerFileUpload() {
      this.$refs.fileInput && this.$refs.fileInput.click()
    },
    handleFileChange(e) {
      const files = e.target.files
      if (files && files.length) {
        this.addFiles(files)
      }
      e.target.value = ''
    },
    handleDrop(e) {
      this.isDragover = false
      const files = e.dataTransfer.files
      if (files && files.length) {
        this.addFiles(files)
      }
    },
    addFiles(fileList) {
      const files = Array.from(fileList)
      files.forEach(file => {
        if (!this.selectedFiles.find(f => f.file.name === file.name && f.file.size === file.size)) {
          const ext = this.getFileExt(file.name)
          this.selectedFiles.push({ file, ext })
        }
      })
      this.$message.success(`已选择 ${this.selectedFiles.length} 个文件`)
    },
    removeSelectedFile(idx) {
      this.selectedFiles.splice(idx, 1)
    },
    getFileExt(name) {
      const idx = name.lastIndexOf('.')
      return idx >= 0 ? name.substring(idx + 1).toLowerCase() : ''
    },
    detectDataTypeFromXml(xml) {
      if (!xml || xml === '<View></View>') return 'image'
      let doc
      try {
        doc = new DOMParser().parseFromString(xml, 'text/xml')
      } catch (e) {
        return 'image'
      }
      if (!doc || !doc.documentElement || doc.querySelector('parsererror')) return 'image'
      if (doc.querySelector('Image')) return 'image'
      if (doc.querySelector('Audio') || doc.querySelector('AudioPlus')) return 'audio'
      if (doc.querySelector('Paragraphs')) return 'paragraphs'
      if (doc.querySelector('Video')) return 'video'
      if (doc.querySelector('TimeSeries')) return 'timeseries'
      if (doc.querySelector('Pairwise')) return 'pairwise'
      if (doc.querySelectorAll('HyperText').length) return 'html'
      if (doc.querySelectorAll('Text').length) return 'text'
      return 'image'
    },
    getCurrentDataType() {
      if (this.liMode === 'visual') {
        return this.dataType || 'image'
      }
      return this.detectDataTypeFromXml(this.customConfig)
    },
    getAllowedExts(dataType) {
      const entry = this.dataTypeExtMap[dataType]
      return entry ? entry.exts : []
    },
    formatFileSize(bytes) {
      if (bytes < 1024) return bytes + ' B'
      if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
      return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
    },
    addLabels() {
      const text = (this.newLabelsInput || '').trim()
      if (!text) {
        this.$message.warning('请输入标签名称')
        return
      }
      const names = text.split('\n').map(l => l.trim()).filter(Boolean)
      names.forEach(name => {
        if (!this.labels.find(l => l.name === name)) {
          const color = this.labelColors[this.labels.length % this.labelColors.length]
          this.labels.push({ name, color })
        }
      })
      this.newLabelsInput = ''
    },
    removeLabel(idx) {
      this.labels.splice(idx, 1)
    },
    syncPreviewBoxes() {
      if (!this.labels.length) {
        this.previewBoxes = []
        return
      }
      const positions = [
        { left: 5, top: 12, width: 38, height: 28 },
        { left: 52, top: 18, width: 40, height: 30 },
        { left: 25, top: 58, width: 42, height: 32 },
        { left: 70, top: 60, width: 22, height: 14 },
        { left: 12, top: 72, width: 18, height: 12 }
      ]
      this.previewBoxes = this.labels.map((label, idx) => {
        const pos = positions[idx % positions.length]
        return { ...pos, label: label.name, color: label.color }
      })
    },
    handlePreviewImgError() {
      this.previewImgError = true
    },
    resetAudioPlayer() {
      this.audioPlaying = false
      this.audioCurrentTime = 0
      this.audioDuration = 0
      const audioEl = this.$refs.previewAudio
      if (audioEl) {
        audioEl.pause()
        audioEl.currentTime = 0
      }
    },
    toggleAudioPlay() {
      const audioEl = this.$refs.previewAudio
      if (!audioEl) return
      if (this.audioPlaying) {
        audioEl.pause()
        this.audioPlaying = false
      } else {
        audioEl.play().then(() => {
          this.audioPlaying = true
        }).catch(() => {
          this.audioPlaying = false
        })
      }
    },
    onAudioTimeUpdate() {
      const audioEl = this.$refs.previewAudio
      if (audioEl) {
        this.audioCurrentTime = audioEl.currentTime
      }
    },
    onAudioLoadedMetadata() {
      const audioEl = this.$refs.previewAudio
      if (audioEl) {
        this.audioDuration = audioEl.duration || 0
      }
    },
    onAudioEnded() {
      this.audioPlaying = false
      this.audioCurrentTime = 0
    },
    seekAudio(e) {
      const audioEl = this.$refs.previewAudio
      if (!audioEl || !this.audioDuration) return
      const rect = e.currentTarget.getBoundingClientRect()
      const percent = (e.clientX - rect.left) / rect.width
      audioEl.currentTime = percent * this.audioDuration
      this.audioCurrentTime = audioEl.currentTime
    },
    formatAudioTime(seconds) {
      if (!seconds || isNaN(seconds)) return '00:00'
      const mins = Math.floor(seconds / 60)
      const secs = Math.floor(seconds % 60)
      const pad = n => (n < 10 ? '0' + n : '' + n)
      return pad(mins) + ':' + pad(secs)
    },
    getWaveBarHeight(n) {
      const base = 8
      const variation = Math.sin(n * 0.7) * 12 + Math.cos(n * 0.3) * 8 + Math.sin(n * 1.3) * 6
      return Math.max(4, base + variation)
    },
    addQualityLabel() {
      const text = this.newQualityLabel.trim()
      if (!text) return
      const lines = text.split('\n').map(l => l.trim()).filter(Boolean)
      this.qualityLabels.push(...lines)
      this.newQualityLabel = ''
    },
    removeQualityLabel(idx) {
      this.qualityLabels.splice(idx, 1)
    },
    addSafetyLabel() {
      const text = this.newSafetyLabel.trim()
      if (!text) return
      const lines = text.split('\n').map(l => l.trim()).filter(Boolean)
      this.safetyLabels.push(...lines)
      this.newSafetyLabel = ''
    },
    removeSafetyLabel(idx) {
      this.safetyLabels.splice(idx, 1)
    },
    async handleSaveProject() {
      if (!this.projectForm.name.trim()) {
        this.$message.warning('请输入项目名称')
        this.activeCreateTab = 'name'
        return
      }

      if (this.selectedFiles.length) {
        const currentType = this.getCurrentDataType()
        const allowedExts = this.getAllowedExts(currentType)
        const typeLabel = (this.dataTypeExtMap[currentType] && this.dataTypeExtMap[currentType].label) || currentType
        const mismatchFiles = this.selectedFiles.filter(f => !allowedExts.includes(f.ext))
        if (mismatchFiles.length && allowedExts.length) {
          const allowedStr = allowedExts.join(', ')
          const mismatchNames = mismatchFiles.map(f => f.file.name).join('、')
          try {
            await this.$confirm(
              `当前标注设置类型为「${typeLabel}」，允许的文件格式为：${allowedStr}。\n以下 ${mismatchFiles.length} 个文件类型不匹配，上传后可能无法创建标注任务：\n${mismatchNames}\n\n是否仍要继续上传？`,
              '文件类型不匹配',
              { confirmButtonText: '继续上传', cancelButtonText: '取消', type: 'warning' }
            )
          } catch (e) {
            return
          }
        }
      }

      const labelConfig = this.liMode === 'visual'
        ? this.labelConfigCode
        : (this.customConfig || '<View></View>')

      this.uploadLoading = true
      try {
        const project = await createProject({
          title: this.projectForm.name,
          description: this.projectForm.description || ''
        })
        const projectId = project.id
        console.log('[标注项目] 创建成功, projectId =', projectId)

        await setProjectLabelConfig(projectId, labelConfig)
        console.log('[标注项目] 标注配置已设置')

        if (this.selectedFiles.length) {
          console.log('[标注项目] 开始上传文件, 数量 =', this.selectedFiles.length)
          const filesToUpload = this.selectedFiles.map(f => f.file)
          try {
            const uploadRes = await uploadDatasetFiles(projectId, filesToUpload)
            console.log('[标注项目] 文件上传响应:', uploadRes)
            this.handleUploadResponse(uploadRes, this.selectedFiles.length)
          } catch (uploadErr) {
            console.error('[标注项目] 文件上传失败:', uploadErr)
            const detail = this.extractErrorMsg(uploadErr)
            this.$message.error(`项目已创建，但文件上传失败：${detail}`)
          }
        }

        if (this.projectForm.urls.length) {
          try {
            const urlRes = await uploadDatasetByUrl(projectId, this.projectForm.urls)
            console.log('[标注项目] URL 导入响应:', urlRes)
            this.handleUploadResponse(urlRes, this.projectForm.urls.length, 'URL')
          } catch (urlErr) {
            console.error('[标注项目] URL 导入失败:', urlErr)
            const detail = this.extractErrorMsg(urlErr)
            this.$message.warning(`URL 导入失败：${detail}`)
          }
        }

        this.$message.success('项目创建成功')
        this.createDialogVisible = false
        this.resetCreateForm()
        this.loadDashboard()
      } catch (err) {
        console.error('[标注项目] 创建项目失败:', err)
        const msg = this.extractErrorMsg(err) || '创建项目失败'
        this.$message.error(msg)
      } finally {
        this.uploadLoading = false
      }
    },
    extractErrorMsg(err) {
      if (!err) return ''
      if (err.response && err.response.data) {
        const data = err.response.data
        return data.detail || data.message || data.error || (typeof data === 'string' ? data : '')
      }
      return err.message || ''
    },
    handleUploadResponse(res, totalCount, type = '文件') {
      if (!res) {
        this.$message.warning(`${type}上传无响应`)
        return
      }
      const taskCount = res.task_count || 0
      const successCount = res.success_count || 0
      const failedCount = res.failed_count || 0
      const results = res.results || []

      if (taskCount > 0) {
        this.$message.success(`成功创建 ${taskCount} 条标注任务`)
      } else if (successCount > 0) {
        this.$message.success(`成功上传 ${successCount} 个${type}`)
      }

      if (failedCount > 0) {
        const failedItems = results.filter(r => !r.success)
        const firstError = failedItems[0] && failedItems[0].error
        let errMsg = ''
        if (firstError) {
          try {
            const parsed = JSON.parse(firstError)
            if (parsed.validation_errors && parsed.validation_errors.non_field_errors) {
              errMsg = parsed.validation_errors.non_field_errors[0]
            } else if (parsed.detail) {
              errMsg = parsed.detail
            }
          } catch (e) {
            errMsg = firstError
          }
        }
        const hint = errMsg ? `：${errMsg}` : ''
        this.$message.warning(`${failedCount} 个${type}导入失败${hint}`)
      }
    },
    copyCode() {
      const textarea = document.createElement('textarea')
      textarea.value = this.labelConfigCode
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      try {
        document.execCommand('copy')
        this.$message.success('代码已复制到剪贴板')
      } catch (e) {
        this.$message.error('复制失败')
      }
      document.body.removeChild(textarea)
    },
    downloadCode() {
      const blob = new Blob([this.labelConfigCode], { type: 'text/xml' })
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = 'label_config.xml'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)
    },
    handleOpen(project) {
      this.$router.push({
        path: `/llmops/datasets/annotation/projects/${project.id}/tasks`
      })
    },
    handleCommand(command, project) {
      if (command === 'settings') {
        this.$router.push({
          path: `/llmops/datasets/annotation/projects/${project.id}/settings`
        })
      } else if (command === 'label') {
        this.handleOpen(project)
      }
    },
    async loadTemplates() {
      try {
        const res = await getTemplates()
        const list = (res && res.templates) || []
        this.templates = list.map((tpl, idx) => this.mapApiTemplateToUi(tpl, idx))
      } catch (err) {
        console.error('获取标注模板数据失败:', err)
        this.templates = []
      }
    },
    handleImgError(tpl) {
      if (tpl) {
        tpl.image = ''
      }
    },
    mapApiTemplateToUi(tpl, idx) {
      const categoryMap = {
        'Computer Vision': 'cv',
        'Natural Language Processing': 'nlp',
        'Audio/Speech Processing': 'audio',
        'Conversational AI': 'conversational',
        'Chat': 'chat',
        'Ranking & Scoring': 'ranking',
        'Structured Data Parsing': 'structured',
        'Time Series Analysis': 'timeseries',
        'Videos': 'videos',
        'Generative AI': 'generative',
        'Community Contributions': 'community'
      }
      const gradients = [
        'linear-gradient(135deg,#dbeafe,#bfdbfe)',
        'linear-gradient(135deg,#fce7f3,#fbcfe8)',
        'linear-gradient(135deg,#ede9fe,#ddd6fe)',
        'linear-gradient(135deg,#d1fae5,#a7f3d0)',
        'linear-gradient(135deg,#e0f2fe,#bae6fd)',
        'linear-gradient(135deg,#fef3c7,#fde68a)'
      ]
      return {
        id: `tpl-${idx}`,
        category: categoryMap[tpl.group] || 'community',
        name: tpl.title,
        icon: 'el-icon-picture-outline',
        bg: gradients[idx % gradients.length],
        image: tpl.image ? tpl.image.replace('/static/templates/', '/ls-templates/') : '',
        config: tpl.config || '',
        details: tpl.details || '',
        order: tpl.order
      }
    },
    async loadDashboard() {
      this.loading = true
      try {
        const namespace = (this.$store.state.project.currentProject && this.$store.state.project.currentProject.name) || ''
        const res = await getProjects({
          page: 1,
          page_size: 20
        })
        const allItems = (res && res.items) || []
        const results = namespace ? allItems.filter(p => (p.description || '') === namespace) : allItems
        let totalTasks = 0
        let totalAnnotations = 0
        let totalInProgress = 0
        let totalAnnotators = 0
        this.projects = results.map(p => {
          const taskNumber = p.task_number || 0
          const finishedTaskNumber = p.finished_task_number || 0
          const inProgress = Math.max(taskNumber - finishedTaskNumber, 0)
          const progress = taskNumber > 0 ? Math.round(finishedTaskNumber / taskNumber * 100) : 0
          totalTasks += taskNumber
          totalAnnotations += (p.total_annotations_number || 0)
          totalInProgress += inProgress
          totalAnnotators += (p.members_count || 0)
          return this.mapApiProjectToUi({
            ...p,
            in_progress: inProgress,
            progress
          })
        })
        this.stats = {
          total: this.projects.length,
          completed: totalAnnotations,
          inProgress: totalInProgress,
          annotators: totalAnnotators
        }
      } catch (err) {
        console.error('获取标注项目数据失败:', err)
        this.stats = { total: 0, completed: 0, inProgress: 0, annotators: 0 }
        this.projects = []
        this.$message.error('获取标注项目数据失败')
      } finally {
        this.loading = false
      }
    },
    mapApiProjectToUi(p) {
      const title = p.title || `Project #${p.id}`
      return {
        ...p,
        id: p.id,
        name: title,
        subtitle: p.workspace_title || p.workspace || '/',
        completed: p.finished_task_number || 0,
        total: p.task_number || 0,
        skipped: p.skipped_annotations_number || 0,
        predicted: p.total_predictions_number || 0,
        progress: p.progress || 0,
        date: this.formatDate(p.created_at),
        avatar: this.getInitials(title),
        avatarColor: this.getAvatarColor(p.id)
      }
    },
    formatDate(value) {
      if (!value) return '-'
      const date = new Date(value)
      if (isNaN(date.getTime())) return '-'
      const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
      const pad = n => (n < 10 ? `0${n}` : `${n}`)
      return `${date.getDate()} ${months[date.getMonth()]} ${date.getFullYear()}, ${pad(date.getHours())}:${pad(date.getMinutes())}`
    },
    getInitials(title) {
      if (!title) return '-'
      const parts = title.split(/[\s\-_]+/).filter(Boolean)
      if (parts.length >= 2) {
        return (parts[0][0] + parts[1][0]).toUpperCase()
      }
      return title.substr(0, 2).toUpperCase()
    },
    getAvatarColor(id) {
      const colors = ['#b89fb8', '#9b8fb8', '#8fb8a8', '#b8a88f', '#8f9bb8', '#b88f9b']
      const index = Math.abs(Number(id) || 0) % colors.length
      return colors[index]
    }
  },
  created() {
    this.loadDashboard()
    this.loadTemplates()
  }
}
</script>

<style lang="scss" scoped>
.annotation-container {
  min-height: calc(100vh - 60px);
}

.annotation-content {
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 20px;
  font-weight: bold;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.create-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}

.view-btn {
  padding: 8px 10px;
  color: #6b7280;
  border: 1px solid #e5e7eb;
  background: #fff;

  &.active {
    color: #2563eb;
    border-color: #bfdbfe;
    background: #eff6ff;
  }

  &:hover {
    color: #2563eb;
  }
}

/* 统计卡片 */
.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.metric-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  border: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    transform: translateY(-2px);
  }

  .metric-main {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  .metric-label {
    font-size: 13px;
    color: #6b7280;
  }

  strong {
    font-size: 28px;
    font-weight: 700;
    color: #1f2937;
    line-height: 1.2;
  }

  .metric-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    color: #fff;
  }

  &.blue .metric-icon {
    background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  }

  &.green .metric-icon {
    background: linear-gradient(135deg, #10b981, #059669);
  }

  &.orange .metric-icon {
    background: linear-gradient(135deg, #f59e0b, #d97706);
  }

  &.purple .metric-icon {
    background: linear-gradient(135deg, #8b5cf6, #6d28d9);
  }
}

/* 项目网格 */
.project-section {
  margin-bottom: 24px;
}

.project-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.project-empty {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 48px 24px;
  text-align: center;

  i {
    font-size: 40px;
    color: #d1d5db;
  }

  p {
    font-size: 14px;
    color: #6b7280;
    margin: 12px 0 16px;
  }
}

.project-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  border: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
    border-color: #bfdbfe;
  }
}

.card-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.project-title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.project-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  cursor: pointer;
  transition: color 0.2s;

  &:hover {
    color: #2563eb;
  }
}

.project-subtitle {
  font-size: 13px;
  color: #9ca3af;
}

.card-count-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.card-count {
  font-size: 14px;
  color: #374151;
  font-weight: 500;
  white-space: nowrap;

  .count-completed {
    color: #1f2937;
  }

  .count-sep {
    color: #9ca3af;
    margin: 0 2px;
  }

  .count-total {
    color: #9ca3af;
  }
}

.card-more {
  font-size: 18px;
  color: #9ca3af;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s;

  &:hover {
    color: #6b7280;
    background: #f3f4f6;
  }
}

.card-stats {
  display: flex;
  gap: 16px;
  // padding: 8px 0;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.stat-icon {
  font-size: 14px;

  &.completed {
    color: #10b981;
  }

  &.skipped {
    color: #ef4444;
  }

  &.predicted {
    color: #6366f1;
  }
}

.stat-value {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.card-progress {
  padding: 0;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f3f4f6;
}

.project-date {
  font-size: 12px;
  color: #9ca3af;
  display: flex;
  align-items: center;
  gap: 4px;

  i {
    font-size: 13px;
  }
}

.project-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  flex-shrink: 0;
}

/* 列表视图 */
.table-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  border: 1px solid #e5e7eb;
}

.link-text {
  color: #2563eb;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

.status-completed {
  color: #10b981;
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-skipped {
  color: #ef4444;
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-predicted {
  color: #6366f1;
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-hover {
  transition: all 0.3s ease;
}

.card-hover:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

/* 创建项目弹框样式 */
.create-project-dialog {
  ::v-deep .el-dialog {
    border-radius: 16px;
    overflow: hidden;
  }

  ::v-deep .el-dialog__header {
    padding: 20px 24px 0;
    margin-right: 0;
    padding-bottom: 16px;
    border-bottom: 1px solid #e5e7eb;
  }

  ::v-deep .el-dialog__title {
    font-size: 20px;
    font-weight: 700;
    color: #1f2937;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
  }

  ::v-deep .el-dialog__headerbtn {
    top: 24px;
  }

  .dialog-title-slot {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .dialog-title-text {
    font-size: 20px;
    font-weight: 700;
    color: #1f2937;
  }

  .dialog-title-actions {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .header-cancel-btn {
    color: #6b7280;
    border-color: #e5e7eb;
    background: #fff;

    &:hover {
      color: #1f2937;
      border-color: #d1d5db;
      background: #f9fafb;
    }
  }

  .header-save-btn {
    background: #2563eb;
    border-color: #2563eb;

    &:hover,
    &:focus {
      background: #1d4ed8;
      border-color: #1d4ed8;
    }
  }

  ::v-deep .el-dialog__body {
    padding: 20px 24px 0;
  }

  ::v-deep .el-dialog__footer {
    padding: 16px 24px 20px;
    border-top: 1px solid #e5e7eb;
  }
}

.create-project-body {
  min-height: 380px;
}

/* 步骤标签页 - 胶囊风格 */
.create-project-tabs {
  ::v-deep .el-tabs__header {
    margin-bottom: 20px;
  }

  ::v-deep .el-tabs__nav-wrap::after {
    display: none;
  }

  ::v-deep .el-tabs__nav {
    background: #f3f4f6;
    border-radius: 10px;
    padding: 4px;
  }

  ::v-deep .el-tabs__item {
    padding: 0 24px !important;
    font-size: 14px;
    font-weight: 500;
    color: #6b7280;
    border: 1px solid transparent;
    border-radius: 8px;
    height: 34px;
    line-height: 32px;
    margin-right: 0;
  }

  ::v-deep .el-tabs__item:hover {
    color: #2563eb;
  }

  ::v-deep .el-tabs__item.is-active {
    color: #1f2937;
    background-color: #fff;
    border-color: #e5e7eb;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  }

  ::v-deep .el-tabs__active-bar {
    display: none;
  }
}

.tab-step-content {
  min-height: 300px;
}

/* 项目名称表单 */
.project-form {
  max-width: 520px;
  margin: 0 auto;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

.required {
  color: #ef4444;
}

.form-hint {
  font-size: 12px;
  color: #6b7280;
  margin-top: 6px;
}

::v-deep .project-form .el-form-item {
  margin-bottom: 22px;
}

/* 数据导入 */
.import-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.url-input-group {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.url-input {
  flex: 1;
}

.or-text {
  color: #6b7280;
  font-size: 13px;
}

.url-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
}

.url-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 13px;
  color: #374151;
}

.url-text {
  flex: 1;
  word-break: break-all;
}

.url-remove {
  cursor: pointer;
  color: #9ca3af;

  &:hover {
    color: #ef4444;
  }
}

.upload-dropzone {
  border: 2px dashed #d1d5db;
  border-radius: 12px;
  background: #f9fafb;
  padding: 32px 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover,
  &.is-dragover {
    border-color: #3b82f6;
    background: #eff6ff;
  }
}

.dropzone-icon {
  font-size: 40px;
  color: #3b82f6;
  margin-bottom: 12px;

  i {
    font-size: 40px;
  }
}

.dropzone-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.dropzone-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin-top: 4px;
  margin-bottom: 24px;
}

.file-types {
  display: inline-block;
  text-align: left;
  margin-bottom: 24px;
}

.file-type-row {
  display: flex;
  gap: 24px;
  padding: 2px 0;
  font-size: 13px;
}

.file-type-label {
  color: #374151;
  font-weight: 500;
  min-width: 110px;
}

.file-type-exts {
  color: #6b7280;
}

.dropzone-notes {
  text-align: left;
  font-size: 12px;
  color: #6b7280;
  border-top: 1px solid #e5e7eb;
  padding-top: 16px;
}

.notes-title {
  font-weight: 600;
  color: #374151;
  margin-bottom: 6px;
}

.notes-list {
  margin: 0;
  padding-left: 18px;

  li {
    margin: 3px 0;
  }
}

.selected-files-list {
  margin-top: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  overflow: hidden;
}

.selected-files-title {
  padding: 10px 16px;
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}

.selected-files-items {
  max-height: 200px;
  overflow-y: auto;
}

.selected-file-item {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  border-bottom: 1px solid #f3f4f6;
  font-size: 13px;

  &:last-child {
    border-bottom: none;
  }

  i.el-icon-document {
    color: #3b82f6;
    margin-right: 8px;
    font-size: 16px;
  }

  .selected-file-name {
    flex: 1;
    color: #1f2937;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    margin-right: 12px;
  }

  .selected-file-size {
    color: #9ca3af;
    font-size: 12px;
    margin-right: 12px;
  }

  .selected-file-remove {
    color: #9ca3af;
    cursor: pointer;
    font-size: 14px;

    &:hover {
      color: #ef4444;
    }
  }
}

/* 标注设置 - Labeling Interface */
.labeling-interface {
  display: flex;
  gap: 0;
  min-height: 460px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}

/* 左侧配置面板 */
.li-config-panel {
  width: 340px;
  flex-shrink: 0;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  background: #fafafa;
}

.li-header {
  padding: 16px;
  border-bottom: 1px solid #e5e7eb;
  background: #fff;
}

.li-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 12px 0;
}

.li-header-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.browse-templates-btn {
  border-color: #2563eb;
  color: #2563eb;
  background: #fff;

  &:hover {
    background: #eff6ff;
  }
}

.code-visual-toggle {
  display: flex;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  overflow: hidden;

  span {
    padding: 6px 14px;
    font-size: 13px;
    color: #6b7280;
    cursor: pointer;
    background: #fff;
    transition: all 0.2s;

    &.active {
      background: #f3f4f6;
      color: #1f2937;
      font-weight: 600;
    }

    &:hover:not(.active) {
      background: #f9fafb;
    }
  }
}

.templates-browse {
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
  background: #fff;
  max-height: 200px;
  overflow-y: auto;
}

.tb-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.tb-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;

  &:hover {
    border-color: #bfdbfe;
  }

  &.selected {
    border-color: #2563eb;
    box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.15);
  }
}

.tb-preview {
  height: 60px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 6px;

  i {
    font-size: 24px;
    color: #6b7280;
    opacity: 0.7;
  }
}

.tb-name {
  font-size: 12px;
  color: #374151;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.li-section {
  padding: 14px 16px;
  border-bottom: 1px solid #e5e7eb;
}

.li-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.li-section-desc {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 8px;
}

.data-field-row {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 10px 12px;
  margin-bottom: 8px;
}

.field-variable {
  font-size: 13px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 6px;
  font-family: monospace;
}

.field-control {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.field-label {
  font-size: 12px;
  color: #6b7280;
}

.field-select {
  width: 100%;
}

.choices-textarea {
  margin-bottom: 8px;
}

::v-deep .choices-textarea .el-textarea__inner {
  font-size: 13px;
}

.add-btn {
  margin-bottom: 4px;
}

.choices-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.choice-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 12px;
  background: #f3f4f6;
  border-radius: 6px;
  font-size: 13px;
  color: #374151;
}

.choice-text {
  flex: 1;
}

.choice-remove {
  cursor: pointer;
  color: #9ca3af;
  font-size: 12px;

  &:hover {
    color: #ef4444;
  }
}

.li-save-row {
  padding: 14px 16px;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #e5e7eb;
  background: #fff;
  margin-top: auto;
}

.li-save-btn {
  min-width: 100px;
}

/* 中间预览面板 */
.li-preview-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  min-width: 0;
}

.preview-header {
  padding: 14px 16px;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  border-bottom: 1px solid #e5e7eb;
  background: #fff;
}

.preview-body {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.preview-block {
  margin-bottom: 20px;
}

.preview-block-title {
  font-size: 15px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 6px;
}

.preview-sample {
  font-size: 13px;
  color: #6b7280;
  padding: 10px 12px;
  background: #f9fafb;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.preview-divider {
  text-align: center;
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
  margin: 24px 0 20px;
}

.quality-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.quality-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.quality-index {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

.star-rating {
  display: flex;
  gap: 4px;

  .star {
    font-size: 22px;
    color: #d1d5db;
    cursor: pointer;
    transition: color 0.15s;

    &:hover {
      color: #f59e0b;
    }

    &.active {
      color: #f59e0b;
    }
  }
}

.safety-section {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.safety-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 10px;
}

.safety-choices {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.safety-choice {
  padding: 6px 16px;
  border: 1px solid #d1d5db;
  border-radius: 20px;
  font-size: 13px;
  color: #374151;
  cursor: pointer;
  background: #fff;
  transition: all 0.2s;

  &:hover {
    border-color: #93c5fd;
    color: #2563eb;
  }

  &.selected {
    background: #2563eb;
    border-color: #2563eb;
    color: #fff;
  }
}

/* 右侧：代码预览面板 */
.li-code-panel {
  width: 320px;
  flex-shrink: 0;
  border-left: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  background: #f7f8fa;
}

.code-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
  background: #fff;
}

.code-panel-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.code-panel-actions {
  display: flex;
  gap: 14px;

  i {
    font-size: 16px;
    color: #6b7280;
    cursor: pointer;
    transition: color 0.2s;

    &:hover {
      color: #2563eb;
    }
  }
}

.code-body {
  flex: 1;
  overflow: auto;
  padding: 16px;
}

.code-content {
  margin: 0;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 12px;
  line-height: 1.7;
  color: #374151;
  white-space: pre;
  word-break: break-all;
}

@media (max-width: 1200px) {
  .metric-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .metric-grid {
    grid-template-columns: 1fr;
  }

  .project-grid {
    grid-template-columns: 1fr;
  }
}

.template-selector {
  display: flex;
  height: 600px;
}
.template-sidebar {
  width: 240px;
  border-right: 1px solid #ebeef5;
  overflow-y: auto;
  padding: 12px 0;
}
.template-cat-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
}
.template-cat-item:hover { background: #f5f7fa; }
.template-cat-item.active {
  background: #ecf5ff;
  color: #409eff;
  font-weight: 600;
}
.template-main {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}
.template-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}
.template-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
}
.template-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64,158,255,0.2);
  transform: translateY(-2px);
}
.template-preview {
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: #909399;
  overflow: hidden;
}
.template-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.template-name {
  padding: 12px;
  text-align: center;
  font-size: 14px;
  font-weight: 500;
  border-top: 1px solid #ebeef5;
}
.custom-template-card {
  border-style: dashed;
  border-color: #d1d5db;
}
.custom-template-card:hover {
  border-color: #2563eb;
  border-style: solid;
}
.custom-preview {
  background: linear-gradient(135deg, #f3f4f6, #e5e7eb);
  color: #6b7280;
}
.custom-preview i {
  font-size: 48px;
}

/* 标注接口 - 代码编辑器 */
.li-code-editor {
  flex: 1;
  padding: 12px 16px 16px;
  display: flex;
}
.li-code-editor .code-textarea {
  flex: 1;
}
::v-deep .code-textarea .el-textarea__inner {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 13px;
  line-height: 1.6;
  color: #1f2937;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 12px;
  resize: none;
  height: 100%;
}
/* Configure data */
.data-config-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.data-config-label {
  font-size: 13px;
  color: #374151;
  white-space: nowrap;
}
.data-config-select {
  width: 110px;
  flex-shrink: 0;
}
.data-config-input {
  flex: 1;
}

/* Add label names */
.labels-textarea {
  margin-bottom: 8px;
}
::v-deep .labels-textarea .el-textarea__inner {
  font-size: 13px;
  resize: vertical;
}
.add-labels-btn {
  margin-bottom: 4px;
}

/* Labels list */
.labels-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.label-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 12px 6px 10px;
  border-radius: 6px;
  border-left: 4px solid;
  font-size: 13px;
  color: #374151;
}
.label-item-name {
  font-weight: 500;
}
.label-item-remove {
  cursor: pointer;
  color: #9ca3af;
  font-size: 12px;
  &:hover { color: #ef4444; }
}

/* Configure settings */
.setting-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
.setting-label {
  font-size: 13px;
  color: #374151;
  white-space: nowrap;
}
.setting-input {
  width: 80px;
}
.setting-checkbox-row {
  margin-bottom: 8px;
  &:last-child { margin-bottom: 0; }
}
::v-deep .setting-checkbox-row .el-checkbox__label {
  font-size: 13px;
  color: #374151;
}
.li-visual-editor {
  flex: 1;
  overflow-y: auto;
}

/* 预览区域 */
/* 预览区域 */
.preview-canvas-wrapper {
  flex: 1;
  display: flex;
  gap: 0;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  background: #f3f4f6;
}
.preview-canvas {
  flex: 1;
  position: relative;
  overflow: hidden;
  background: #e5e7eb;
}
.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.preview-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #9ca3af;
  background: linear-gradient(135deg, #f3f4f6, #e5e7eb);
  i { font-size: 48px; }
  span { font-size: 13px; }
}
.preview-bbox {
  position: absolute;
  border-style: solid;
  border-radius: 2px;
  box-sizing: border-box;
}
.bbox-label {
  position: absolute;
  top: 0;
  left: 0;
  transform: translateY(-100%);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 1px 6px;
  border-radius: 2px 2px 0 0;
  white-space: nowrap;
}
.preview-toolbar {
  width: 40px;
  flex-shrink: 0;
  background: #fff;
  border-left: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 0;
  gap: 4px;
}
.toolbar-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  cursor: pointer;
  color: #6b7280;
  font-size: 16px;
  transition: all 0.2s;
  &:hover { background: #f3f4f6; color: #1f2937; }
  &.active { background: #eff6ff; color: #2563eb; }
}
.preview-hint {
  margin-top: 12px;
  font-size: 13px;
  color: #6b7280;
}
.preview-type-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
  min-height: 0;
}

.preview-text-card {
  flex: 1;
  padding: 20px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow-y: auto;
}

.preview-text-content {
  font-size: 14px;
  line-height: 1.8;
  color: #374151;
}
.preview-field-label {
  font-size: 12px;
  font-weight: 600;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 8px;
}

.preview-choices-area {
  padding: 12px 16px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.preview-choices-title {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

.preview-choices-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.preview-choice-tag {
  padding: 4px 14px;
  border: 1px solid #d1d5db;
  border-radius: 16px;
  font-size: 13px;
  color: #374151;
  background: #fff;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: #93c5fd;
    color: #2563eb;
  }
}

.preview-labels-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 8px 0;
}

.preview-label-tag {
  padding: 3px 12px;
  border: 1px solid;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.preview-audio-card {
  padding: 24px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.audio-player-main {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.audio-wave-container {
  position: relative;
  height: 60px;
  cursor: pointer;
  display: flex;
  align-items: center;
}

.audio-wave-bars {
  display: flex;
  align-items: center;
  gap: 2px;
  width: 100%;
  height: 100%;
}

.audio-wave-bar {
  flex: 1;
  background: #e5e7eb;
  border-radius: 2px;
  min-height: 4px;
  transition: background 0.1s ease;

  &.played {
    background: linear-gradient(to top, #3b82f6, #60a5fa);
  }
}

.audio-progress-line {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.audio-progress-played {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background: transparent;
}

.audio-progress-thumb {
  position: absolute;
  top: 50%;
  width: 12px;
  height: 12px;
  background: #3b82f6;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.4);
}

.audio-controls-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.audio-play-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: #3b82f6;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
  transition: background 0.2s ease;

  &:hover {
    background: #2563eb;
  }

  &:active {
    transform: scale(0.95);
  }
}

.audio-time-display {
  font-size: 13px;
  color: #6b7280;
  font-family: monospace;
}

.audio-current-time {
  color: #3b82f6;
  font-weight: 600;
}

.audio-total-time {
  color: #9ca3af;
}

.preview-html-card {
  flex: 1;
  padding: 20px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow-y: auto;
  font-size: 14px;
  line-height: 1.8;
  color: #374151;
}

.preview-video-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: linear-gradient(135deg, #1f2937, #374151);
  border-radius: 8px;
  color: #9ca3af;
}

.preview-video-icon {
  font-size: 56px;
  color: #d1d5db;
}

.preview-video-hint {
  font-size: 14px;
}
.preview-empty {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: #9ca3af;
  gap: 8px;
}
.preview-empty i {
  font-size: 48px;
  color: #d1d5db;
}
.preview-empty p {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #6b7280;
}
.preview-empty span {
  font-size: 13px;
  color: #9ca3af;
  max-width: 320px;
}
.preview-code {
  margin: 0;
  padding: 16px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 13px;
  line-height: 1.6;
  color: #374151;
  white-space: pre-wrap;
  word-break: break-all;
}
.config-back-bar {
  padding: 10px 0 16px;
}

/* 时间序列预览 */
.ts-preview-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: auto;
}

.ts-labels-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.ts-label-tag {
  display: inline-flex;
  align-items: center;
  padding: 3px 12px 3px 10px;
  border-left: 4px solid;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.ts-chart-wrap {
  position: relative;
  flex: 1;
  min-height: 220px;
  padding: 24px 40px 28px 44px;
  background: #fff;
}

.ts-chart {
  width: 100%;
  height: 100%;
  display: block;
}

.ts-y-axis {
  position: absolute;
  top: 24px;
  bottom: 28px;
  left: 6px;
  width: 36px;
  pointer-events: none;
}

.ts-y-label {
  position: absolute;
  transform: translateY(-50%);
  font-size: 10px;
  color: #9ca3af;
  white-space: nowrap;
}

.ts-top-axis {
  position: absolute;
  top: 8px;
  left: 44px;
  right: 40px;
  height: 14px;
  pointer-events: none;
}

.ts-top-label {
  position: absolute;
  transform: translateX(-50%);
  font-size: 9px;
  color: #9ca3af;
}

.ts-x-axis {
  position: absolute;
  bottom: 8px;
  left: 44px;
  right: 40px;
  height: 14px;
  pointer-events: none;
}

.ts-x-label {
  position: absolute;
  transform: translateX(-50%);
  font-size: 10px;
  color: #9ca3af;
}

.ts-channel-legend {
  position: absolute;
  top: 28px;
  left: 80px;
  pointer-events: none;
}

.ts-channel-name {
  font-size: 12px;
  color: #3b82f6;
  font-weight: 600;
}

.ts-selection-region {
  position: absolute;
  top: 24px;
  bottom: 28px;
  left: 44px;
  width: 22%;
  background: rgba(156, 163, 175, 0.18);
  border-left: 1px dashed #9ca3af;
  border-right: 1px dashed #9ca3af;
  pointer-events: none;
}

/* HyperText 预览 */
.hypertext-preview-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: auto;
}

.hypertext-labels-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hypertext-label-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 3px 10px 3px 8px;
  border-left: 4px solid;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.hypertext-label-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 8px;
  background: rgba(0, 0, 0, 0.08);
  font-size: 10px;
  font-weight: 600;
}

.hypertext-content {
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fafafa;
  font-size: 14px;
  line-height: 1.8;
  color: #374151;
}

.hypertext-content :deep(div) {
  padding: 10px 14px;
  margin-bottom: 10px;
  border-radius: 6px;
}

.hypertext-content :deep(div:first-child) {
  text-align: center;
  background: #f3f4f6;
}

.hypertext-content :deep(div:nth-child(2)) {
  margin-left: 60px;
  background: #f3f4f6;
}

.hypertext-content :deep(div:nth-child(3)) {
  background: #dcfce7;
}

/* Pairwise 预览 */
.pairwise-preview-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: auto;
}

.pairwise-title {
  font-size: 15px;
  font-weight: 600;
  color: #374151;
}

.pairwise-items-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 0 4px;
}

.pairwise-item-text {
  font-size: 14px;
  color: #374151;
  line-height: 1.6;
}

.pairwise-prompt {
  background: #2563eb;
  color: #fff;
  padding: 16px 20px;
  border-radius: 8px;
}

.pairwise-prompt-text {
  font-size: 14px;
  line-height: 1.7;
  font-weight: 500;
}

.pairwise-answers-row {
  display: flex;
  gap: 16px;
}

.pairwise-answer-card {
  flex: 1;
  background: #374151;
  color: #fff;
  padding: 18px 20px;
  border-radius: 8px;
  min-height: 120px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
}

.pairwise-answer-text {
  font-size: 13px;
  line-height: 1.7;
}
</style>