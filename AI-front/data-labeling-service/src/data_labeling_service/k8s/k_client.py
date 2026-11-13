import os
from kubernetes import client, config
from kubernetes.client.rest import ApiException
from kubernetes.config import kube_config, load_incluster_config
from kubernetes.client.models import v1_pod, v1_object_meta, v1_pod_spec, v1_deployment, v1_deployment_spec
from kubernetes.client.models.v1_pod import V1Pod


class K8s():
  def _create_api_client(self, file_path=None):
    """为每个实例创建独立的 ApiClient"""
    client_config = client.Configuration()
    # if not file_path:
    #   file_path = conf.get('CLUSTERS',{}).get(conf.get('ENVIRONMENT'),{}).get('KUBECONFIG','')
    if not file_path:
      file_path = os.getenv('KUBECONFIG', '')
    if file_path and os.path.exists(file_path) and ''.join(open(file_path).readlines()).strip():
      # 加载指定文件的配置
      kube_config.load_kube_config(config_file=file_path, client_configuration=client_config)
    else:
      try:
        # 回退到默认 kubeconfig（KUBECONFIG 环境变量或 ~/.kube/config）
        kube_config.load_kube_config(client_configuration=client_config)
      except Exception:
        # 最后回退到集群内配置
        load_incluster_config(client_configuration=client_config)
    client_config.verify_ssl = False
    return client.ApiClient(configuration=client_config)

  def __init__(self, file_path=None, cluster_name=''):
    # 为每个实例创建独立的 ApiClient
    api_client = self._create_api_client(file_path)
    self.kubeconfig = file_path
    self.cluster_name = cluster_name

    # if not cluster_name and not file_path:
    #   self.cluster_name = conf.get('ENVIRONMENT').lower()
    if not cluster_name and file_path:
      self.cluster_name = file_path.split('/')[-1].replace('-kubeconfig','')

    self.v1 = client.CoreV1Api(api_client)
    self.AppsV1Api = client.AppsV1Api(api_client)
    self.NetworkingV1Api = client.NetworkingV1Api(api_client)
    self.CustomObjectsApi = client.CustomObjectsApi(api_client)
    self.rbacvi = client.RbacAuthorizationV1Api(api_client)
    self.autoscalev2 = client.AutoscalingV2Api(api_client)
    self.autoscalev1 = client.AutoscalingV1Api(api_client)
    self.v1.api_client.configuration.verify_ssl = False  # 只能设置 /usr/local/lib/python3.9/dist-packages/kubernetes/client/configuration.py:   self.verify_ssl= True ---> False
    # self.gpu_resource=conf.get('GPU_RESOURCE',{})
    # self.vgpu_resource = conf.get('VGPU_RESOURCE', {})
    # self.vgpu_drive_type = conf.get("VGPU_DRIVE_TYPE", "vgpu")
    # self.get_gpu = core.get_gpu

  # 创建 deployment
  def create_deployment(self, namespace, name, replicas, labels, command, args, volume_mount,working_dir,
      node_selector, resource_memory, resource_cpu, resource_gpu, image_pull_policy,
      image_pull_secrets, image, hostAliases, env, privileged, accounts, username,
      ports,scheduler_name='default-scheduler', health=None, annotations=None,sidecar=None):
    if labels is None:
      labels = {}
    if annotations is None:
      annotations = {}
    if sidecar is None:
      sidecar = []
    if command is None:
      command = []
    if args is None:
      args = []
    if volume_mount is None:
      volume_mount = []
    if node_selector is None:
      node_selector = {}
    if hostAliases is None:
      hostAliases = []
    if env is None:
      env = []
    if ports is None:
      ports = []
    if image_pull_secrets is None:
      image_pull_secrets = []
    if accounts is None:
      accounts = ""
    if privileged is None:
      privileged = False

    # 构造环境变量
    container_env = []
    if env and type(env) == str:
      envs = re.split('\r|\n', env)
      # envs = [env.split('=') for env in envs if env and len(env.split('=')) == 2]
      envs = [[env[:env.index('=')], env[env.index('=') + 1:]] for env in envs if env and '=' in env]
      container_env = [client.V1EnvVar(name=env[0], value=env[1]) for env in envs]
    if env and type(env) == dict:
      container_env = [client.V1EnvVar(name=str(env_key), value=str(env[env_key])) for env_key in env]
    
    # 构建 ContainerPort
    container_ports = []
    if isinstance(ports, dict):
      for port_name, port_value in ports.items():
        if isinstance(port_value, dict):
          container_ports.append(
            client.V1ContainerPort(
              name=port_value.get("name", port_name),
              container_port=int(
                port_value.get(
                  "containerPort",
                  port_value.get("port")
                )
              ),
              protocol=port_value.get("protocol", "TCP")
            )
          )
        else:
          container_ports.append(
            client.V1ContainerPort(
              name=str(port_name),
              container_port=int(port_value),
              protocol="TCP"
            )
          )
    elif isinstance(ports, list):
      for port in ports:
        # 例如：
        # ports=[80, 8080]
        if isinstance(port, int):
          container_ports.append(
            client.V1ContainerPort(
              container_port=port,
              protocol="TCP"
            )
          )

        # 例如：
        # ports=[
        #   {"name": "http", "containerPort": 8080}
        # ]
        elif isinstance(port, dict):
          container_ports.append(
            client.V1ContainerPort(
              name=port.get("name"),
              container_port=int(port.get("containerPort", port.get("port"))),
              protocol=port.get("protocol", "TCP")
            )
          )
        elif isinstance(port, client.V1ContainerPort):
          container_ports.append(port)
    
    # 构建资源限制
    resources = {}
    requests = {}
    limits = {}

    if resource_cpu:
      requests["cpu"] = str(resource_cpu)
      limits["cpu"] = str(resource_cpu)
    if resource_memory:
      requests["memory"] = str(resource_memory)
      limits["memory"] = str(resource_memory)

    # GPU
    if resource_gpu:
      # NVIDIA GPU
      requests["nvidia.com/gpu"] = str(resource_gpu)
      limits["nvidia.com/gpu"] = str(resource_gpu)
    if requests:
      resources["requests"] = requests
    if limits:
      resources["limits"] = limits

    # pvc
    container_volume_mounts = []
    if isinstance(volume_mount, list):
      for item in volume_mount:
        if isinstance(item, client.V1VolumeMount):
          container_volume_mounts.append(item)
        elif isinstance(item, dict):
          container_volume_mounts.append(
            client.V1VolumeMount(
              name=item.get("name"),
              mount_path=item.get("mountPath", item.get("mount_path")),
              read_only=item.get("readOnly", item.get("read_only", False)),
              sub_path=item.get("subPath", item.get("sub_path"))
            )
          )

    elif isinstance(volume_mount, dict):
      for volume_name, mount_path in volume_mount.items():
        container_volume_mounts.append(
          client.V1VolumeMount(
            name=volume_name,
            mount_path=mount_path
          )
        )

    # 健康检查
    liveness_probe = None
    readiness_probe = None
    startup_probe = None

    if health:
      def build_probe(probe_config):
        if not probe_config:
          return None
        # HTTP
        http_get = probe_config.get("httpGet")
        if http_get:
          http_action = client.V1HTTPGetAction(
            path=http_get.get("path", "/"),
            port=http_get.get("port"),
            scheme=http_get.get("scheme", "HTTP"),
            http_headers=[
              client.V1HTTPHeader(
                  name=x.get("name"),
                  value=x.get("value")
              )
              for x in http_get.get("httpHeaders", [])
            ]
          )
        else:
          http_action = None
        # TCP
        tcp_socket = probe_config.get("tcpSocket")
        if tcp_socket:
          tcp_action = client.V1TCPSocketAction(port=tcp_socket.get("port"))
        else:
          tcp_action = None
        # Exec
        exec_config = probe_config.get("exec")
        if exec_config:
          exec_action = client.V1ExecAction(command=exec_config.get("command"))
        else:
          exec_action = None
        return client.V1Probe(
          http_get=http_action,
          tcp_socket=tcp_action,
          _exec=exec_action,
          initial_delay_seconds=probe_config.get("initialDelaySeconds", 10),
          period_seconds=probe_config.get("periodSeconds", 10),
          timeout_seconds=probe_config.get("timeoutSeconds", 1),
          failure_threshold=probe_config.get("failureThreshold", 3),
          success_threshold=probe_config.get("successThreshold", 1)
        )
      if isinstance(health, dict):
        liveness_probe = build_probe(health.get("liveness"))
        readiness_probe = build_probe(health.get("readiness"))
        startup_probe = build_probe(health.get("startup"))
        # 如果传的是：
        #
        # health={
        #     "httpGet": {...}
        # }
        #
        # 默认作为 readinessProbe
        if (not liveness_probe and not readiness_probe and not startup_probe and ("httpGet" in health or "tcpSocket" in health or "exec" in health)):
          readiness_probe = build_probe(health)

    # 主容器
    main_container = client.V1Container(
      name=name,
      image=image,
      image_pull_policy=image_pull_policy or "IfNotPresent",
      command=command if command else None,
      args=args if args else None,
      working_dir=working_dir if working_dir else None,
      env=container_env if container_env else None,
      ports=container_ports if container_ports else None,
      resources=client.V1ResourceRequirements(requests=resources.get("requests"), limits=resources.get("limits")) if resources else None,
      volume_mounts=(container_volume_mounts if container_volume_mounts else None),
      security_context=client.V1SecurityContext(privileged=bool(privileged)) if privileged is not None else None,
      liveness_probe=liveness_probe,
      readiness_probe=readiness_probe,
      startup_probe=startup_probe
    )

    # Sidecar 容器
    containers = [main_container]
    for index, sc in enumerate(sidecar):
      if isinstance(sc, client.V1Container):
        containers.append(sc)
        continue
      if not isinstance(sc, dict):
        continue
      sc_name = sc.get("name", f"{name}-sidecar-{index}")
      sc_image = sc.get("image")
      if not sc_image:
        raise ValueError(f"sidecar[{index}] 必须指定 image")
      sc_command = sc.get("command")
      sc_args = sc.get("args")
      sc_env = []
      sidecar_env = sc.get("env", [])
      if isinstance(sidecar_env, dict):
        for k, v in sidecar_env.items():
          sc_env.append(client.V1EnvVar(name=str(k), value=str(v)))
      elif isinstance(sidecar_env, list):
        for item in sidecar_env:
          if isinstance(item, client.V1EnvVar):
            sc_env.append(item)
          elif isinstance(item, dict):
            sc_env.append(client.V1EnvVar(name=item.get("name"), value=item.get("value")))
      sc_ports = []
      for p in sc.get("ports", []):
        if isinstance(p, int):
          sc_ports.append(
            client.V1ContainerPort(
              container_port=p
            )
          )
        elif isinstance(p, dict):
          sc_ports.append(
            client.V1ContainerPort(
              name=p.get("name"),
              container_port=int(p.get("containerPort", p.get("port"))),
              protocol=p.get("protocol", "TCP")
            )
          )
      sc_volume_mounts = []
      for vm in sc.get("volumeMounts", []):
        if isinstance(vm, client.V1VolumeMount):
          sc_volume_mounts.append(vm)
        elif isinstance(vm, dict):
          sc_volume_mounts.append(
            client.V1VolumeMount(
              name=vm.get("name"),
              mount_path=vm.get("mountPath", vm.get("mount_path")),
              read_only=vm.get("readOnly", vm.get("read_only", False))
            )
          )
      sc_container = client.V1Container(
        name=sc_name,
        image=sc_image,
        image_pull_policy=sc.get("imagePullPolicy", image_pull_policy or "IfNotPresent"),
        command=sc_command,
        args=sc_args,
        working_dir=sc.get("workingDir"),
        env=sc_env if sc_env else None,
        ports=sc_ports if sc_ports else None,
        volume_mounts=(sc_volume_mounts if sc_volume_mounts else None),
        security_context=client.V1SecurityContext(privileged=bool(sc.get("privileged", False)))
      )
      containers.append(sc_container)

    # imagePullSecrets
    pull_secrets = []
    for secret in image_pull_secrets:
      if isinstance(secret, client.V1LocalObjectReference):
        pull_secrets.append(secret)
      elif isinstance(secret, str):
        pull_secrets.append(client.V1LocalObjectReference(name=secret))
      elif isinstance(secret, dict):
        pull_secrets.append(client.V1LocalObjectReference(name=secret.get("name")))

    # hostAliases
    pod_host_aliases = []
    for item in hostAliases:
      if isinstance(item, client.V1HostAlias):
        pod_host_aliases.append(item)
      elif isinstance(item, dict):
        pod_host_aliases.append(
          client.V1HostAlias(ip=item.get("ip"), hostnames=item.get("hostnames", item.get("hostNames", [])))
        )
    
    # Pod Spec
    pod_spec = client.V1PodSpec(
      containers=containers,
      node_selector=node_selector or None,
      scheduler_name=scheduler_name,
      image_pull_secrets=(pull_secrets if pull_secrets else None),
      host_aliases=(pod_host_aliases if pod_host_aliases else None),
      service_account_name=accounts or None,
      restart_policy="Always"
    )

    # Pod Template
    pod_template = client.V1PodTemplateSpec(
      metadata=client.V1ObjectMeta(
        labels=labels
      ),
      spec=pod_spec
    )

    # Deployment Spec
    deployment_spec = client.V1DeploymentSpec(
      replicas=int(replicas),
      selector=client.V1LabelSelector(match_labels=labels),
      template=pod_template
    )

    # Deployment
    deployment = client.V1Deployment(
      api_version = "apps/v1",
      kind = "Deployment",
      metadata = client.V1ObjectMeta(name=name, namespace=namespace, labels=labels, annotations=annotations),
      spec = deployment_spec
    )

    # 创建 deployment
    try:
      self.AppsV1Api.create_namespaced_deployment(namespace, deployment)
      print(f"Deployment {name} created successfully")
    except ApiException as api_e:
      print(f"Error creating deployment {name}: {api_e}")
    except Exception as e:
      print(f"Error creating deployment {name}: {e}")

  # 删除 deployment
  def delete_deployment(self, namespace, name = None, labels = None):
    if name:
      try:
        self.AppsV1Api.delete_namespaced_deployment(name, namespace, grace_period_seconds=0)
        print(f"Deployment {name} deleted successfully")
      except ApiException as api_e:
        if api_e.status != 404:
          print(f"Error deleting deployment {name}: {api_e}")
      except Exception as e:
        print(f"Error deleting deployment {name}: {e}")
    elif labels:
      try:
        label_arr = ["%s=%s" % (key, labels[key]) for key in labels]
        label_str = ",".join(label_arr)
        deploys = self.AppsV1Api.list_namespaced_deployment(namespace, label_selector=label_str).items or []
        for deploy in deploys:
          self.AppsV1Api.delete_namespaced_deployment(name=deploy.metadata.name, namespace=namespace, grace_period_seconds=0)
      except ApiException as api_e:
        if api_e.status != 404:
          print(f"Error deleting deployments with labels {label_str}: {api_e}")
      except Exception as e:
        print(f"Error deleting deployments with labels {label_str}: {e}")

  def create_service(self, namespace, name, ports, selector, service_type='ClusterIP', external_ip=None, annotations=None, load_balancer_ip=None, external_traffic_policy=None, disable_load_balancer=False):
    svc_metadata = v1_object_meta.V1ObjectMeta(name=name, namespace=namespace, labels=selector, annotations=annotations)
    service_ports = []
    for index, port in enumerate(ports):
      if type(port) == list and len(port) > 1:
        service_ports.append(client.V1ServicePort(name='http%s'%index, node_port=int(port[0]) if service_type=='NodePort' else None, port=int(port[0]), protocol='TCP', target_port=int(port[1])))
      else:
        service_ports.append(client.V1ServicePort(name='http%s' % index, node_port=int(port) if service_type=='NodePort' else None, port=int(port), protocol='TCP', target_port=int(port)))
    svc_spec = client.V1ServiceSpec(
      cluster_ip='None' if disable_load_balancer else None,
      ports = service_ports,
      selector = selector,
      type = service_type,
      external_ips = external_ip,
      load_balancer_ip = load_balancer_ip,
      external_traffic_policy = external_traffic_policy,
    )
    service = client.V1Service(api_version='v1', kind='Service', metadata=svc_metadata, spec=svc_spec)
    try:
      self.v1.read_namespaced_service(name=name, namespace=namespace,_request_timeout=5)
      self.v1.replace_namespaced_service(name=name, namespace=namespace, body=service)
    except ApiException as e:
      if e.status == 404:
        pass
        # print(service)
        service = self.v1.create_namespaced_service(namespace, body=service)

  def delete_service(self, namespace, name = None, labels = None):
    if name:
      try:
        self.v1.delete_namespaced_service(name=name, namespace=namespace, grace_period_seconds=0)
      except ApiException as api_e:
        if api_e.status != 404:
          print(api_e)
      except Exception as e:
        print(e)
    if labels:
      try:
        # 获取具有指定标签的服务
        services = self.v1.list_namespaced_service(namespace=namespace, label_selector=",".join([f"{k}={v}" for k, v in labels.items()])).items or []
        for service in services:
          self.v1.delete_namespaced_service(name=service.metadata.name, namespace=namespace, grace_period_seconds=0)
          print(f"Deleted service: {service.metadata.name} in namespace: {service.metadata.namespace}")
      except ApiException as api_e:
        if api_e.status != 404:
          print(api_e)
      except Exception as e:
        print(e)

  # 创建 Label Studio ML Backend 服务（Deployment + 同名 Service）
  def create_ml_backend(self, namespace, name, image, port=9090, replicas=1,
      labels=None, annotations=None, command=None, args=None, env=None,
      node_selector=None, resource_memory=None, resource_cpu=None, resource_gpu=None,
      image_pull_policy='IfNotPresent', image_pull_secrets=None, volume_mount=None,
      working_dir=None, hostAliases=None, privileged=False, accounts=None,
      scheduler_name='default-scheduler', health=None, service_type='ClusterIP'):
    """创建一个 ML Backend：Deployment 以及与其同名的 Service。"""
    if labels is None:
      labels = {"app": name}

    # 1. 创建 Deployment
    self.create_deployment(
      namespace=namespace,
      name=name,
      replicas=replicas,
      labels=labels,
      annotations=annotations,
      command=command,
      args=args,
      volume_mount=volume_mount,
      working_dir=working_dir,
      node_selector=node_selector,
      resource_memory=resource_memory,
      resource_cpu=resource_cpu,
      resource_gpu=resource_gpu,
      image_pull_policy=image_pull_policy,
      image_pull_secrets=image_pull_secrets,
      image=image,
      hostAliases=hostAliases,
      env=env,
      privileged=privileged,
      accounts=accounts,
      username=None,
      ports=[{"name": "http", "containerPort": port, "protocol": "TCP"}],
      scheduler_name=scheduler_name,
      health=health
    )

    # 2. 创建同名 Service
    self.create_service(
      namespace=namespace,
      name=name,
      ports=[port],
      selector=labels,
      service_type=service_type
    )
    print(f"ML Backend {name} created successfully")

  # 删除 ML Backend（同时删除 Deployment 与同名 Service）
  def delete_ml_backend(self, namespace, name):
    """删除 ML Backend：同时删除 Deployment 与同名 Service。"""
    self.delete_deployment(namespace=namespace, name=name)
    self.delete_service(namespace=namespace, name=name)
    print(f"ML Backend {name} deleted successfully")


_k8s_client = None


def get_k8s_client(file_path=None, cluster_name=''):
  """获取全局复用的 K8s 客户端。"""
  global _k8s_client
  if _k8s_client is None:
    _k8s_client = K8s(file_path=file_path, cluster_name=cluster_name)
  return _k8s_client


if __name__ == '__main__':
  k8s = K8s(file_path="/root/.kube/config")
  k8s.create_deployment(
    namespace="default",
    name="nginx-test",
    replicas=1,
    labels={
      "app": "nginx-test"
    },
    command=None,
    args=None,
    volume_mount=[],
    working_dir=None,
    node_selector={},
    resource_memory="512Mi",
    resource_cpu="500m",
    resource_gpu=None,
    image_pull_policy="IfNotPresent",
    image_pull_secrets=[],
    image="docker-registry:81/public/nginx:1.29.0-alpine",
    hostAliases=[],
    env={
      "TEST_ENV": "hello"
    },
    privileged=False,
    accounts=None,
    username=None,
    ports=[
      {
        "name": "http",
        "containerPort": 80,
        "protocol": "TCP"
      }
    ],
    scheduler_name="default-scheduler"
)

