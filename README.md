# MCSS-RL
基于Lagrange插值法和RS纠删码的云数据隐藏系统

### 文件夹说明：

- Client：客户端代码
- Server：服务端代码

### 类说明：

**文件块发送部分**

- SendFileServer - 服务器端被动接收由客户端发来的文件块
- SendFileClinet - 客户端主动发送文件块给服务器

**RPC部分**

- RSCalcRPCServer - RPC服务接收
- RSCalcServiceProxy.startClient() - 创建RPC客户端

**RS计算部分**

RSCalcServer - RS计算（服务端）
RSCalcServer.createQuestFileClient - 创建RS计算客户端（请求相应文件块）

