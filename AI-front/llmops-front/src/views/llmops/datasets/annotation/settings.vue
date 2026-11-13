<template>
  <div class="settings-container">
    <div class="settings-content">
      <!-- 头部 -->
      <div class="page-header">
        <div class="header-left">
          <div class="back-btn" @click="handleBack">
            <i class="el-icon-arrow-left"></i>
          </div>
          <div class="breadcrumb">
            <span class="breadcrumb-item">Projects</span>
            <span class="breadcrumb-separator">/</span>
            <span class="breadcrumb-item">{{ projectName }}</span>
            <span class="breadcrumb-separator">/</span>
            <span class="breadcrumb-item">Settings</span>
            <span class="breadcrumb-separator">/</span>
            <span class="breadcrumb-item active">{{ activeTabLabel }}</span>
          </div>
        </div>
        <div class="header-right">
          <el-button type="primary" size="small" @click="handleExport">Export</el-button>
        </div>
      </div>

      <div class="settings-body">
        <el-tabs v-model="activeTab" class="settings-tabs" tab-position="left" @tab-click="handleTabClick">
          <!-- 通用设置 -->
          <!-- <el-tab-pane label="通用" name="general">
            <div class="tab-pane-inner">
              <el-form :model="generalForm" label-width="0px" class="settings-form">
                <el-form-item>
                  <label class="form-label">项目名称</label>
                  <el-input v-model="generalForm.name" placeholder="请输入项目名称"></el-input>
                </el-form-item>

                <el-form-item>
                  <label class="form-label">描述</label>
                  <el-input
                    v-model="generalForm.description"
                    type="textarea"
                    :rows="4"
                    placeholder="可选的项目描述"></el-input>
                </el-form-item>

                <el-form-item>
                  <label class="form-label">颜色</label>
                  <div class="color-picker-row">
                    <div
                      v-for="color in colorOptions"
                      :key="color"
                      class="color-dot"
                      :class="{ active: generalForm.color === color }"
                      :style="{ background: color }"
                      @click="generalForm.color = color">
                      <i v-if="generalForm.color === color" class="el-icon-check"></i>
                    </div>
                  </div>
                </el-form-item>

                <el-form-item>
                  <label class="form-label">工作空间</label>
                  <el-select v-model="generalForm.workspace" placeholder="请选择工作空间" style="width: 100%;">
                    <el-option
                      v-for="item in workspaceOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"></el-option>
                  </el-select>
                </el-form-item>

                <el-form-item>
                  <label class="form-label">项目显示名称</label>
                  <el-input v-model="generalForm.showName" placeholder="请输入项目显示名称"></el-input>
                  <div class="form-hint">在列表中显示的项目名称，不影响项目本身的名称。</div>
                </el-form-item>

                <el-form-item class="switch-item">
                  <label class="form-label">专家模式</label>
                  <el-switch v-model="generalForm.expertMode"></el-switch>
                  <div class="form-hint">启用后，标注界面将显示所有高级控制选项。</div>
                </el-form-item>

                <el-form-item class="switch-item">
                  <label class="form-label">显示跳过按钮</label>
                  <el-switch v-model="generalForm.showSkip"></el-switch>
                </el-form-item>

                <el-form-item class="switch-item">
                  <label class="form-label">启用预测</label>
                  <el-switch v-model="generalForm.enablePredictions"></el-switch>
                  <div class="form-hint">允许在标注界面中查看和使用模型预测结果。</div>
                </el-form-item>

                <el-form-item class="switch-item">
                  <label class="form-label">匿名标注</label>
                  <el-switch v-model="generalForm.anonymous"></el-switch>
                  <div class="form-hint">标注人员无法看到其他标注人员的姓名和头像。</div>
                </el-form-item>

                <el-form-item class="switch-item">
                  <label class="form-label">允许取消已提交的标注</label>
                  <el-switch v-model="generalForm.allowCancel"></el-switch>
                </el-form-item>

                <div class="form-actions">
                  <el-button type="primary" @click="handleSave('general')">保存</el-button>
                </div>
              </el-form>
            </div>
          </el-tab-pane> -->

          <!-- 标注界面 -->
          <el-tab-pane label="标注界面" name="labeling">
            <div class="tab-pane-inner labeling-pane">
              <div class="labeling-interface">
                <!-- 左侧：Labeling Interface 配置面板 -->
                <div class="li-config-panel">
                  <div class="li-header">
                    <h2 class="li-title">Labeling Interface</h2>
                    <div class="li-header-actions">
                      <el-button size="small" class="browse-templates-btn" @click="showTemplates = !showTemplates">
                        Browse Templates
                      </el-button>
                      <div class="code-visual-toggle">
                        <span :class="{ active: liMode === 'code' }" @click="liMode = 'code'">Code</span>
                        <span :class="{ active: liMode === 'visual' }" @click="liMode = 'visual'">Visual</span>
                      </div>
                    </div>
                  </div>

                  <!-- 模板浏览（折叠） -->
                  <div v-if="showTemplates" class="templates-browse">
                    <div class="tb-grid">
                      <div
                        v-for="tpl in allTemplates"
                        :key="tpl.id"
                        class="tb-card"
                        :class="{ selected: selectedTemplate === tpl.id }"
                        @click="selectedTemplate = tpl.id">
                        <div class="tb-preview" :style="{ background: tpl.bg }">
                          <i :class="tpl.icon"></i>
                        </div>
                        <div class="tb-name">{{ tpl.name }}</div>
                      </div>
                    </div>
                  </div>

                  <!-- Code 模式：直接编辑 XML -->
                  <div v-if="liMode === 'code'" class="li-code-editor">
                    <el-input
                      v-model="labelingConfig"
                      type="textarea"
                      :rows="22"
                      placeholder="请输入 XML 配置"
                      class="code-textarea"></el-input>
                  </div>

                  <!-- Visual 模式：可视化配置 -->
                  <template v-else>
                    <!-- Configure data -->
                    <div class="li-section">
                      <div class="li-section-title">Configure data</div>
                      <div v-for="(field, idx) in dataFields" :key="idx" class="data-field-row">
                        <div class="field-variable">{{ field.variable }}</div>
                        <div class="field-control">
                          <label class="field-label">Use text from field</label>
                          <el-select v-model="field.source" placeholder="<set manually>" size="small" class="field-select">
                            <el-option label="<set manually>" value="manual"></el-option>
                            <el-option label="instruction" value="instruction"></el-option>
                            <el-option label="input" value="input"></el-option>
                            <el-option label="output" value="output"></el-option>
                          </el-select>
                        </div>
                      </div>
                    </div>

                    <!-- Add choices - 安全分类 -->
                    <div class="li-section">
                      <div class="li-section-title">Add choices</div>
                      <div class="li-section-desc">Use new line as a separator to add multiple labels</div>
                      <el-input
                        v-model="newSafetyLabel"
                        type="textarea"
                        :rows="2"
                        placeholder="输入标签，每行一个"
                        class="choices-textarea"></el-input>
                      <el-button size="small" type="primary" class="add-btn" @click="addSafetyLabel">Add</el-button>
                    </div>

                    <!-- Choices 列表 -->
                    <div class="li-section">
                      <div class="li-section-title">Choices ({{ safetyLabels.length }})</div>
                      <div class="choices-list">
                        <div v-for="(label, idx) in safetyLabels" :key="idx" class="choice-item">
                          <span class="choice-text">{{ label }}</span>
                          <i class="el-icon-close choice-remove" @click="removeSafetyLabel(idx)"></i>
                        </div>
                      </div>
                    </div>

                    <!-- Add choices - 其他分类（可折叠） -->
                    <div class="li-section collapsible" :class="{ collapsed: !showExtraChoices }">
                      <div class="li-section-header" @click="showExtraChoices = !showExtraChoices">
                        <div class="li-section-title">Add choices</div>
                        <i class="el-icon-arrow-down collapse-arrow" :class="{ expanded: showExtraChoices }"></i>
                      </div>
                      <div v-if="showExtraChoices" class="collapsible-body">
                        <div class="li-section-desc">Use new line as a separator to add multiple labels</div>
                        <el-input
                          v-model="newExtraLabel"
                          type="textarea"
                          :rows="2"
                          placeholder="输入标签，每行一个"
                          class="choices-textarea"></el-input>
                        <el-button size="small" type="primary" class="add-btn" @click="addExtraLabel">Add</el-button>
                        <div v-if="extraLabels.length > 0" class="li-section" style="margin-top: 16px;">
                          <div class="li-section-title">Choices ({{ extraLabels.length }})</div>
                          <div class="choices-list">
                            <div v-for="(label, idx) in extraLabels" :key="idx" class="choice-item">
                              <span class="choice-text">{{ label }}</span>
                              <i class="el-icon-close choice-remove" @click="removeExtraLabel(idx)"></i>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </template>

                  <div class="li-save-row">
                    <el-button type="primary" class="li-save-btn" :loading="labelingSaving" @click="handleSave('labeling')">Save</el-button>
                  </div>
                </div>

                <!-- 中间：Preview 预览面板 -->
                <div class="li-preview-panel">
                  <div class="preview-header">Preview</div>
                  <div class="preview-body" v-loading="sampleTaskLoading">
                    <!-- 动态渲染 Text 字段 -->
                    <div v-for="(field, idx) in previewTextFields" :key="idx" class="preview-block">
                      <div class="preview-block-title">{{ field.name || field.fieldName }}</div>
                      <div class="preview-sample">{{ field.content }}</div>
                    </div>

                    <!-- 动态渲染 Choices -->
                    <div v-for="(group, gIdx) in previewChoiceGroups" :key="gIdx" class="safety-section">
                      <div class="safety-title">{{ group.name }}</div>
                      <div class="safety-choices">
                        <div
                          v-for="(label, lIdx) in group.labels"
                          :key="lIdx"
                          class="safety-choice"
                          :class="{ selected: selectedChoices[gIdx] === label }"
                          @click="selectChoice(gIdx, label)">
                          {{ label }}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 右侧：Info / History 面板 -->
                <div class="li-info-panel">
                  <div class="info-tabs">
                    <span class="info-tab active">Info</span>
                    <span class="info-tab">History</span>
                    <div class="info-tabs-actions">
                      <i class="el-icon-refresh"></i>
                      <i class="el-icon-close"></i>
                    </div>
                  </div>
                  <div class="info-body">
                    <div class="info-icon">
                      <i class="el-icon-location-outline"></i>
                    </div>
                    <div class="info-title">View region details</div>
                    <div class="info-desc">Select a region to view its properties, metadata and available actions</div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 标注设置 -->
          <el-tab-pane label="标注" name="annotation">
            <div class="tab-pane-inner annotation-pane">
              <h2 class="annotation-title">Annotation Settings</h2>

              <div class="annotation-card">
                <!-- Labeling Instructions -->
                <div class="annotation-section">
                  <h3 class="annotation-section-title">Labeling Instructions</h3>
                  <p class="annotation-desc">
                    Write instructions to help users complete labeling tasks.
                  </p>
                  <p class="annotation-desc">
                    The instruction field supports HTML markup and it allows use of images, iframes (pdf).
                  </p>
                  <div class="annotation-switch-row">
                    <el-switch v-model="annotationForm.showBeforeLabeling"></el-switch>
                    <span class="annotation-switch-label">Show before labeling</span>
                  </div>
                  <el-input
                    v-model="annotationForm.instruction"
                    type="textarea"
                    :rows="5"
                    placeholder=""
                    class="annotation-textarea"></el-input>
                </div>

                <!-- Prelabeling -->
                <div class="annotation-section">
                  <h3 class="annotation-section-title">Prelabeling</h3>
                  <div class="annotation-switch-row">
                    <el-switch v-model="annotationForm.usePredictions"></el-switch>
                    <div class="annotation-switch-text">
                      <div class="annotation-switch-label">Use predictions to prelabel tasks</div>
                      <div class="annotation-switch-hint">Enable and select which set of predictions to use for prelabeling.</div>
                    </div>
                  </div>
                  <div class="annotation-select-row">
                    <label class="annotation-select-label">Select which predictions or which model you want to use:</label>
                    <el-select v-model="annotationForm.predictionsSource" placeholder="No model or predictions available" class="annotation-select" disabled>
                      <el-option label="No model or predictions available" value=""></el-option>
                    </el-select>
                  </div>
                </div>

                <!-- Maximum Annotations -->
                <div class="annotation-section">
                  <h3 class="annotation-section-title">Maximum annotations</h3>
                  <p class="annotation-desc">
                    Number of distinct annotators required to complete a task. When a task receives this many annotations from different users, it is marked as completed.
                  </p>
                  <div class="annotation-number-row">
                    <el-input-number
                      v-model="annotationForm.maximumAnnotations"
                      :min="1"
                      :max="10"
                      :step="1"
                      controls-position="right"
                      class="annotation-number-input">
                    </el-input-number>
                    <span class="annotation-number-hint">annotations per task</span>
                  </div>
                </div>

                <div class="annotation-save-row">
                  <el-button type="primary" class="annotation-save-btn" @click="handleSave('annotation')">Save</el-button>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 模型 -->
          <!-- <el-tab-pane label="模型" name="model">
            <div class="tab-pane-inner">
              <div class="section-hint">
                <i class="el-icon-info"></i>
                <span>连接机器学习后端，实现预标注和主动学习。</span>
              </div>
              <el-form :model="mlForm" label-width="0px" class="settings-form">
                <el-form-item>
                  <label class="form-label">ML 后端 URL</label>
                  <el-input v-model="mlForm.url" placeholder="例如：http://localhost:9090"></el-input>
                </el-form-item>
                <el-form-item class="switch-item">
                  <label class="form-label">启用主动学习</label>
                  <el-switch v-model="mlForm.activeLearning"></el-switch>
                  <div class="form-hint">根据模型不确定度智能选择下一个待标注任务。</div>
                </el-form-item>
                <el-form-item class="switch-item">
                  <label class="form-label">训练完成后自动训练</label>
                  <el-switch v-model="mlForm.autoTrain"></el-switch>
                </el-form-item>
                <div class="form-actions">
                  <el-button type="primary" @click="handleSave('model')">保存</el-button>
                </div>
              </el-form>
            </div>
          </el-tab-pane> -->
          <!-- 模型 -->
          <el-tab-pane label="模型" name="model">
            <div class="tab-pane-inner">
              <div class="section-hint" v-if="connectedModels.length === 0">
                <i class="el-icon-info"></i>
                <span>连接机器学习后端，实现预标注和主动学习。</span>
              </div>

              <!-- 已连接模型列表 -->
              <div v-if="connectedModels.length > 0" class="model-list">
                <div v-for="model in connectedModels" :key="model.id" class="model-card">
                  <div class="model-card-header">
                    <div class="model-status">
                      <span class="status-dot connected"></span>
                      <span class="status-text">Connected</span>
                    </div>
                    <el-dropdown @command="(cmd) => handleModelCommand(cmd, model)">
                      <i class="el-icon-more model-menu"></i>
                      <el-dropdown-menu slot="dropdown">
                        <el-dropdown-item command="edit">Edit</el-dropdown-item>
                        <el-dropdown-item command="test">Send Test Request</el-dropdown-item>
                        <el-dropdown-item command="train">Start Training</el-dropdown-item>
                        <el-dropdown-item command="delete" style="color:#f56c6c;">Delete</el-dropdown-item>
                      </el-dropdown-menu>
                    </el-dropdown>
                  </div>
                  <div class="model-title">{{ model.title }}</div>
                  <div class="model-meta">
                    <span class="model-url">{{ model.url }}</span>
                    <span class="model-created">Created {{ formatDate(model.created_at) }}</span>
                  </div>
                </div>

                <div class="model-info-box">
                  <p class="info-title">A connected model has been detected! If you wish to fetch predictions from this model, please follow these steps:</p>
                  <ol class="info-steps">
                    <li>Navigate to the Data Manager.</li>
                    <li>Select the desired tasks.</li>
                    <li>Click on Batch predictions from the Actions menu.</li>
                  </ol>
                  <p class="info-note">If you want to use the model predictions for prelabeling, please configure this in the <a href="#" @click.prevent="activeTab='annotation'">Annotation settings</a>.</p>
                </div>

                <div class="configuration-card">
                  <h3 class="config-title">Configuration</h3>
                  <el-form label-width="0px" class="settings-form">
                    <el-form-item class="switch-item">
                      <el-switch v-model="mlForm.auto_update"></el-switch>
                      <div class="switch-content">
                        <div class="switch-title">Start model training on annotation submission</div>
                        <div class="switch-desc">This option will send a request to /train with information about annotations. You can use this to enable an Active Learning loop. You can also manually start training through model menu in its card.</div>
                      </div>
                    </el-form-item>
                  </el-form>
                  <div class="config-actions">
                    <el-button type="primary" :loading="configSaving" @click="handleSaveConfig">Save</el-button>
                  </div>
                </div>
              </div>

              <!-- 连接新模型表单 -->
              <el-form v-else :model="mlForm" label-width="0px" class="settings-form" v-loading="modelLoading">
                <el-form-item>
                  <label class="form-label">模型标题</label>
                  <el-input v-model="mlForm.title" placeholder="请输入模型名称"></el-input>
                </el-form-item>
                <el-form-item>
                  <label class="form-label">模型版本</label>
                  <el-input v-model="mlForm.model_version" placeholder="例如：qwen2.5:0.5b"></el-input>
                </el-form-item>
                <el-form-item>
                  <label class="form-label">镜像地址</label>
                  <el-input v-model="mlForm.image" placeholder="例如：docker-registry:81/harbor-ailabx/label-studio-ml-backend:eversec-cpuv3"></el-input>
                </el-form-item>
                <el-form-item>
                  <label class="form-label">Base URL</label>
                  <el-input v-model="mlForm.base_url" placeholder="例如：http://192.168.224.248:11434"></el-input>
                </el-form-item>
                <el-form-item>
                  <label class="form-label">API Key</label>
                  <el-input v-model="mlForm.apikey" placeholder="Ollama 类型可不填" show-password></el-input>
                </el-form-item>
                <el-form-item>
                  <label class="form-label">温度 (Temperature)</label>
                  <el-input v-model="mlForm.temperature" placeholder="0"></el-input>
                </el-form-item>
                <el-form-item>
                  <label class="form-label">请求超时 (秒)</label>
                  <el-input v-model="mlForm.request_timeout" placeholder="120"></el-input>
                </el-form-item>
                <el-form-item>
                  <label class="form-label">标签 (LABELS)</label>
                  <el-input v-model="mlForm.labels" placeholder="0,1"></el-input>
                </el-form-item>
                <el-form-item>
                  <label class="form-label">描述</label>
                  <el-input v-model="mlForm.description" type="textarea" :rows="2" placeholder="可选"></el-input>
                </el-form-item>
                <el-form-item class="switch-item">
                  <label class="form-label">交互式预标注</label>
                  <el-switch v-model="mlForm.is_interactive"></el-switch>
                  <div class="form-hint">启用后，部分标注工具将在标注过程中向 ML 后端交互发送请求。</div>
                </el-form-item>
                <el-form-item class="switch-item">
                  <label class="form-label">训练完成后自动训练</label>
                  <el-switch v-model="mlForm.auto_update"></el-switch>
                </el-form-item>
                <div class="form-actions">
                  <el-button type="primary" :loading="modelSaving" @click="handleSaveModel">保存</el-button>
                </div>
              </el-form>
            </div>
          </el-tab-pane>

          <!-- 预测 -->
          <el-tab-pane label="预测" name="predictions">
            <div class="tab-pane-inner">
              <div v-if="predictionsLoading" class="predictions-loading">
                <i class="el-icon-loading"></i>
                <span>加载中...</span>
              </div>

              <!-- 无预测时的空状态 -->
              <div v-else-if="predictionsList.length === 0 && liveModels.length === 0" class="predictions-empty">
                <div class="predictions-empty-icon">
                  <i class="el-icon-magic-stick"></i>
                </div>
                <h3 class="predictions-empty-title">No predictions uploaded yet</h3>
                <p class="predictions-empty-desc">
                  Upload predictions to automatically prelabel your data and speed up annotation.
                  Import predictions from multiple model versions to compare their performance,
                  or connect live models from the Model page to generate predictions on demand.
                </p>
                <a class="predictions-learn-more" href="https://labelstud.io/guide/predictions" target="_blank" rel="noopener">
                  Learn more <i class="el-icon-top-right"></i>
                </a>
              </div>

              <!-- 有预测时的列表 -->
              <div v-else class="predictions-list">
                <h3 class="predictions-list-title">Predictions</h3>
                <p class="predictions-list-desc">
                  List of predictions available in the project. Each card is associated with a
                  separate model version. To learn about how to import predictions,
                  <a href="https://labelstud.io/guide/predictions" target="_blank" rel="noopener">see the documentation</a>.
                </p>

                <!-- 已导入的预测 -->
                <div v-if="predictionsList.length > 0" class="predictions-section">
                  <h4 class="predictions-section-title">Imported Predictions</h4>
                  <div class="predictions-cards">
                    <div v-for="(item, idx) in predictionsList" :key="'static-' + idx" class="prediction-card">
                      <div class="prediction-card-main">
                        <div class="prediction-card-title">{{ item.model_version }}</div>
                        <div class="prediction-card-meta">
                          <span class="prediction-count">
                            <i class="el-icon-magic-stick"></i> {{ item.count }}
                          </span>
                          <span class="prediction-time">
                            {{ formatPredictionTime(item.latest) }}
                          </span>
                        </div>
                      </div>
                      <el-dropdown @command="(cmd) => handlePredictionCommand(cmd, item)">
                        <i class="el-icon-more prediction-card-menu"></i>
                        <el-dropdown-menu slot="dropdown">
                          <el-dropdown-item command="delete" style="color:#f56c6c;">Delete</el-dropdown-item>
                        </el-dropdown-menu>
                      </el-dropdown>
                    </div>
                  </div>
                </div>

                <!-- 在线模型 -->
                <div v-if="liveModels.length > 0" class="predictions-section">
                  <h4 class="predictions-section-title">Live Models</h4>
                  <div class="predictions-cards">
                    <div v-for="(model, idx) in liveModels" :key="'live-' + idx" class="prediction-card">
                      <div class="prediction-card-main">
                        <div class="prediction-card-title">{{ model.model_version || model.title }}</div>
                        <div class="prediction-card-meta">
                          <span class="prediction-live-badge">
                            <span class="live-status-dot" :class="model.state === 'CO' ? 'connected' : 'disconnected'"></span>
                            {{ model.readable_state }}
                          </span>
                          <span class="prediction-live-title">{{ model.title }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 云存储 -->
          <el-tab-pane label="云存储" name="storage">
            <div class="tab-pane-inner">
              <div class="storage-tabs">
                <div
                  class="storage-tab"
                  :class="{ active: storageTab === 'source' }"
                  @click="storageTab = 'source'">源存储</div>
                <div
                  class="storage-tab"
                  :class="{ active: storageTab === 'target' }"
                  @click="storageTab = 'target'">目标存储</div>
              </div>

              <div class="storage-list">
                <div v-if="storages.length === 0" class="empty-storage">
                  <i class="el-icon-folder-opened"></i>
                  <p>暂无存储配置</p>
                  <el-button type="primary" plain @click="showAddStorage = true">添加存储</el-button>
                </div>
                <div v-else class="storage-items">
                  <div v-for="(item, idx) in storages" :key="idx" class="storage-item">
                    <div class="storage-info">
                      <i class="el-icon-cloudy storage-type-icon"></i>
                      <div>
                        <div class="storage-title">{{ item.title }}</div>
                        <div class="storage-path">{{ item.bucket }} / {{ item.prefix }}</div>
                      </div>
                    </div>
                    <div class="storage-actions">
                      <el-button type="text" size="small">同步</el-button>
                      <el-button type="text" size="small" style="color:#f56c6c;">删除</el-button>
                    </div>
                  </div>
                  <el-button type="primary" plain @click="showAddStorage = true">+ 添加存储</el-button>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- Webhooks -->
          <el-tab-pane label="Webhooks" name="webhooks">
            <div class="tab-pane-inner">
              <div class="section-hint">
                <i class="el-icon-info"></i>
                <span>配置 Webhook，在标注事件发生时向指定 URL 发送通知。</span>
              </div>
              <el-form :model="webhookForm" label-width="0px" class="settings-form">
                <el-form-item>
                  <label class="form-label">Webhook URL</label>
                  <el-input v-model="webhookForm.url" placeholder="https://example.com/webhook"></el-input>
                </el-form-item>
                <el-form-item>
                  <label class="form-label">密钥（可选）</label>
                  <el-input v-model="webhookForm.secret" type="password" placeholder="用于签名验证的密钥"></el-input>
                </el-form-item>
                <el-form-item class="switch-item">
                  <label class="form-label">启用 Webhook</label>
                  <el-switch v-model="webhookForm.enabled"></el-switch>
                </el-form-item>
                <div class="form-actions">
                  <el-button type="primary" @click="handleSave('webhook')">保存</el-button>
                </div>
              </el-form>
            </div>
          </el-tab-pane>

          <!-- 危险区域 -->
                    <!-- 危险区域 -->
          <el-tab-pane label="危险区域" name="danger">
            <div class="tab-pane-inner danger-pane">
              <h2 class="danger-title-page">危险区域</h2>
              <p class="danger-page-desc">
                请自行承担执行这些操作的风险。此页面上的操作无法撤销，请确保您的数据已备份。
              </p>

              <!-- 重置缓存 -->
              <div class="danger-action-card">
                <h3 class="danger-action-title">重置缓存</h3>
                <p class="danger-action-desc">
                  当您因现有标签的验证错误而无法修改标注配置，但您确信这些标签并不存在时，重置缓存可能会有所帮助。您可以使用此操作重置缓存后再试一次。
                </p>
                <el-button type="danger" plain class="danger-action-btn" @click="handleResetCache">重置缓存</el-button>
              </div>

              <!-- 关闭所有标签页 -->
              <div class="danger-action-card">
                <h3 class="danger-action-title">关闭所有标签页</h3>
                <p class="danger-action-desc">
                  如果数据管理器无法加载，关闭所有数据管理器标签页可能会有所帮助。
                </p>
                <el-button type="danger" plain class="danger-action-btn" @click="handleDropAllTabs">关闭所有标签页</el-button>
              </div>

              <!-- 删除项目 -->
              <div class="danger-action-card">
                <h3 class="danger-action-title">删除项目</h3>
                <p class="danger-action-desc">
                  删除项目将从数据库中移除所有任务、标注和项目数据。
                </p>
                <el-button type="danger" plain class="danger-action-btn" @click="handleDeleteProject">删除项目</el-button>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 编辑模型连接弹窗 -->
    <el-dialog
      title="Edit Model Connection"
      :visible.sync="editDialogVisible"
      width="520px"
      :close-on-click-modal="false"
      append-to-body>
      <el-form :model="editForm" label-width="0px" class="settings-form">
        <el-form-item>
          <label class="form-label">ML 后端 URL</label>
          <el-input v-model="editForm.url" placeholder="例如：http://localhost:9090"></el-input>
        </el-form-item>
        <el-form-item>
          <label class="form-label">模型标题</label>
          <el-input v-model="editForm.title" placeholder="请输入模型名称"></el-input>
        </el-form-item>
        <el-form-item>
          <label class="form-label">模型版本</label>
          <el-input v-model="editForm.model_version" placeholder="例如：qwen2.5-0.5b"></el-input>
        </el-form-item>
        <el-form-item>
          <label class="form-label">描述</label>
          <el-input v-model="editForm.description" type="textarea" :rows="2" placeholder="可选"></el-input>
        </el-form-item>
        <el-form-item class="switch-item">
          <label class="form-label">交互式预标注</label>
          <el-switch v-model="editForm.is_interactive"></el-switch>
          <div class="form-hint">启用后，部分标注工具将在标注过程中向 ML 后端交互发送请求。</div>
        </el-form-item>
        <el-form-item class="switch-item">
          <label class="form-label">提交标注后自动训练</label>
          <el-switch v-model="editForm.auto_update"></el-switch>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSaving" @click="handleSaveEdit">保存</el-button>
      </div>
    </el-dialog>

    <!-- 测试请求弹窗 -->
    <el-dialog
      title="Test Request"
      :visible.sync="testDialogVisible"
      width="720px"
      :close-on-click-modal="false"
      append-to-body
      @close="handleTestDialogClose">
      <div class="test-request-dialog">
        <el-button type="primary" :loading="testSending" @click="handleSendTestRequest">Send Request</el-button>
        <p class="test-request-hint">
          This sends a test request to the prediction endpoint of the ML Backend using a random task.
        </p>
        <div class="test-request-panels">
          <div class="test-request-panel">
            <div class="test-request-panel-title">Request</div>
            <el-input
              v-model="testRequest"
              type="textarea"
              :rows="10"
              readonly
              placeholder="Request will be shown here..."
              class="test-request-textarea"></el-input>
          </div>
          <div class="test-request-panel">
            <div class="test-request-panel-title">Response</div>
            <el-input
              v-model="testResponse"
              type="textarea"
              :rows="10"
              readonly
              placeholder="Response will be shown here..."
              class="test-request-textarea"></el-input>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 重置缓存弹窗 -->
    <el-dialog
      title="Reset Cache"
      :visible.sync="resetCacheDialogVisible"
      width="520px"
      :close-on-click-modal="false"
      append-to-body
      custom-class="reset-cache-dialog"
      @close="handleResetCacheDialogClose">
      <div class="reset-cache-content">
        <p class="reset-cache-desc">
          You are about to reset the cache for <strong>{{ projectName }}</strong>. This action cannot be undone.
        </p>
        <p class="reset-cache-hint">
          To proceed, type "cache" in the field below:
        </p>
        <el-input
          v-model="resetCacheInput"
          placeholder=""
          class="reset-cache-input"></el-input>
      </div>
      <div slot="footer">
        <el-button @click="resetCacheDialogVisible = false">Cancel</el-button>
        <el-button
          type="primary"
          :disabled="resetCacheInput.trim() !== 'cache'"
          :loading="resetCacheLoading"
          @click="confirmResetCache">Reset Cache</el-button>
      </div>
    </el-dialog>

    <!-- 关闭所有标签页弹窗 -->
    <el-dialog
      title="Drop All Tabs"
      :visible.sync="dropAllTabsDialogVisible"
      width="520px"
      :close-on-click-modal="false"
      append-to-body
      custom-class="drop-all-tabs-dialog"
      @close="handleDropAllTabsDialogClose">
      <div class="drop-all-tabs-content">
        <p class="drop-all-tabs-desc">
          You are about to drop all tabs for <strong>{{ projectName }}</strong>. This action cannot be undone.
        </p>
        <p class="drop-all-tabs-hint">
          To proceed, type "tabs" in the field below:
        </p>
        <el-input
          v-model="dropAllTabsInput"
          placeholder=""
          class="drop-all-tabs-input"></el-input>
      </div>
      <div slot="footer">
        <el-button @click="dropAllTabsDialogVisible = false">Cancel</el-button>
        <el-button
          type="primary"
          :disabled="dropAllTabsInput.trim() !== 'tabs'"
          :loading="dropAllTabsLoading"
          @click="confirmDropAllTabs">Drop All Tabs</el-button>
      </div>
    </el-dialog>

    <!-- 删除项目弹窗 -->
    <el-dialog
      title="Delete Project"
      :visible.sync="deleteProjectDialogVisible"
      width="520px"
      :close-on-click-modal="false"
      append-to-body
      custom-class="delete-project-dialog"
      @close="handleDeleteProjectDialogClose">
      <div class="delete-project-content">
        <p class="delete-project-desc">
          You are about to delete the project <strong>{{ projectName }}</strong>. This action cannot be undone.
        </p>
        <p class="delete-project-hint">
          To proceed, type "delete" in the field below:
        </p>
        <el-input
          v-model="deleteProjectInput"
          placeholder=""
          class="delete-project-input"></el-input>
      </div>
      <div slot="footer">
        <el-button @click="deleteProjectDialogVisible = false">Cancel</el-button>
        <el-button
          type="danger"
          :disabled="deleteProjectInput.trim() !== 'delete'"
          :loading="deleteProjectLoading"
          @click="confirmDeleteProject">Delete Project</el-button>
      </div>
    </el-dialog>

    <!-- 导出数据弹窗 -->
    <el-dialog
      title="Export data"
      :visible.sync="exportDialogVisible"
      width="520px"
      :close-on-click-modal="false"
      append-to-body
      custom-class="export-data-dialog">
      <div class="export-data-content">
        <p class="export-data-desc">
          You can export dataset in one of the following formats:
        </p>
        <div class="export-format-list" v-loading="exportFormatsLoading">
          <div
            v-for="fmt in exportFormats"
            :key="fmt.name"
            class="export-format-item"
            :class="{ disabled: fmt.disabled, selected: selectedExportFormat === fmt.name }"
            @click="!fmt.disabled && (selectedExportFormat = fmt.name)">
            <div class="export-format-radio">
              <el-radio
                v-model="selectedExportFormat"
                :label="fmt.name"
                :disabled="fmt.disabled"
                @click.native.stop></el-radio>
            </div>
            <div class="export-format-info">
              <div class="export-format-title">{{ fmt.title }}</div>
              <div class="export-format-desc">{{ fmt.description }}</div>
              <div v-if="fmt.tags && fmt.tags.length" class="export-format-tags">
                <span v-for="tag in fmt.tags" :key="tag" class="export-format-tag">{{ tag }}</span>
              </div>
            </div>
          </div>
        </div>
        <p class="export-data-footer">
          Can't find an export format?
          <a href="https://labelstud.io/guide/export.html" target="_blank" rel="noopener">Let us know in Slack or submit an issue to the Repository</a>
        </p>
        <p class="export-data-tip">
          Having a timeout or trouble exporting large projects?
        </p>
      </div>
      <div slot="footer">
        <el-button @click="exportDialogVisible = false">Cancel</el-button>
        <el-button
          type="primary"
          :disabled="!selectedExportFormat || exportFormatsLoading"
          :loading="exportLoading"
          @click="confirmExport">Export</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
// import { getProject, generateSampleTask, deleteProject } from '@/api/llmops/labelstudio'
import { getProject, generateSampleTask, deleteProject, getProjectModels, createProjectModel, deleteProjectModel, updateProjectModel, trainModel, testModelPredict, getProjectModelVersions, deleteProjectPredictions, resetProjectSummary, resetDataManagerViews, getProjectExportFormats, exportProjectData, updateProject, validateLabelConfig, createMlBackend, deleteMlBackend } from '@/api/llmops/labelstudio'

const COLOR_MAP = {
  red: '#ef4444',
  blue: '#3b82f6',
  green: '#10b981',
  yellow: '#f59e0b',
  orange: '#f97316',
  purple: '#8b5cf6',
  pink: '#ec4899',
  teal: '#14b8a6',
  cyan: '#06b6d4',
  indigo: '#6366f1',
  gray: '#6b7280',
  white: '#ffffff',
  black: '#1f2937'
}

function resolveColor(color) {
  if (!color) return '#3b82f6'
  if (color.startsWith('#')) return color
  return COLOR_MAP[color.toLowerCase()] || color
}

function parseLabelConfig(xml) {
  const parser = new DOMParser()
  const doc = parser.parseFromString(xml, 'text/xml')
  const parseError = doc.querySelector('parsererror')
  if (parseError) {
    return { error: 'XML 解析失败，请检查配置格式', nodes: [] }
  }
  const root = doc.documentElement
  if (!root) return { error: 'XML 解析失败，请检查配置格式', nodes: [] }
  function walk(node) {
    const children = []
    node.childNodes.forEach(child => {
      if (child.nodeType === 1) {
        children.push(walk(child))
      }
    })
    const attrs = {}
    for (let i = 0; i < node.attributes.length; i++) {
      const attr = node.attributes[i]
      attrs[attr.name] = attr.value
    }
    return { tag: node.tagName, attrs, children }
  }
  return { error: null, nodes: [walk(root)] }
}

const LabelingPreview = {
  name: 'LabelingPreview',
  props: {
    nodes: { type: Array, required: true }
  },
  data() {
    return {
      selected: {}
    }
  },
  methods: {
    resolveColor,
    pickChoice(name, value) {
      this.$set(this.selected, name, value)
    },
    renderNode(node) {
      const h = this.$createElement
      const tag = node.tag
      const attrs = node.attrs || {}
      const children = node.children || []
      switch (tag) {
        case 'View':
          return h('div', { class: 'ls-view' }, this.renderChildren(children))
        case 'Header':
          return h('div', { class: 'ls-header' }, attrs.value || '')
        case 'Text': {
          const value = attrs.value || ''
          const display = value.startsWith('$') ? 'Sample: ' + value.slice(1) + ' will go here.' : value
          return h('div', { class: 'ls-text' }, display)
        }
        case 'Image':
          return h('div', { class: 'ls-image-placeholder' }, [
            h('i', { class: 'el-icon-picture-outline' }),
            h('span', '图片预览区域')
          ])
        case 'Choices':
        case 'RectangleLabels':
        case 'PolygonLabels':
        case 'KeyPointLabels':
        case 'BrushLabels': {
          const name = attrs.name || ''
          const choiceNodes = children.filter(c => c.tag === 'Choice' || c.tag === 'Label')
          const choiceEls = choiceNodes.map(c => {
            const val = c.attrs.value || ''
            const bg = resolveColor(c.attrs.background)
            const isSelected = this.selected[name] === val
            return h('div', {
              class: ['ls-choice', { selected: isSelected }],
              style: { borderColor: bg, color: isSelected ? '#fff' : bg, background: isSelected ? bg : '#fff' },
              on: { click: () => this.pickChoice(name, val) }
            }, val)
          })
          return h('div', { class: 'ls-choices' }, choiceEls)
        }
        case 'Rating': {
          const max = parseInt(attrs.maxRating) || 5
          const name = attrs.name || ''
          const current = this.selected[name] || 0
          const stars = []
          for (let i = 1; i <= max; i++) {
            stars.push(h('i', {
              class: ['el-icon-star-off', 'ls-star', { active: i <= current }],
              on: { click: () => { this.$set(this.selected, name, i) } }
            }))
          }
          return h('div', { class: 'ls-rating' }, stars)
        }
        case 'TextArea':
          return h('textarea', {
            class: 'ls-textarea',
            attrs: { placeholder: attrs.placeholder || '输入内容...', rows: attrs.rows || 3 }
          })
        case 'Labels': {
          const labelNodes = children.filter(c => c.tag === 'Label')
          const els = labelNodes.map(c => {
            const val = c.attrs.value || ''
            const bg = resolveColor(c.attrs.background)
            return h('span', {
              class: 'ls-label-tag',
              style: { background: bg, color: '#fff' }
            }, val)
          })
          return h('div', { class: 'ls-labels' }, els)
        }
        case 'Pairwise':
          return h('div', { class: 'ls-pairwise' }, '对比选择区域')
        default:
          if (children.length) {
            return h('div', { class: 'ls-unknown' }, this.renderChildren(children))
          }
          return null
      }
    },
    renderChildren(children) {
      return children.map(c => this.renderNode(c)).filter(Boolean)
    }
  },
  render(h) {
    return h('div', { class: 'ls-preview' }, this.renderChildren(this.nodes))
  }
}

export default {
  name: 'AnnotationSettings',
  data() {
    return {
      projectId: this.$route.params.id,
      projectName: '',
      activeTab: 'labeling',
      storageTab: 'source',
      showAddStorage: false,
      // colorOptions: ['#3b82f6', '#10b981', '#f59e0b', '#8b5cf6', '#ef4444', '#ec4899', '#06b6d4', '#84cc16'],
      // workspaceOptions: [
      //   { value: 'ws1', label: '默认工作空间' },
      //   { value: 'ws2', label: 'NLP 团队' },
      //   { value: 'ws3', label: 'CV 团队' },
      //   { value: 'ws4', label: '语音团队' }
      // ],
      // generalForm: {
      //   name: '',
      //   description: '',
      //   color: '#3b82f6',
      //   workspace: 'ws1',
      //   showName: '',
      //   expertMode: false,
      //   showSkip: true,
      //   enablePredictions: true,
      //   anonymous: false,
      //   allowCancel: false
      // },
      labelingConfig: `<View>
  <Image name="image" value="$image"/>
  <RectangleLabels name="label" toName="image">
    <Label value="Airplane" background="blue"/>
    <Label value="Car" background="green"/>
  </RectangleLabels>
</View>`,
      liMode: 'visual',
      showTemplates: false,
      selectedTemplate: null,
      newSafetyLabel: '',
      newExtraLabel: '',
      showExtraChoices: false,
      selectedChoices: {},
      dataFields: [
        { variable: '$instruction', source: 'manual' },
        { variable: '$input', source: 'manual' },
        { variable: '$output', source: 'manual' }
      ],
      safetyLabels: ['安全', '存在风险'],
      extraLabels: [],
      allTemplates: [
        { id: 'cv-1', name: '多边形语义分割', icon: 'el-icon-picture-outline', bg: 'linear-gradient(135deg,#dbeafe,#bfdbfe)' },
        { id: 'cv-2', name: '掩码语义分割', icon: 'el-icon-picture', bg: 'linear-gradient(135deg,#fce7f3,#fbcfe8)' },
        { id: 'cv-3', name: '边界框目标检测', icon: 'el-icon-full-screen', bg: 'linear-gradient(135deg,#ede9fe,#ddd6fe)' },
        { id: 'nlp-1', name: '文本分类', icon: 'el-icon-document', bg: 'linear-gradient(135deg,#dbeafe,#bfdbfe)' },
        { id: 'nlp-2', name: '命名实体识别', icon: 'el-icon-edit', bg: 'linear-gradient(135deg,#fce7f3,#fbcfe8)' },
        { id: 'audio-1', name: '音频分类', icon: 'el-icon-microphone', bg: 'linear-gradient(135deg,#dbeafe,#bfdbfe)' }
      ],
      annotationForm: {
        showBeforeLabeling: false,
        instruction: '',
        usePredictions: true,
        predictionsSource: '',
        maximumAnnotations: 1
      },
      mlForm: {
        // url: '',
        // activeLearning: false,
        // autoTrain: false
        url: '',
        title: '',
        description: '',
        auth_method: 'NONE',
        basic_auth_user: 'NONE',
        basic_auth_pass: 'NONE',
        is_interactive: false,
        auto_update: false,
        model_version: '',
        timeout: 0,
        extra_params: '',
        // ML 后端部署配置
        image: 'docker-registry:81/harbor-ailabx/label-studio-ml-backend:eversec-cpuv3',
        base_url: 'http://192.168.224.248:11434',
        apikey: '',
        temperature: '0',
        request_timeout: '120',
        labels: '0,1'
      },
      connectedModels: [],
      modelLoading: false,
      modelSaving: false,
      configSaving: false,
      labelingSaving: false,
      editDialogVisible: false,
      editingModelId: null,
      editSaving: false,
      editForm: {
        url: '',
        title: '',
        description: '',
        is_interactive: false,
        auto_update: false,
        model_version: ''
      },
      testDialogVisible: false,
      testingModelId: null,
      testSending: false,
      testRequest: '',
      testResponse: '',
      predictionsList: [],
      predictionsLoading: false,
      liveModels: [],
      webhookForm: {
        url: '',
        secret: '',
        enabled: false
      },
      storages: [],
      sampleTaskData: {},
      sampleTaskLoading: false,
      sampleTaskTimer: null,
      isBuildingXml: false,
      resetCacheDialogVisible: false,
      resetCacheInput: '',
      resetCacheLoading: false,
      dropAllTabsDialogVisible: false,
      dropAllTabsInput: '',
      dropAllTabsLoading: false,
      deleteProjectDialogVisible: false,
      deleteProjectInput: '',
      deleteProjectLoading: false,
      exportDialogVisible: false,
      exportFormats: [],
      exportFormatsLoading: false,
      selectedExportFormat: '',
      exportLoading: false
    }
  },
  computed: {
    activeTabLabel() {
      const map = {
        // general: 'General',
        labeling: 'Labeling Interface',
        annotation: 'Annotation',
        model: 'Model',
        predictions: 'Predictions',
        storage: 'Cloud Storage',
        webhooks: 'Webhooks',
        danger: 'Danger Zone'
      }
      return map[this.activeTab] || 'Settings'
    },
    parsedResult() {
      if (!this.labelingConfig.trim()) return { error: null, nodes: [] }
      try {
        return parseLabelConfig(this.labelingConfig)
      } catch (e) {
        return { error: 'XML 解析失败：' + e.message, nodes: [] }
      }
    },
    parsedNodes() {
      return this.parsedResult.nodes
    },
    parseError() {
      return this.parsedResult.error
    },
    previewTextFields() {
      const xml = (this.labelingConfig || '').trim()
      if (!xml || xml === '<View></View>') return []
      let doc
      try {
        doc = new DOMParser().parseFromString(xml, 'text/xml')
      } catch (e) {
        return []
      }
      if (!doc || doc.querySelector('parsererror')) return []
      const fields = []
      doc.querySelectorAll('Text').forEach(el => {
        const name = el.getAttribute('name') || ''
        const value = el.getAttribute('value') || ''
        const fieldName = value.replace(/^\$/, '')
        const fromApi = this.resolveSampleValue(fieldName)
        fields.push({
          name,
          variable: value,
          fieldName,
          content: fromApi !== null ? fromApi : 'Sample: Your text will go here.'
        })
      })
      return fields
    },
    previewChoiceGroups() {
      const xml = (this.labelingConfig || '').trim()
      if (!xml || xml === '<View></View>') return []
      let doc
      try {
        doc = new DOMParser().parseFromString(xml, 'text/xml')
      } catch (e) {
        return []
      }
      if (!doc || doc.querySelector('parsererror')) return []
      const groups = []
      doc.querySelectorAll('Choices').forEach(el => {
        const name = el.getAttribute('name') || 'Choices'
        const labels = []
        el.querySelectorAll('Choice').forEach(c => {
          const v = c.getAttribute('value')
          if (v) labels.push(v)
        })
        if (labels.length > 0) {
          groups.push({ name, labels })
        }
      })
      return groups
    }
  },
  watch: {
    labelingConfig: {
      handler() {
        if (!this.isBuildingXml) {
          this.parseXmlToVisual()
        }
        this.debounceFetchSampleTask()
      },
      immediate: false
    }
  },
  created() {
    this.loadProject()
  },
  methods: {
    selectChoice(groupIdx, label) {
      this.$set(this.selectedChoices, groupIdx, label)
    },
    debounceFetchSampleTask() {
      if (this.sampleTaskTimer) {
        clearTimeout(this.sampleTaskTimer)
      }
      this.sampleTaskTimer = setTimeout(() => {
        this.fetchSampleTask()
      }, 500)
    },
    parseXmlToVisual() {
      const xml = (this.labelingConfig || '').trim()
      if (!xml || xml === '<View></View>') return
      let doc
      try {
        doc = new DOMParser().parseFromString(xml, 'text/xml')
      } catch (e) {
        return
      }
      if (!doc || doc.querySelector('parsererror')) return

      // 解析 Text 字段 → dataFields
      const textEls = doc.querySelectorAll('Text')
      const dataFields = []
      textEls.forEach(el => {
        const name = el.getAttribute('name') || ''
        const value = el.getAttribute('value') || ''
        dataFields.push({ variable: value || `$${name}`, source: 'manual' })
      })
      if (dataFields.length > 0) {
        this.dataFields = dataFields
      }

      // 解析 Choices → safetyLabels / extraLabels
      const choicesEls = doc.querySelectorAll('Choices')
      if (choicesEls.length > 0) {
        const safety = []
        const extra = []
        choicesEls.forEach((el, idx) => {
          const choiceValues = []
          el.querySelectorAll('Choice').forEach(c => {
            const v = c.getAttribute('value')
            if (v) choiceValues.push(v)
          })
          if (idx === 0) {
            safety.push(...choiceValues)
          } else {
            extra.push(...choiceValues)
          }
        })
        this.safetyLabels = safety
        this.extraLabels = extra
      }
    },
    buildXmlFromVisual() {
      const lines = ['<View>']
      this.dataFields.forEach(f => {
        const varName = f.variable.replace(/^\$/, '')
        lines.push(`  <Text name="${varName}" value="${f.variable}"/>`)
      })
      if (this.safetyLabels.length > 0) {
        lines.push('  <Choices name="safety" toName="text" choice="single">')
        this.safetyLabels.forEach(l => {
          lines.push(`    <Choice value="${l}"/>`)
        })
        lines.push('  </Choices>')
      }
      if (this.extraLabels.length > 0) {
        lines.push('  <Choices name="extra" toName="text" choice="single">')
        this.extraLabels.forEach(l => {
          lines.push(`    <Choice value="${l}"/>`)
        })
        lines.push('  </Choices>')
      }
      lines.push('</View>')
      this.isBuildingXml = true
      this.labelingConfig = lines.join('\n')
      this.$nextTick(() => {
        this.isBuildingXml = false
      })
    },
    async fetchSampleTask() {
      const xml = (this.labelingConfig || '').trim()
      if (!xml || xml === '<View></View>') {
        this.sampleTaskData = {}
        return
      }
      if (!this.projectId) return
      console.log('[sample-task] 开始请求, projectId:', this.projectId)
      this.sampleTaskLoading = true
      try {
        const res = await generateSampleTask(this.projectId, xml)
        console.log('[sample-task] 返回数据:', res)
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
    async loadProject() {
      try {
        const data = await getProject(this.projectId)
        if (data) {
          this.projectName = data.title || data.name || '未命名项目'
          // this.generalForm.name = data.title || data.name || ''
          // this.generalForm.description = data.description || ''
          // if (data.color) this.generalForm.color = data.color
          if (data.label_config) this.labelingConfig = data.label_config
        }
      } catch (e) {
        this.projectName = `项目 #${this.projectId}`
        this.generalForm.name = `项目 #${this.projectId}`
      }
      this.$nextTick(() => {
        this.parseXmlToVisual()
        this.debounceFetchSampleTask()
      })
    },
    async loadProject() {
      try {
        const data = await getProject(this.projectId)
        if (data) {
          this.projectName = data.title || data.name || '未命名项目'
          if (data.label_config) this.labelingConfig = data.label_config
          if (data.maximum_annotations != null) {
            this.annotationForm.maximumAnnotations = data.maximum_annotations
          }
          if (data.instruction != null) this.annotationForm.instruction = data.instruction
          if (data.show_instruction != null) this.annotationForm.showBeforeLabeling = !!data.show_instruction
        }
      } catch (e) {
        this.projectName = `项目 #${this.projectId}`
      }
      this.$nextTick(() => {
        this.parseXmlToVisual()
        this.debounceFetchSampleTask()
      })
      this.loadModels()
      this.loadPredictions()
    },
    handleBack() {
      this.$router.push('/llmops/datasets/annotation')
    },
    handleTabClick(tab) {
      if (tab.name === 'predictions') {
        this.loadPredictions()
      } else if (tab.name === 'model') {
        this.loadModels()
      }
    },
    async handleSave(type) {
      if (type === 'labeling') {
        const config = (this.labelingConfig || '').trim()
        if (!config) {
          this.$message.warning('标注配置不能为空')
          return
        }
        this.labelingSaving = true
        try {
          await validateLabelConfig(this.projectId, config)
        } catch (e) {
          const msg = (e && e.response && e.response.data && (e.response.data.detail || e.response.data.validation_errors)) || '标注配置格式不正确'
          this.$message.error(typeof msg === 'string' ? msg : JSON.stringify(msg))
          this.labelingSaving = false
          return
        }
        try {
          await updateProject(this.projectId, { label_config: config })
          this.$message.success('标注配置已保存')
        } catch (e) {
          console.error('[设置] 保存标注配置失败:', e)
          this.$message.error('保存失败，请稍后重试')
        } finally {
          this.labelingSaving = false
        }
        return
      }
      if (type === 'annotation') {
        try {
          await updateProject(this.projectId, {
            maximum_annotations: this.annotationForm.maximumAnnotations,
            instruction: this.annotationForm.instruction,
            show_instruction: this.annotationForm.showBeforeLabeling
          })
          this.$message.success('标注设置已保存')
        } catch (e) {
          console.error('[设置] 保存标注设置失败:', e)
          this.$message.error('保存失败，请稍后重试')
        }
        return
      }
      this.$message.success(`${this.getTabLabel(type)}已保存`)
    },
    async loadModels() {
      this.modelLoading = true
      try {
        const res = await getProjectModels(this.projectId)
        const list = (res && res.items) || (res && res.results) || (Array.isArray(res) ? res : [])
        this.connectedModels = list
        if (list && list.length > 0) {
          const m = list[0]
          this.mlForm.auto_update = !!m.auto_update
          this.mlForm.is_interactive = !!m.is_interactive
        }
      } catch (e) {
        console.error('[设置] 加载模型列表失败:', e)
        this.connectedModels = []
      } finally {
        this.modelLoading = false
      }
    },

    async loadPredictions() {
      this.predictionsLoading = true
      try {
        const res = await getProjectModelVersions(this.projectId)
        this.predictionsList = (res && res.static) || []
        this.liveModels = (res && res.live) || []
      } catch (e) {
        console.error('[设置] 加载预测列表失败:', e)
        this.predictionsList = []
        this.liveModels = []
      } finally {
        this.predictionsLoading = false
      }
    },

    formatPredictionTime(isoTime) {
      if (!isoTime) return ''
      try {
        const date = new Date(isoTime)
        const now = new Date()
        const diffMs = now - date
        const diffMin = Math.floor(diffMs / 60000)
        const diffHour = Math.floor(diffMs / 3600000)
        const diffDay = Math.floor(diffMs / 86400000)
        if (diffMin < 1) return 'Last prediction created just now'
        if (diffMin < 60) return `Last prediction created ${diffMin} minute${diffMin > 1 ? 's' : ''} ago`
        if (diffHour < 24) return `Last prediction created ${diffHour} hour${diffHour > 1 ? 's' : ''} ago`
        if (diffDay < 7) return `Last prediction created ${diffDay} day${diffDay > 1 ? 's' : ''} ago`
        return `Last prediction created ${date.toLocaleDateString()}`
      } catch (e) {
        return isoTime
      }
    },

    async handlePredictionCommand(cmd, item) {
      if (cmd === 'delete') {
        this.$confirm(`确定要删除模型版本 "${item.model_version}" 的所有预测吗？`, '提示', {
          type: 'warning'
        }).then(async () => {
          try {
            await deleteProjectPredictions(this.projectId, item.model_version)
            this.$message.success('预测已删除')
            await this.loadPredictions()
          } catch (e) {
            console.error('[设置] 删除预测失败:', e)
            this.$message.error('删除失败')
          }
        }).catch(() => {})
      }
    },

    async handleSaveModel() {
      if (!this.mlForm.title.trim()) {
        this.$message.warning('请输入模型标题')
        return
      }
      if (!this.mlForm.model_version.trim()) {
        this.$message.warning('请输入模型版本')
        return
      }
      if (!this.mlForm.image.trim()) {
        this.$message.warning('请输入镜像地址')
        return
      }
      if (!this.mlForm.base_url.trim()) {
        this.$message.warning('请输入 Base URL')
        return
      }
      const isOllamaType = this.mlForm.base_url.trim().endsWith(':11434') || this.mlForm.base_url.trim().endsWith(':11434/')
      if (!isOllamaType && !this.mlForm.apikey.trim()) {
        this.$message.warning('非 Ollama 类型建议填写 API Key')
      }
      this.modelSaving = true
      try {
        // 1. 生成后端名称（基于标题，仅保留小写字母数字和连字符）
        const backendName = 'ls-ml-' + this.mlForm.title.trim()
          .toLowerCase()
          .replace(/[^a-z0-9-]/g, '-')
          .replace(/-+/g, '-')
          .replace(/^-|-$/g, '')
          .slice(0, 40) || ('ls-ml-' + Date.now())

        const backendPort = 9090

        // 获取当前命名空间
        const namespace = (this.$store.state.project.currentProject && this.$store.state.project.currentProject.name) || ''

        // 判断模型类型：Base URL 以 11434 结尾则为 Ollama，否则为通用 OpenAI 兼容接口
        const baseUrl = this.mlForm.base_url.trim()
        const isOllama = baseUrl.endsWith(':11434') || baseUrl.endsWith(':11434/')

        const envVars = {
          MODEL_NAME: this.mlForm.model_version.trim(),
          TEMPERATURE: String(this.mlForm.temperature),
          REQUEST_TIMEOUT: String(this.mlForm.request_timeout),
          LABELS: this.mlForm.labels
        }
        if (isOllama) {
          envVars.OLLAMA_URL = baseUrl
        } else {
          envVars.BASE_URL = baseUrl
          if (this.mlForm.apikey) {
            envVars.API_KEY = this.mlForm.apikey
          }
        }

        // 2. 调用 /api/ml-backends 创建 Deployment + Service
        const mlBackendPayload = {
          name: backendName,
          namespace: namespace,
          image: this.mlForm.image.trim(),
          port: backendPort,
          replicas: 1,
          env: envVars,
          image_pull_policy: 'IfNotPresent',
          service_type: 'ClusterIP',
          resource_cpu: '2',
          resource_memory: '2Gi'
        }
        await createMlBackend(mlBackendPayload)

        // 3. 生成 ML 后端 URL: http://{name}.{namespace}:{port}
        const mlBackendUrl = `http://${backendName}.${namespace}:${backendPort}`

        // 4. 调用 /api/projects/{project_id}/models 连接模型
        const payload = {
          url: mlBackendUrl,
          title: this.mlForm.title.trim(),
          description: this.mlForm.description || '',
          auth_method: 'NONE',
          basic_auth_user: 'NONE',
          basic_auth_pass: 'NONE',
          is_interactive: !!this.mlForm.is_interactive,
          auto_update: !!this.mlForm.auto_update,
          model_version: this.mlForm.model_version || '',
          timeout: 0,
          extra_params: ''
        }
        await createProjectModel(this.projectId, payload)
        this.$message.success('模型部署并连接成功')
        await this.loadModels()
      } catch (e) {
        console.error('[设置] 保存模型失败:', e)
        const detail = (e.response && e.response.data && (e.response.data.detail || e.response.data.error)) || e.message
        this.$message.error('模型连接失败：' + detail)
      } finally {
        this.modelSaving = false
      }
    },

    async handleModelCommand(cmd, model) {
      if (cmd === 'delete') {
        this.$confirm(`确定要删除模型 "${model.title}" 吗？将同时删除对应的 ML 后端服务。`, '提示', {
          type: 'warning'
        }).then(async () => {
          try {
            // 从 model.url 中解析出后端名称和 namespace
            // URL 格式：http://{name}.{namespace}:{port} 或 http://{name}.{namespace}.svc.cluster.local:{port}
            const url = model.url || ''
            const host = url.replace(/^https?:\/\//, '').split(':')[0]
            const parts = host.split('.')
            const backendName = parts[0] || ''
            const namespace = parts[1] || ''

            // 1. 删除 ML 后端（Deployment + Service）
            if (backendName && namespace) {
              try {
                await deleteMlBackend(backendName, namespace)
              } catch (e) {
                console.warn('[设置] 删除 ML 后端失败:', e)
              }
            }

            // 2. 断开模型连接
            await deleteProjectModel(model.id)
            this.$message.success('模型已删除')
            await this.loadModels()
          } catch (e) {
            this.$message.error('删除失败')
          }
        }).catch(() => {})
      } else if (cmd === 'edit') {
        this.editingModelId = model.id
        this.editForm = {
          url: model.url || '',
          title: model.title || '',
          description: model.description || '',
          is_interactive: !!model.is_interactive,
          auto_update: !!model.auto_update,
          model_version: model.model_version || ''
        }
        this.editDialogVisible = true
      } else if (cmd === 'test') {
        this.testingModelId = model.id
        this.testRequest = ''
        this.testResponse = ''
        this.testDialogVisible = true
      } else if (cmd === 'train') {
        this.$confirm(`确定要启动模型 "${model.title}" 的训练吗？`, '提示', {
          type: 'info'
        }).then(async () => {
          try {
            await trainModel(model.id)
            this.$message.success('训练任务已发送至 ML 后端')
          } catch (e) {
            const detail = (e.response && e.response.data && (e.response.data.detail || e.response.data.error)) || e.message
            this.$message.error('启动训练失败：' + detail)
          }
        }).catch(() => {})
      }
    },

    async handleSaveEdit() {
      if (!this.editForm.url.trim()) {
        this.$message.warning('请输入 ML 后端 URL')
        return
      }
      if (!this.editForm.title.trim()) {
        this.$message.warning('请输入模型标题')
        return
      }
      this.editSaving = true
      try {
        const model = this.connectedModels.find(m => m.id === this.editingModelId) || {}
        const payload = {
          url: this.editForm.url.trim(),
          title: this.editForm.title.trim(),
          description: this.editForm.description || '',
          auth_method: model.auth_method || 'NONE',
          basic_auth_user: model.basic_auth_user || 'NONE',
          basic_auth_pass: model.basic_auth_pass || 'NONE',
          is_interactive: !!this.editForm.is_interactive,
          auto_update: !!this.editForm.auto_update,
          model_version: this.editForm.model_version || '',
          timeout: model.timeout || 0,
          extra_params: model.extra_params || ''
        }
        await updateProjectModel(this.editingModelId, payload)
        this.$message.success('模型配置已更新')
        this.editDialogVisible = false
        await this.loadModels()
      } catch (e) {
        console.error('[设置] 更新模型失败:', e)
        this.$message.error('更新失败，请检查配置')
      } finally {
        this.editSaving = false
      }
    },

    async handleSendTestRequest() {
      if (!this.testingModelId) return
      this.testSending = true
      this.testRequest = ''
      this.testResponse = ''
      try {
        const res = await testModelPredict(this.testingModelId)
        this.testRequest = JSON.stringify((res && res.request) || {}, null, 2)
        this.testResponse = JSON.stringify((res && res.response) || {}, null, 2)
      } catch (e) {
        const errData = e.response && e.response.data
        this.testRequest = JSON.stringify({
          method: 'POST',
          url: `/api/models/${this.testingModelId}/predict/test`,
          params: { random: true }
        }, null, 2)
        this.testResponse = JSON.stringify(errData || { error: e.message }, null, 2)
      } finally {
        this.testSending = false
      }
    },

    handleTestDialogClose() {
      this.testRequest = ''
      this.testResponse = ''
      this.testingModelId = null
    },

    async handleSaveConfig() {
      if (!this.connectedModels || this.connectedModels.length === 0) {
        this.$message.warning('暂无已连接的模型')
        return
      }
      const model = this.connectedModels[0]
      this.configSaving = true
      try {
        const payload = {
          url: model.url || '',
          title: model.title || '',
          description: model.description || '',
          auth_method: model.auth_method || 'NONE',
          basic_auth_user: model.basic_auth_user || 'NONE',
          basic_auth_pass: model.basic_auth_pass || 'NONE',
          is_interactive: !!model.is_interactive,
          auto_update: !!this.mlForm.auto_update,
          model_version: model.model_version || '',
          timeout: model.timeout || 0,
          extra_params: model.extra_params || ''
        }
        await updateProjectModel(model.id, payload)
        this.$message.success('配置已保存')
        await this.loadModels()
      } catch (e) {
        console.error('[设置] 保存模型配置失败:', e)
        this.$message.error('保存失败，请稍后重试')
      } finally {
        this.configSaving = false
      }
    },

    formatDate(dateStr) {
      if (!dateStr) return ''
      try {
        const d = new Date(dateStr)
        const now = new Date()
        const diffDays = Math.floor((now - d) / (1000 * 60 * 60 * 24))
        if (diffDays === 0) return 'today'
        if (diffDays === 1) return 'yesterday'
        if (diffDays < 30) return `${diffDays} days ago`
        return d.toLocaleDateString()
      } catch (e) {
        return dateStr
      }
    },
    getTabLabel(type) {
      const map = {
        // general: '通用设置',
        labeling: '标注配置',
        annotation: '标注设置',
        model: '模型设置',
        predictions: '预测设置',
        webhook: 'Webhook 设置'
      }
      return map[type] || '设置'
    },
    handleResetLabeling() {
      this.labelingConfig = `<View>
  <Image name="image" value="$image"/>
  <RectangleLabels name="label" toName="image">
    <Label value="Airplane" background="blue"/>
    <Label value="Car" background="green"/>
  </RectangleLabels>
</View>`
      this.$message.info('已重置为默认配置')
    },
    addSafetyLabel() {
      const text = this.newSafetyLabel.trim()
      if (!text) return
      const lines = text.split('\n').map(l => l.trim()).filter(Boolean)
      this.safetyLabels.push(...lines)
      this.newSafetyLabel = ''
      this.buildXmlFromVisual()
    },
    removeSafetyLabel(idx) {
      this.safetyLabels.splice(idx, 1)
      this.buildXmlFromVisual()
    },
    addExtraLabel() {
      const text = this.newExtraLabel.trim()
      if (!text) return
      const lines = text.split('\n').map(l => l.trim()).filter(Boolean)
      this.extraLabels.push(...lines)
      this.newExtraLabel = ''
      this.buildXmlFromVisual()
    },
    removeExtraLabel(idx) {
      this.extraLabels.splice(idx, 1)
      this.buildXmlFromVisual()
    },
    handleResetCache() {
      this.resetCacheInput = ''
      this.resetCacheDialogVisible = true
    },
    handleResetCacheDialogClose() {
      this.resetCacheInput = ''
      this.resetCacheLoading = false
    },
    async confirmResetCache() {
      if (this.resetCacheInput.trim() !== 'cache') return
      this.resetCacheLoading = true
      try {
        await resetProjectSummary(this.projectId)
        this.$message.success('缓存已重置')
        this.resetCacheDialogVisible = false
      } catch (e) {
        console.error('[设置] 重置缓存失败:', e)
        this.$message.error('重置缓存失败，请稍后重试')
      } finally {
        this.resetCacheLoading = false
      }
    },
    handleDropAllTabs() {
      this.dropAllTabsInput = ''
      this.dropAllTabsDialogVisible = true
    },
    handleDropAllTabsDialogClose() {
      this.dropAllTabsInput = ''
      this.dropAllTabsLoading = false
    },
    async confirmDropAllTabs() {
      if (this.dropAllTabsInput.trim() !== 'tabs') return
      this.dropAllTabsLoading = true
      try {
        await resetDataManagerViews(this.projectId)
        this.$message.success('所有标签页已关闭')
        this.dropAllTabsDialogVisible = false
      } catch (e) {
        console.error('[设置] 关闭所有标签页失败:', e)
        this.$message.error('关闭所有标签页失败，请稍后重试')
      } finally {
        this.dropAllTabsLoading = false
      }
    },
    handleDeleteProject() {
      this.deleteProjectInput = ''
      this.deleteProjectDialogVisible = true
    },
    handleDeleteProjectDialogClose() {
      this.deleteProjectInput = ''
      this.deleteProjectLoading = false
    },
    async confirmDeleteProject() {
      if (this.deleteProjectInput.trim() !== 'delete') return
      this.deleteProjectLoading = true
      try {
        await deleteProject(this.projectId)
        this.$message.success('项目已删除')
        this.deleteProjectDialogVisible = false
        this.$router.push('/llmops/datasets/annotation')
      } catch (e) {
        console.error('[设置] 删除项目失败:', e)
        this.$message.error('删除项目失败，请稍后重试')
      } finally {
        this.deleteProjectLoading = false
      }
    },
    async handleExport() {
      this.exportDialogVisible = true
      this.exportFormats = []
      this.selectedExportFormat = ''
      this.exportFormatsLoading = true
      try {
        const res = await getProjectExportFormats(this.projectId)
        this.exportFormats = res.formats || []
        const firstAvailable = this.exportFormats.find(f => !f.disabled)
        if (firstAvailable) {
          this.selectedExportFormat = firstAvailable.name
        }
      } catch (e) {
        console.error('[设置] 获取导出格式列表失败:', e)
        this.$message.error('获取导出格式列表失败')
      } finally {
        this.exportFormatsLoading = false
      }
    },
    async confirmExport() {
      if (!this.selectedExportFormat) return
      this.exportLoading = true
      try {
        const res = await exportProjectData(this.projectId, this.selectedExportFormat)
        const blob = res.data
        const disposition = res.headers['content-disposition'] || ''
        let fileName = ''
        const utf8Match = disposition.match(/filename\*=UTF-8''([^;]+)/i)
        const plainMatch = disposition.match(/filename="?([^";]+)"?/i)
        if (utf8Match) {
          fileName = decodeURIComponent(utf8Match[1])
        } else if (plainMatch) {
          fileName = plainMatch[1]
        } else {
          const ext = (this.selectedExportFormat || 'json').toLowerCase().replace(/_/g, '-')
          fileName = `${this.projectName || 'project'}-${ext}`
        }
        const fmt = this.exportFormats.find(f => f.name === this.selectedExportFormat)
        const url = window.URL.createObjectURL(new Blob([blob]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', fileName)
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        this.$message.success(`已导出为 ${fmt ? fmt.title : this.selectedExportFormat} 格式`)
        this.exportDialogVisible = false
      } catch (e) {
        console.error('[设置] 导出数据失败:', e)
        this.$message.error('导出数据失败，请稍后重试')
      } finally {
        this.exportLoading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.settings-container {
  min-height: calc(100vh - 60px);
}

.settings-content {
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #374151;
  transition: all 0.2s ease;

  &:hover {
    color: #2563eb;
    border-color: #bfdbfe;
    background: #eff6ff;
  }

  i {
    font-size: 16px;
  }
}

.header-titles {
  display: flex;
  flex-direction: column;
}

.page-title {
  font-size: 20px;
  font-weight: bold;
  color: #1f2937;
  margin: 0 0 4px 0;
}

.page-subtitle {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
}

.settings-body {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.settings-tabs {
  ::v-deep .el-tabs__header {
    margin: 0;
    border-right: 1px solid #e5e7eb;
    background: #f9fafb;
    padding: 16px 0;
  }

  ::v-deep .el-tabs__nav-wrap::after {
    display: none;
  }

  ::v-deep .el-tabs__item {
    padding: 0 24px !important;
    height: 40px;
    line-height: 40px;
    font-size: 14px;
    color: #6b7280;
    text-align: left;

    &:hover {
      color: #2563eb;
    }
  }

  ::v-deep .el-tabs__item.is-active {
    color: #1f2937;
    font-weight: 600;
    background: #eff6ff;
    border-right: 3px solid #2563eb;
  }

  ::v-deep .el-tabs__active-bar {
    display: none;
  }

  ::v-deep .el-tabs__content {
    padding: 28px 36px;
  }
}

.tab-pane-inner {
  max-width: 640px;

  &.labeling-pane {
    max-width: none;
  }
}

/* 标注界面两栏布局 */
.labeling-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.labeling-left {
  flex: 1;
  min-width: 0;
}

.labeling-right {
  flex: 1;
  min-width: 0;
  position: sticky;
  top: 20px;
}

.preview-panel {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  overflow: hidden;
  background: #fafafa;
}

.preview-panel-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;

  i {
    color: #2563eb;
    font-size: 16px;
  }
}

.preview-panel-body {
  padding: 20px;
  min-height: 420px;
  max-height: 560px;
  overflow-y: auto;
}

.preview-error {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  color: #b91c1c;
  font-size: 13px;

  i {
    font-size: 18px;
    flex-shrink: 0;
  }
}

.preview-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #9ca3af;

  i {
    font-size: 40px;
    margin-bottom: 10px;
    color: #d1d5db;
  }

  p {
    margin: 0;
    font-size: 14px;
  }
}

.preview-content {
  background: #fff;
  border-radius: 8px;
}

/* 预览内部组件样式 */
::v-deep .ls-preview {
  font-size: 14px;
  color: #1f2937;
}

::v-deep .ls-view {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 12px;
}

::v-deep .ls-header {
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
  padding: 4px 0;
  border-bottom: 1px solid #e5e7eb;
}

::v-deep .ls-text {
  padding: 10px 12px;
  background: #f9fafb;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
  color: #6b7280;
  font-size: 13px;
}

::v-deep .ls-image-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 180px;
  background: #f3f4f6;
  border: 2px dashed #d1d5db;
  border-radius: 8px;
  color: #9ca3af;
  font-size: 13px;

  i {
    font-size: 36px;
    color: #d1d5db;
  }
}

::v-deep .ls-choices {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 4px 0;
}

::v-deep .ls-choice {
  padding: 6px 16px;
  border: 1.5px solid #3b82f6;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;

  &:hover {
    opacity: 0.85;
  }

  &.selected {
    color: #fff;
  }
}

::v-deep .ls-rating {
  display: flex;
  gap: 6px;

  .ls-star {
    font-size: 24px;
    color: #d1d5db;
    cursor: pointer;
    transition: color 0.15s;

    &.active {
      color: #f59e0b;
    }

    &:hover {
      color: #f59e0b;
    }
  }
}

::v-deep .ls-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 13px;
  color: #374151;
  resize: vertical;
  box-sizing: border-box;

  &:focus {
    outline: none;
    border-color: #3b82f6;
  }
}

::v-deep .ls-labels {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

::v-deep .ls-label-tag {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  color: #fff;
}

::v-deep .ls-pairwise {
  padding: 40px;
  text-align: center;
  color: #9ca3af;
  background: #f9fafb;
  border: 1px dashed #e5e7eb;
  border-radius: 8px;
}

::v-deep .ls-unknown {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.section-hint {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px 16px;
  background: #eff6ff;
  border-radius: 8px;
  margin-bottom: 24px;
  font-size: 13px;
  color: #1e40af;

  i {
    margin-top: 2px;
    color: #2563eb;
  }
}

.settings-form {
  ::v-deep .el-form-item {
    margin-bottom: 24px;
  }
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

.form-hint {
  font-size: 12px;
  color: #6b7280;
  margin-top: 6px;
}

.switch-item {
  ::v-deep .el-form-item__content {
    display: flex;
    align-items: center;
    gap: 16px;
    flex-wrap: wrap;
  }
}

.color-picker-row {
  display: flex;
  gap: 10px;
}

.color-dot {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid transparent;
  transition: all 0.2s ease;

  &:hover {
    transform: scale(1.1);
  }

  &.active {
    border-color: #fff;
    box-shadow: 0 0 0 2px #2563eb;
  }

  i {
    color: #fff;
    font-size: 14px;
    font-weight: bold;
  }
}

.form-actions {
  display: flex;
  gap: 10px;
  padding-top: 8px;
}

.storage-tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 20px;
  background: #f3f4f6;
  border-radius: 8px;
  padding: 4px;
  width: fit-content;
}

.storage-tab {
  padding: 8px 20px;
  border-radius: 6px;
  font-size: 14px;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s ease;

  &.active {
    background: #fff;
    color: #1f2937;
    font-weight: 600;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  }
}

.empty-storage {
  text-align: center;
  padding: 40px;
  color: #9ca3af;

  i {
    font-size: 48px;
    margin-bottom: 12px;
    color: #d1d5db;
  }

  p {
    margin: 0 0 16px 0;
    font-size: 14px;
  }
}

.storage-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.storage-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #f9fafb;
}

.storage-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.storage-type-icon {
  font-size: 24px;
  color: #3b82f6;
}

.storage-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.storage-path {
  font-size: 12px;
  color: #6b7280;
  margin-top: 2px;
}

.storage-actions {
  display: flex;
  gap: 8px;
}

.danger-card {
  border: 1px solid #fecaca;
  border-radius: 12px;
  background: #fef2f2;
  padding: 20px;
}

.danger-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.danger-title {
  font-size: 15px;
  font-weight: 600;
  color: #b91c1c;
  margin-bottom: 4px;
}

.danger-desc {
  font-size: 13px;
  color: #6b7280;
}

/* 面包屑 */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.breadcrumb-item {
  color: #6b7280;

  &.active {
    color: #1f2937;
    font-weight: 600;
  }
}

.breadcrumb-separator {
  color: #d1d5db;
}

/* 标注界面三栏布局 */
.labeling-interface {
  display: flex;
  gap: 0;
  min-height: 600px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}

.li-config-panel {
  flex: 0 0 380px;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  max-height: 80vh;
}

.li-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
  flex-wrap: wrap;
  gap: 10px;
}

.li-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.li-header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.browse-templates-btn {
  border: 1px solid #2563eb;
  color: #2563eb;
  background: #fff;

  &:hover {
    background: #eff6ff;
  }
}

.code-visual-toggle {
  display: flex;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  overflow: hidden;

  span {
    padding: 6px 14px;
    font-size: 13px;
    color: #6b7280;
    cursor: pointer;
    transition: all 0.2s ease;

    &.active {
      background: #f3f4f6;
      color: #1f2937;
      font-weight: 600;
    }

    &:hover {
      background: #f9fafb;
    }
  }
}

.li-code-editor {
  padding: 16px 20px;
}

.code-textarea {
  ::v-deep .el-textarea__inner {
    font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
    font-size: 13px;
    line-height: 1.6;
  }
}

.li-section {
  padding: 16px 20px;
  border-bottom: 1px solid #f3f4f6;

  &:last-child {
    border-bottom: none;
  }

  &.collapsible {
    padding: 0;

    &.collapsed .collapsible-body {
      display: none;
    }
  }
}

.li-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  cursor: pointer;
  border-bottom: 1px solid #f3f4f6;

  &:hover {
    background: #f9fafb;
  }
}

.collapse-arrow {
  font-size: 14px;
  color: #9ca3af;
  transition: transform 0.2s ease;

  &.expanded {
    transform: rotate(180deg);
  }
}

.collapsible-body {
  padding: 16px 20px;
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
  margin-bottom: 10px;
}

.data-field-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;

  &:last-child {
    margin-bottom: 0;
  }
}

.field-variable {
  flex: 0 0 90px;
  padding: 8px 10px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 13px;
  color: #374151;
  font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
}

.field-control {
  flex: 1;
}

.field-label {
  display: block;
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 4px;
}

.field-select {
  width: 100%;
}

.choices-textarea {
  margin-bottom: 10px;

  ::v-deep .el-textarea__inner {
    font-size: 13px;
  }
}

.add-btn {
  margin-bottom: 0;
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
  padding: 8px 12px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 13px;
  color: #374151;
}

.choice-text {
  flex: 1;
}

.choice-remove {
  color: #9ca3af;
  cursor: pointer;
  font-size: 14px;

  &:hover {
    color: #ef4444;
  }
}

.li-save-row {
  padding: 16px 20px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: flex-end;
  margin-top: auto;
}

.li-save-btn {
  min-width: 80px;
}

/* 预览面板 */
.li-preview-panel {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: #fff;
  overflow: hidden;
}

.preview-header {
  padding: 12px 20px;
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  border-bottom: 1px solid #e5e7eb;
  background: #fafafa;
}

.preview-body {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  max-height: 80vh;
}

.preview-block {
  margin-bottom: 16px;
}

.preview-block-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.preview-sample {
  padding: 10px 12px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 13px;
  color: #6b7280;
}

.safety-section {
  margin-top: 20px;
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
  border: 1.5px solid #2563eb;
  border-radius: 20px;
  font-size: 13px;
  color: #2563eb;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    opacity: 0.85;
  }

  &.selected {
    background: #2563eb;
    color: #fff;
  }
}

/* Info / History 面板 */
.li-info-panel {
  flex: 0 0 240px;
  border-left: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.info-tabs {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #e5e7eb;
  padding: 0 12px;
}

.info-tab {
  padding: 12px 16px;
  font-size: 13px;
  color: #6b7280;
  cursor: pointer;
  border-bottom: 2px solid transparent;

  &.active {
    color: #1f2937;
    font-weight: 600;
    border-bottom-color: #2563eb;
  }
}

.info-tabs-actions {
  margin-left: auto;
  display: flex;
  gap: 8px;

  i {
    color: #9ca3af;
    cursor: pointer;
    font-size: 14px;

    &:hover {
      color: #374151;
    }
  }
}

.info-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;
}

.info-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #eff6ff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;

  i {
    font-size: 24px;
    color: #2563eb;
  }
}

.info-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 6px;
}

.info-desc {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.5;
}

/* 模板浏览 */
.templates-browse {
  padding: 16px 20px;
  border-bottom: 1px solid #f3f4f6;
  background: #fafafa;
}

.tb-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.tb-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #fff;

  &:hover {
    border-color: #2563eb;
    box-shadow: 0 2px 8px rgba(37, 99, 235, 0.12);
  }

  &.selected {
    border-color: #2563eb;
    box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.2);
  }
}

