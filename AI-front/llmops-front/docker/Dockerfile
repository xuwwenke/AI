ARG PLATFORM_SUFFIX
ARG DOCKER_REGISTRY
FROM ${DOCKER_REGISTRY}/public/openresty:1.27.1.2-4-bookworm-v16${PLATFORM_SUFFIX}
# 源码中.conf 结尾，方便编辑，IDE有提示，映射到docker内部为.template结尾
ADD docker/conf/default.template.conf /etc/nginx/templates/default.conf.template
# 前端静态资源
ADD dist/ /app/dist
