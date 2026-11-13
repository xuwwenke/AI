## 启动示例：
```bash
docker run -d --name data-labeling-service --restart unless-stopped -p 8000:8000 -e LABEL_STUDIO_URL="http://192.168.246.151:5427" -e LABEL_STUDIO_API_KEY="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6ODA5NjkzODI2NiwiaWF0IjoxNzg5NzM4MjY2LCJqdGkiOiJjNWM1NzVkZWQwYWY0OGM3YWNiMGZhZjJhNDMwZTExZSIsInVzZXJfaWQiOiIyIn0.NS9BsF3vOVCy6Yfqe8cMy-ihezppzK8VR-trPZ_Dd_A" docker-registry:81/harbor-ailabx/data-labeling-service:0.1.0
```
> 需先部署label studio 服务，并获取token