.tb-preview {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;

  i {
    font-size: 24px;
    color: #6b7280;
  }
}

.tb-name {
  padding: 8px;
  font-size: 12px;
  color: #374151;
  text-align: center;
  border-top: 1px solid #f3f4f6;
}

/* Annotation 设置页 */
.annotation-pane {
  max-width: 720px;
}

.annotation-title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 20px 0;
}

.annotation-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 28px 32px;
}

.annotation-section {
  margin-bottom: 32px;

  &:last-of-type {
    margin-bottom: 24px;
  }
}

.annotation-section-title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 12px 0;
}

.annotation-desc {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 6px 0;
  line-height: 1.6;
}

.annotation-switch-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 16px 0;
}

.annotation-switch-label {
  font-size: 14px;
  color: #374151;
}

.annotation-switch-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.annotation-switch-hint {
  font-size: 13px;
  color: #6b7280;
}

.annotation-textarea {
  margin-top: 4px;

  ::v-deep .el-textarea__inner {
    border: 1px solid #e5e7eb;
    border-radius: 6px;
    font-size: 14px;
    resize: vertical;
  }
}

.annotation-select-row {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.annotation-select-label {
  font-size: 14px;
  color: #374151;
}

.annotation-select {
  width: 100%;
  max-width: 420px;
}

.annotation-number-row {
  margin-top: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.annotation-number-input {
  width: 120px;
}

.annotation-number-hint {
  font-size: 14px;
  color: #6b7280;
}

.annotation-save-row {
  display: flex;
  justify-content: flex-end;
  padding-top: 8px;
  border-top: 1px solid #f3f4f6;
}

.annotation-save-btn {
  min-width: 90px;
}
/* 模型卡片 */
.model-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.model-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px 20px;
}

.model-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.model-status {
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;

  &.connected {
    background: #10b981;
  }
}

.status-text {
  font-size: 12px;
  color: #6b7280;
}

.model-menu {
  cursor: pointer;
  font-size: 18px;
  color: #6b7280;

  &:hover {
    color: #374151;
  }
}

.model-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.model-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.model-url {
  font-size: 13px;
  color: #6b7280;
}

.model-created {
  font-size: 12px;
  color: #9ca3af;
}

.model-info-box {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px 20px;
  font-size: 13px;
  color: #6b7280;
}

.info-title {
  margin: 0 0 8px 0;
}

.info-steps {
  margin: 8px 0;
  padding-left: 20px;
  line-height: 1.8;
}

.info-note {
  margin: 12px 0 0 0;

  a {
    color: #2563eb;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}

.configuration-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
}

.config-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 16px 0;
}

.switch-content {
  margin-left: 12px;
  flex: 1;
}

.switch-title {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

.switch-desc {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
}

.config-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.test-request-dialog {
  .test-request-hint {
    font-size: 13px;
    color: #6b7280;
    margin: 12px 0 16px;
  }

  .test-request-panels {
    display: flex;
    gap: 16px;
  }

  .test-request-panel {
    flex: 1;
    min-width: 0;
  }

  .test-request-panel-title {
    font-size: 14px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 8px;
  }

  .test-request-textarea {
    ::v-deep textarea {
      font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
      font-size: 12px;
      line-height: 1.5;
      background: #f9fafb;
    }
  }
}

.predictions-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: #6b7280;
  font-size: 14px;

  i {
    margin-right: 8px;
    font-size: 18px;
  }
}

.predictions-empty {
  text-align: center;
  padding: 48px 32px;
  background: #eef2ff;
  border: 1px solid #e0e7ff;
  border-radius: 8px;
}

.predictions-empty-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 20px;
  border-radius: 50%;
  background: #c7d2fe;
  display: flex;
  align-items: center;
  justify-content: center;

  i {
    font-size: 26px;
    color: #4f46e5;
  }
}

