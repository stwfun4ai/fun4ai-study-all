---
typora-root-url: images
---

runoob Git教程：https://www.runoob.com/git/git-basic-operations.html

# 1 环境配置

## 1.1 安装

官网：https://git-scm.com/download/win

镜像：https://npm.taobao.org/mirrors/git-for-windows/

​	编辑器选择notepad++ 其他直接下一步。

> ==**设置用户名和邮箱**==

```bash
git config --global user.name "fun4ai"
git config --global user.email "7lfun4ai@gmail.com"
```



## 1.2 卸载

​	反安装，移除git环境变量



# 2 基本理论

> 工作区域

![image-20201116174658548](git workspace.png)

![''](git-command.jpg)

- workspace：工作区
- staging area：暂存区/缓存区
- local repository：或本地仓库
- remote repository：远程仓库

# Git command

<img src="git commands.jpg" />

# 获取 Git 仓库

1. 在现有目录中初始化仓库: 进入项目目录运行 `git init` 命令,该命令将创建一个名为 `.git` 的子目录。
2. 从一个服务器克隆一个现有的 Git 仓库: `git clone [url]` 自定义本地仓库的名字: `git clone [url] directoryname`



# 开源项目修改打包

找`contributing.md`文件会告诉你怎么打包















