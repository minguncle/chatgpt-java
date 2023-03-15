## OpenAI Chat GPT API

这是一个基于Java的OpenAI API。它提供了一组REST接口，可用于接入OpenAiIAPI或实现部分proxy，支持同步和流式请求。

### 安装和使用

1. 克隆项目到本地：`git clone https://github.com/minguncle/chatgpt-java.git`

2. 配置API Key和代理

   在`src/main/resources/application.yml`中设置API Key和代理（可选）。

3. 构建项目

   ```bash
   cd OpenAI-Chat-GPT-API/
   mvn clean package
   ```

4. 运行项目

   ```bash
   java -jar target/chat-gpt-1.0.0.jar
   ```

5. 测试API

   - 请参考项目启动后的doc
   
   ~~~bash
   ----------------------------------------------------------
   	Application 'ChatGPT' is running! Access URLs:
   	Local: 		http://localhost:8082
   	External: 	http://192.168.200.1:8082
   	Doc: 	http://192.168.200.1:8082/doc.html
   ----------------------------------------------------------
   ~~~

6. 补充说明

   - 本服务可以用作[chatgpt-web](https://github.com/Chanzhaoyu/chatgpt-web)项目的代理
   - 配置[chatgpt-web](https://github.com/Chanzhaoyu/chatgpt-web)

   ~~~yaml
   OPENAI_API_BASE_URL=本服务地址
   ~~~

   - 参照guide运行即可

### REST接口

- 项目主要提供了两套接口：
  - /v1/chat/completions
  - /chat
  - /chat/stream
- 其中completions与官方接口保持一致，支持通过stream控制流式输出，可以用作proxy
- chat接口可以自定义实现，根据需要进行二次开发

### 项目主要结构

- `src/main/java/com/minguncle/chatgpt/controller/`：REST接口控制器
- `src/main/java/com/minguncle/chatgpt/service/`：服务接口和服务实现类
- `src/main/java/com/minguncle/chatgpt/utils/api/`：OpenAI API接口的Java实现
- `src/main/java/com/minguncle/chatgpt/event`：SSE回调实现，可以按需定义实现增强业务
- `src/main/java/com/minguncle/chatgpt/aop`：日志增强实现
- `src/main/java/com/minguncle/chatgpt/config`：项目配置

### 技术栈

- Java 8
- Spring Boot 2.7.9
- OpenAI API
- Maven