.predictions-empty-title {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 12px;
}

.predictions-empty-desc {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.6;
  max-width: 480px;
  margin: 0 auto 20px;
}

.predictions-learn-more {
  font-size: 14px;
  color: #4f46e5;
  text-decoration: none;
  font-weight: 500;

  &:hover {
    text-decoration: underline;
  }

  i {
    font-size: 12px;
    margin-left: 4px;
  }
}

.predictions-list-title {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 8px;
}

.predictions-list-desc {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.6;
  margin: 0 0 20px;

  a {
    color: #4f46e5;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}

.predictions-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.predictions-section {
  margin-top: 20px;

  &:first-of-type {
    margin-top: 0;
  }
}

.predictions-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f3f4f6;
}

.prediction-live-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
}

.live-status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;

  &.connected {
    background: #10b981;
    box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.15);
  }

  &.disconnected {
    background: #f59e0b;
    box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.15);
  }
}

.prediction-live-title {
  font-size: 13px;
  color: #6b7280;
}

.prediction-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }
}

.prediction-card-main {
  min-width: 0;
  flex: 1;
}

.prediction-card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 6px;
  word-break: break-all;
}

.prediction-card-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: #6b7280;
}

.prediction-count {
  display: inline-flex;
  align-items: center;

  i {
    margin-right: 4px;
    color: #4f46e5;
  }
}

