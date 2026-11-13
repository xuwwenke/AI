FROM docker-registry:81/public/python:3.12-rc-alpine3.15

WORKDIR /app

COPY requirements.txt .

RUN pip install --no-cache-dir -i https://pypi.tuna.tsinghua.edu.cn/simple -r requirements.txt

COPY . .

EXPOSE 8080

CMD ["uvicorn","main:app","--host","0.0.0.0","--port","8080"]