.prediction-card-menu {
  font-size: 18px;
  color: #6b7280;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;

  &:hover {
    background: #f3f4f6;
    color: #1f2937;
  }
}

.reset-cache-content {
  padding: 8px 4px;
}

.reset-cache-desc {
  font-size: 14px;
  color: #374151;
  line-height: 1.6;
  margin: 0 0 16px;

  strong {
    font-weight: 600;
    color: #111827;
  }
}

.reset-cache-hint {
  font-size: 14px;
  color: #374151;
  margin: 0 0 10px;
}

.reset-cache-input {
  width: 100%;
}

.drop-all-tabs-content {
  padding: 8px 4px;
}

.drop-all-tabs-desc {
  font-size: 14px;
  color: #374151;
  line-height: 1.6;
  margin: 0 0 16px;

  strong {
    font-weight: 600;
    color: #111827;
  }
}

.drop-all-tabs-hint {
  font-size: 14px;
  color: #374151;
  margin: 0 0 10px;
}

.drop-all-tabs-input {
  width: 100%;
}

.delete-project-content {
  padding: 8px 4px;
}

.delete-project-desc {
  font-size: 14px;
  color: #374151;
  line-height: 1.6;
  margin: 0 0 16px;

  strong {
    font-weight: 600;
    color: #111827;
  }
}

.delete-project-hint {
  font-size: 14px;
  color: #374151;
  margin: 0 0 10px;
}

.delete-project-input {
  width: 100%;
}

.header-right {
  display: flex;
  align-items: center;
}

.export-data-content {
  padding: 8px 4px;
}

.export-data-desc {
  font-size: 14px;
  color: #374151;
  margin: 0 0 16px;
}

.export-format-list {
  max-height: 480px;
  overflow-y: auto;
  margin-bottom: 16px;
}

.export-format-item {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: #3b82f6;
    background-color: #f9fafb;
  }

  &.selected {
    border-color: #3b82f6;
    background-color: #eff6ff;
  }

  &.disabled {
    opacity: 0.5;
    cursor: not-allowed;

    &:hover {
      border-color: #e5e7eb;
      background-color: #fff;
    }
  }
}

.export-format-radio {
  margin-right: 12px;
  padding-top: 2px;
}

.export-format-info {
  flex: 1;
  min-width: 0;
}

.export-format-title {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 4px;
}

.export-format-desc {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.5;
  margin-bottom: 6px;
}

.export-format-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.export-format-tag {
  display: inline-block;
  font-size: 11px;
  color: #3b82f6;
  background-color: #eff6ff;
  padding: 2px 8px;
  border-radius: 10px;
}

.export-data-footer {
  font-size: 13px;
  color: #6b7280;
  margin: 0 0 8px;

  a {
    color: #3b82f6;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}

.export-data-tip {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
}
</style>