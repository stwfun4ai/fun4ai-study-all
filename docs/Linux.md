# **1 常见目录**

| 目录        | 说明                                                         |
| ----------- | ------------------------------------------------------------ |
| `/bin`      | 存放二进制可执行文件(ls,cat,mkdir等)，**常用命令**一般都在这里。 |
| `/etc`      | 存放**系统管理和配置**文件                                   |
| `/home`     | **存放所有用户文件的根目录**，是用户主目录的基点，比如用户user的主目录就是/home/user，可以用~user表示 |
| `/usr`      | Unix System Resource用于存放系统应用程序，比较重要的目录/usr/local **本地系统管理员软件安装目录（安装系统级的应用）**。这是最庞大的目录，要用到的应用程序和文件几乎都在这个目录。<br />/usr/x11r6 存放x window的目录<br />/usr/bin 众多的应用程序<br /> /usr/sbin 超级用户的一些管理程序<br /> /usr/doc linux文档<br /> /usr/include linux下开发和编译应用程序所需要的头文件<br /> /usr/lib 常用的动态链接库和软件包的配置文件<br /> /usr/man 帮助文档<br /> /usr/src 源代码，linux内核的源代码就放在/usr/src/linux里 <br />/usr/local/bin 本地增加的命令 <br />/usr/local/lib 本地增加的库 |
| `/opt`      | **额外安装的可选应用程序包所放置的位置。**一般情况下，我们可以把tomcat等都安装到这里。 |
| /proc       | 虚拟文件系统目录，是系统内存的映射。可直接访问这个目录来获取系统信息。 |
| `/root`     | **超级用户（系统管理员）的主目录（特权阶级^o^）**            |
| /sbin       | 存放二进制可执行文件，只有root才能访问。这里存放的是系统管理员使用的系统级别的管理命令和程序。如ifconfig等。 |
| /dev        | 用于存放设备文件。                                           |
| /mnt        | 系统管理员安装临时文件系统的安装点，系统提供这个目录是让用户临时挂载其他的文件系统。 |
| /boot       | 存放用于系统引导时使用的各种文件                             |
| /lib        | 存放跟文件系统中的程序运行所需要的共享库及内核模块。共享库又叫动态链接共享库，作用类似windows里的.dll文件，存放了根文件系统程序运行所需的共享文件。 |
| /tmp        | 用于存放各种临时文件，是公用的临时文件存储点。               |
| `/var`      | 用于存放运行时需要改变数据的文件，也是某些大文件的溢出区，比方说各种**服务的日志文件（系统启动日志等。）**等。 |
| /lost+found | 这个目录平时是空的，系统非正常关机而留下“无家可归”的文件（windows下叫什么.chk）就在这里 |



# 2 命令

## 常用指令



`ls`　        		显示文件或目录			（list files）命令用于显示指定工作目录下之内容（列出目前工作目录所含之文件及子目录)。
     -l          	列出文件详细信息l(list)
     -a         	列出当前目录下所有文件及目录，包括隐藏的a(all)		显示所有文件及目录 (ls内定将文件名或目录名称开头为"."的视为隐藏档，不会列出)

​	lscpu 	cpu

​	lsmem	free	内存

​	df -h	fdisk -l	磁盘	

`mkdir`         		创建目录					（make directory）命令用于创建目录。
     -p         	创建目录，若无父目录，则创建p(parent)	mkdir -p runoob2/test
`cd`             		切换目录				（change directory）命令用于切换当前工作目录。
`touch`        		创建空文件
echo          		创建带有内容的文件。
cat            		查看文件内容			（concatenate）命令用于连接文件并打印到标准输出设备上
cp             		拷贝					（copy file）命令主要用于复制文件或目录。

mv            		（move file）命令用来为文件或目录改名、或将文件或目录移入其它位置。
rm            		（remove）命令用于删除一个文件或者目录。
     -r           	递归删除，可删除子目录及文件
     -f            	强制删除
find           		在文件系统中搜索某文件
wc            		默认统计文本中行数、字数、字节数 （-l -w -c/-bytes/-chars）
grep         		在文本文件中查找某个字符串
rmdir        		删除空目录
tree          		树形结构显示目录下的所有文件，包括子目录里的文件。需要安装tree包
pwd          		（print work directory） 命令用于显示工作目录。
ln             		创建链接文件	(link files)命令是一个非常重要命令，它的功能是为某一个文件在另外一个位置建立一个同步的链接。
more、less    		分页显示文本文件内容
head、tail     		显示文件头、尾内容
ctrl+alt+F1   		命令行全屏模式

- nc	所做的就是在两台电脑之间建立链接并返回两个数据流，能建立一个服务器，传输文件，与朋友聊天，传输流媒体或者用它作为其它协议的独立客户端。

## 系统管理命令

stat            显示指定文件的详细信息，比ls更详细
who           显示在线登陆用户
whoami      显示当前操作用户
hostname   显示主机名
uname        显示系统简要信息
     -a          显示系统完整信息
top             动态显示当前耗费资源最多进程信息
ps               显示瞬间进程状态 

​	-aux	
​     -ef         显示系统常驻进程
du              查看目录大小 du -h /home带有单位显示目录信息
df               查看磁盘大小 df -h 带有单位显示磁盘信息
ifconfig       查看网络情况
ping           测试网络连通
`netstat`       显示网络状态信息	-natp
`man`          显示命令手册 manual（如系统调用 man 2 socket）

​			yum -y install man-pages
clear           清屏
alias            对命令重命名 如：alias showmeit=”ps aux” ，另外解除使用unaliax showmeit
kill              杀死进程，可以先用 ps 或 top 命令查看进程的id，然后再用kill命令杀死进程。

## 打包压缩相关命令

gzip	bzip2

tar:                打包压缩

​     -c             归档文件

​     -x             解压缩文件

​     -z             gzip压缩文件

​     -j             bzip2压缩文件

​     -v            显示压缩或解压缩过程 v(view)

​     -f             使用档名

例：

`tar -cvf /home/abc.tar /home/abc`              只打包，不压缩

`tar -zcvf /home/abc.tar.gz /home/abc`        打包，并用gzip压缩

`tar -jcvf /home/abc.tar.bz2 /home/abc`       打包，并用bzip2压缩



如果想解压缩，就直接替换上面的命令  tar -cvf  / tar -zcvf  / tar -jcvf 中的“c” 换成“x” 就可以了。

`tar -xvf /home/abc.tar ` 

`tar -zxvf /home/abc.tar.gz` 

`tar -jxvf /home/abc.tar.bz2 ` 



## 关机/重启机器

- shutdown
  - -r              关机重启
  -  -h             关机不重启
  - now          立刻关机
- halt                关机
- reboot           重启

## Linux管道

将一个命令的标准输出作为另一个命令的标准输入。也就是把几个命令组合起来使用，后一个命令处理前一个命令的输出结果。
例：grep -r “close” /home/* | more       在home目录下所有文件中查找，包括close的文件，并分页输出。

| `&`  | 表示任务在后台执行，如要在后台运行redis-server,则有 `redis-server &` |
| :--: | ------------------------------------------------------------ |
| `&&` | 表示前一条命令执行成功时，才执行后一条命令 ，如 `echo '1‘ && echo '2'` |
| `|`  | 表示管道，上一条命令的输出，作为下一条命令参数，如 `echo 'yes' | wc -l` |
| `||` | 表示上一条命令执行失败后，才执行下一条命令，如 `cat nofile || echo "fail"` |
| `;`  | 表示顺序执行，命令之间没有任何逻辑联系。如`command1;command2;command3` |

  

## Linux软件包管理

dpkg (Debian Package)管理工具，软件包名以.deb后缀。这种方法适合系统不能联网的情况下。
比如安装tree命令的安装包，先将tree.deb传到Linux系统中。再使用如下命令安装。
sudo dpkg -i tree_1.5.3-1_i386.deb         安装软件
sudo dpkg -r tree                                     卸载软件

注：将tree.deb传到Linux系统中，有多种方式。VMwareTool，使用挂载方式；使用winSCP工具等；
APT（Advanced Packaging Tool）高级软件工具。这种方法适合系统能够连接互联网的情况。

依然以tree为例
sudo apt-get install tree                  安装tree
sudo apt-get remove tree              卸载tree
sudo apt-get update                      更新软件
sudo apt-get upgrade        

将.rpm文件转为.deb文件

.rpm为RedHat使用的软件格式。在Ubuntu下不能直接使用，所以需要转换一下。
sudo alien filename.rpm

## vim使用

vim三种模式：命令模式、插入模式、编辑模式。使用 Esc 或 i 或 : 来切换模式。

命令模式下：

|      `:q`       | 退出                                                 |
| :-------------: | ---------------------------------------------------- |
|      `:q!`      | 强制退出                                             |
|      `:wq`      | 保存并退出                                           |
|  `:set number`  | 显示行号                                             |
| `:set nonumber` | 隐藏行号                                             |
|    `/apache`    | 在文档中查找字符apache，按n跳到下一个，shift+n上一个 |
|      `yyp`      | 复制光标所在行，并粘贴                               |

h(左移一个字符←)、j(下一行↓)、k(上一行↑)、l(右移一个字符→)



## 用户及用户组管理

/etc/passwd      存储用户账号
/etc/group        存储组账号
/etc/shadow      存储用户账号的密码
/etc/gshadow    存储用户组账号的密码
useradd user      添加用户
userdel user       删除用户
groupadd user   添加组用户
groupdel user    删除组用户
passwd root      给用户root设置密码
su root             临时提权到root用户
su – root           切换到root用户
/etc/profile        系统环境变量
bash_profile      用户环境变量
.bashrc             用户环境变量
su user             切换用户，加载配置文件.bashrc
su – user           切换用户，加载配置文件/etc/profile ，加载bash_profile

更改文件的用户及用户组

sudo chown [-R递归] owner[:group] {File|Directory}
要想切换文件所属的用户及组。可以使用命令。
sudo chown root:root rarlinux-x64-5.1.b3.tar.gz

## 文件权限管理

三种基本权限

R          读          数值表示为4

W         写          数值表示为2

X          可执行   数值表示为1

更改权限

sudo chmod [u所属用户  g所属组  o其他用户  a所有用户]  [+增加权限  -减少权限]  [r  w  x]   目录名

例如：有一个文件filename，权限为“-rw-r—-x” ,将权限值改为”-rwxrw-r-x”，用数值表示为765

sudo chmod u+x g+w o+r  filename

上面的例子可以用数值表示

sudo chmod 765 filename



# 3 环境安装

​	安装软件一般有三种方式：

- **==rpm==**（jdk）
- **==解压缩==** （tomcat）
- **==yum在线安装==**（docker）

## 3.1 rpm

**JDK安装**

1、下载JDK rpm

​	官网：https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html

​	华为镜像：https://mirrors.huaweicloud.com/java/jdk/

2、安装java环境

```bash
# 检测当前系统是否存在Java环境
java -version
# 如果存在需要先卸载
rpm -qa|grep jdk	# 检测JDK版本信息
rpm -e --nodeps xxx	# xxx代表上一步检测结果

#卸载完毕后安装
rpm -ivh rpm包

#配置环境变量
```

==rpm安装不需要配置环境变量，解压方式需要！==

配置环境变量`/etc/profile`，在文件末尾添加

```bash
export JAVA_HOME=/usr/java/jdk1.8.0_202-amd64
export PATH=$PATH:$JAVA_HOME/bin:$JAVA_HOME/jre/bin
export CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
```

让配置文件生效！`source /etc/profile`

3、确保Linux的防火墙端口是开启的。如果是阿里云需要保证安全组策略端口开放。

```bash
# 查看firewall服务状态
systemctl status firewalld

# 开始、重启、关闭 firewalld.service服务
service firewalld start
service firewalld restart
service firewalld stop

# 查看防火墙规则
firewall-cmd --list-all		#查看全部信息
firewall-cmd --list-ports 	#查看端口信息

# 开放端口
# --zone 作用域
# --add-port 格式：端口/通讯协议
# --permanent永久生效，不添加则重启失效
firewall-cmd --zone=public --add-port=80/tcp --permanent
systemctl restart firewalld.service
```



## 3.2 解压缩

**Tomcat安装**

1、下载tomcat。官网下载apache-tomcat-9.0.39.tar.gz

2、解压文件

```bash
tar -zxvf apache-tomcat-9.0.39.tar.gz
```

3、启动tomcat测试。

```bash
./startup.sh	# 执行
./shutdown.sh	# 停止
```

上传项目购买域名，备案解析。

域名解析后，如果端口是80-http或者443-https可以直接访问，如果是其他端口就需要Apache或者Nginx做一下反向代理，配置文件即可。

## 3.2 yum在线安装

**docker安装**具体详见[Docker](Docker.md)

## 3.3 实用工具

```bash
yum -y install man-pages
yum -y install nc
```







# 4 五种I/O模型

- **阻塞IO模型（bloking IO）**
- **非阻塞IO模型（non-blocking IO）**
- **IO多路复用模型（multiplexing IO）**
- **信号驱动IO模型（signal-driven IO）**
- **异步IO模型（asynchronous IO）**

<img src="D:\Java\document\study\images\io理论实质.jpg" style="zoom:200%;" />

## 4.1 基本概念

### 4.1.1 系统调用函数

**recvfrom**
Linux系统提供给用户用于接收网络IO的系统接口。**从套接字上接收一个消息**，可同时应用于面向连接和无连接的套接字。

如果此系统调用返回值<0，并且 errno为EWOULDBLOCK或EAGAIN（套接字已标记为非阻塞，而接收操作被阻塞或者接收超时 ）时，连接正常，**阻塞**接收数据（这很关键，前4种IO模型都设计此系统调用）。

**select**
select系统调用允许程序同时在多个底层文件描述符上，等待输入的到达或输出的完成。以**数组**形式存储文件描述符，64位机器默认**2048**个。当有数据准备好时，无法感知具体是哪个流OK了，所以需要一个一个的遍历，函数的时间复杂度为**O(n)**。

**poll**
以**链表**形式存储文件描述符，没有长度限制。本质与select相同，函数的时间复杂度也为**O(n)**。

**epoll**
是基于事件驱动的，如果某个流准备好了，会以事件通知，知道具体是哪个流，因此不需要遍历，函数的时间复杂度为**O(1)**。

**sigaction**
用于设置对信号的处理方式，也可检验对某信号的预设处理方式。Linux使用**SIGIO信号**来实现IO异步通知机制。

### **4.1.2 同步&异步**

同步和异步是针对应用程序和内核交互而言的，也可理解为被**被调用者（操作系统）**的角度来说。

- 同步是用户进程触发IO操作并等待或轮询的去查看是否就绪
- 异步是指用户进程触发IO操作以后便开始做自己的事情，而当IO操作已经完成的时候会得到IO完成的通知，需要CPU支持。

### **4.1.3 阻塞&非阻塞**

阻塞和非阻塞是针对于进程在访问数据的时候，也可理解为**调用者（程序）**角度来说。根据IO操作的就绪状态来采取的不同的方式。
阻塞方式下读取或写入方法将一直等待，而非阻塞方式下读取或写入方法会立即返回一个状态值。



## 4.2 阻塞I/O模型

 **bloking IO**

学习过操作系统的知识后，可以知道：不管是网络IO还是磁盘IO，对于读操作而言，都是等到网络的某个数据分组到达后/数据**准备好**后，将数据**拷贝到内核空间的缓冲区中**，再从内核空间**拷贝到用户空间的缓冲区**。

阻塞IO的执行过程是进程进行**系统调用**，**等待内核**将数据准备好并复制到用户态缓冲区后，进程**放弃使用CPU**并**一直阻塞**在此，直到数据准备好。

![输入图片说明](D:\Java\document\study\images\blocking io.png)



## 4.3 非阻塞IO模型 

**non-blocking IO**

每次应用程序**询问内核**是否有数据准备好。如果就绪，就进行**拷贝**操作；如果未就绪，就**不阻塞程序**，内核直接返回未就绪的返回值，等待用户程序下一个轮询。

大致经历两个阶段：

- **等待数据阶段**：==未阻塞==， 用户进程需要盲等，不停的去轮询内核。
- **数据复制阶段**：==阻塞==，此时进行数据复制。

在这两个阶段中，用户进程只有在数据复制阶段被阻塞了，而等待数据阶段没有阻塞，但是用户进程需要盲等，不停地轮询内核，看数据是否准备好。

![输入图片说明](D:\Java\document\study\images\non-blocking io.png)





## 4.4 IO多路复用模型

**multiplexing IO**

多路复用一般都是用于网络IO，服务端与多个客户端的建立连接。

相比于阻塞IO模型，多路复用只是多了一个**select/poll/epoll函数**。select函数会不断地轮询自己所负责的文件描述符/套接字的到达状态，当某个套接字就绪时，就通知用户进程。

**当用户进程调用了select，那么整个进程会被block**，而同时，kernel会“监视”所有select负责的socket，**当任何一个socket中的数据准备好了，select就会返回**。这个时候用户进程再调用read操作，将数据从kernel拷贝到用户进程。

> 多路复用的特点是**通过一种机制一个进程能同时等待IO文件描述符**，内核监视这些文件描述符（套接字描述符），其中的任意一个进入读就绪状态，select， poll，epoll函数就可以返回。对于监视的方式，又可以分为 select， poll， epoll三种方式。

上面的图和blocking IO的图其实并没有太大的不同，事实上，还更差一些。**因为这里需要使用两个system call (select 和 recvfrom)，而blocking IO只调用了一个system call (recvfrom)**。但是，**用select的优势在于它可以同时处理多个connection**。



select负责**轮询等待**，recvfrom负责**拷贝**。当用户进程调用该select，select会监听所有注册好的IO，如果所有IO都没注册好，调用进程就阻塞。

对于客户端来说，一般**感受不到阻塞**，因为请求来了，可以用放到线程池里执行；但对于执行select的操作系统而言，是阻塞的，需要阻塞地**等待某个套接字变为可读**。



==**IO多路复用其实是阻塞在select、poll、epoll这样的系统调用之上，而没有阻塞在真正的I/O系统调用如recvfrom之上。复用的是执行select，poll，epoll的线程。**==



![输入图片说明](D:\Java\document\study\images\multiplexing io.png)

## 4.5 信号驱动IO模型

**signal-driven IO**

信号驱动式I/O：首先我们允许Socket进行信号驱动IO,并安装一个信号处理函数，进程继续运行并不阻塞。当数据准备好时，进程会收到一个SIGIO信号，可以在信号处理函数中调用I/O操作函数处理数据。

![输入图片说明](D:\Java\document\study\images\signal-driven IO.png)

## 4.6 异步IO模型

**asynchronous IO**

相对于同步IO，异步IO不是顺序执行。用户进程进行aio_read系统调用之后，无论内核数据是否准备好，都会直接返回给用户进程，然后用户态进程可以去做别的事情。等到socket数据准备好了，内核直接复制数据给进程，然后从内核向进程发送通知。**IO两个阶段，进程都是非阻塞的。**



![输入图片说明](D:\Java\document\study\images\asynchronous IO.png)



## 4.7 买票举例

故事情节为：老李去买火车票，三天后买到一张退票。参演人员（老李，黄牛，售票员，快递员），往返车站耗费1小时。

**1.阻塞I/O模型**

老李去火车站买票，排队三天买到一张退票。

耗费：在车站吃喝拉撒睡 3天，其他事一件没干。

**2.非阻塞I/O模型**

老李去火车站买票，隔12小时去火车站问有没有退票，三天后买到一张票。

耗费：往返车站6次，路上6小时，其他时间做了好多事。

**3.I/O复用模型**

- **select/poll**

老李去火车站买票，委托黄牛，然后每隔6小时电话黄牛询问，黄牛三天内买到票，然后老李去火车站交钱领票。 

耗费：往返车站2次，路上2小时，黄牛手续费100元，打电话17次

- **epoll**

老李去火车站买票，委托黄牛，黄牛买到后即通知老李去领，然后老李去火车站交钱领票。 

耗费：往返车站2次，路上2小时，黄牛手续费100元，无需打电话

**4.信号驱动I/O模型**

老李去火车站买票，给售票员留下电话，有票后，售票员电话通知老李，然后老李去火车站交钱领票。 

耗费：往返车站2次，路上2小时，免黄牛费100元，无需打电话

**5.异步I/O模型**

老李去火车站买票，给售票员留下电话，有票后，售票员电话通知老李并快递送票上门。 

耗费：往返车站1次，路上1小时，免黄牛费100元，无需打电话



1同2的区别是：自己轮询

2同3的区别是：委托黄牛

3同4的区别是：电话代替黄牛

4同5的区别是：电话通知是自取还是送票上门

## 4.8 总结

![输入图片说明](D:\Java\document\study\images\5种io比较.png)



# 5 epoll

## 概念

epoll是一种I/O事件通知机制，是linux 内核实现IO多路复用的一个实现。
IO多路复用是指，在一个操作里同时监听多个输入输出源，在其中一个或多个输入输出源可用的时候返回，然后对其的进行读写操作。

## I/O

输入输出(input/output)的对象可以是文件(file)， 网络(socket)，进程之间的管道(pipe)。在linux系统中，都用文件描述符(fd)来表示。

## 事件

- 可读事件，当文件描述符关联的内核读缓冲区可读，则触发可读事件。(可读：内核缓冲区非空，有数据可以读取)
- 可写事件，当文件描述符关联的内核写缓冲区可写，则触发可写事件。(可写：内核缓冲区不满，有空闲空间可以写入）

## 通知机制

通知机制，就是当事件发生的时候，则主动通知。通知机制的反面，就是轮询机制。

## epoll的通俗解释

结合以上三条，epoll的通俗解释是一种当文件描述符的内核缓冲区非空的时候，发出可读信号进行通知，当写缓冲区不满的时候，发出可写信号通知的机制。

## API详解

epoll的核心是3个API，核心数据结构是：1个红黑树和1个链表

![image-20201217221908687](D:\Java\document\study\images\image-20201217221908687.png)

### int epoll_create(int size)

​		==内核会产生一个epoll 实例数据结构并返回一个文件描述符，这个特殊的描述符就是epoll实例的句柄，后面的两个接口都以它为中心（即epfd形参）。==

​		size参数表示所要监视文件描述符的最大值，不过在后来的Linux版本中已经被弃用（同时，size不要传0，会报invalid argument错误）

### int epoll_ctl(int epfd， int op， int fd， struct epoll_event *event)

​		==将被监听的描述符添加到红黑树或从红黑树中删除或者对监听事件进行修改==

```c
typedef union epoll_data {
    void *ptr; /* 指向用户自定义数据 */
    int fd; /* 注册的文件描述符 */
    uint32_t u32; /* 32-bit integer */
    uint64_t u64; /* 64-bit integer */
} epoll_data_t;

struct epoll_event {
    uint32_t events; /* 描述epoll事件 */
    epoll_data_t data; /* 见上面的结构体 */
};
```

对于需要监视的文件描述符集合，epoll_ctl对红黑树进行管理，红黑树中每个成员由描述符值和所要监控的文件描述符指向的文件表项的引用等组成。

op参数说明操作类型：

- EPOLL_CTL_ADD：向interest list添加一个需要监视的描述符
- EPOLL_CTL_DEL：从interest list中删除一个描述符
- EPOLL_CTL_MOD：修改interest list中一个描述符

struct epoll_event结构描述一个文件描述符的epoll行为。在使用epoll_wait函数返回处于ready状态的描述符列表时，

- data域是唯一能给出描述符信息的字段，所以在调用epoll_ctl加入一个需要监测的描述符时，一定要在此域写入描述符相关信息
- events域是bit mask，描述一组epoll事件，在epoll_ctl调用中解释为：描述符所期望的epoll事件，可多选。

常用的epoll事件描述如下：

- EPOLLIN：描述符处于可读状态
- EPOLLOUT：描述符处于可写状态
- EPOLLET：将epoll event通知模式设置成edge triggered
- EPOLLONESHOT：第一次进行通知，之后不再监测
- EPOLLHUP：本端描述符产生一个挂断事件，默认监测事件
- EPOLLRDHUP：对端描述符产生一个挂断事件
- EPOLLPRI：由带外数据触发
- EPOLLERR：描述符产生错误时触发，默认检测事件

### int epoll_wait(int epfd， struct epoll_event *events， int maxevents， int timeout)

​		==等待epoll事件从epoll实例中发生， 并返回事件以及对应文件描述符==

- 阻塞等待注册的事件发生，返回事件的数目，并将触发的事件写入events数组中。
- events: 用来记录被触发的events，其大小应该和maxevents一致
- maxevents: 返回的events的最大个数

处于ready状态的那些文件描述符会被复制进ready list中，epoll_wait用于向用户进程返回ready list。events和maxevents两个参数描述一个由用户分配的struct epoll event数组，调用返回时，内核将ready list复制到这个数组中，并将实际复制的个数作为返回值。注意，如果ready list比maxevents长，则只能复制前maxevents个成员；反之，则能够完全复制ready list。
另外，struct epoll event结构中的events域在这里的解释是：在被监测的文件描述符上实际发生的事件。
参数timeout描述在函数调用中阻塞时间上限，单位是ms：

- timeout = -1表示调用将一直阻塞，直到有文件描述符进入ready状态或者捕获到信号才返回；
- timeout = 0用于非阻塞检测是否有描述符处于ready状态，不管结果怎么样，调用都立即返回；
- timeout > 0表示调用将最多持续timeout时间，如果期间有检测对象变为ready状态或者捕获到信号则返回，否则直到超时。

## 两种触发方式

epoll监控多个文件描述符的I/O事件。epoll支持边缘触发(edge trigger，ET)或水平触发（level trigger，LT)，通过epoll_wait等待I/O事件，如果当前没有可用的事件则阻塞调用线程。

> select和poll只支持LT工作模式，epoll的默认的工作模式是LT模式。

### 水平触发

1. 对于读操作，只要缓冲内容不为空，LT模式返回读就绪。
2. 对于写操作，只要缓冲区还不满，LT模式会返回写就绪。

当被监控的文件描述符上有可读写事件发生时，epoll_wait()会通知处理程序去读写。如果这次没有把数据一次性全部读写完(如读写缓冲区太小)，那么下次调用 epoll_wait()时，它还会通知你在上没读写完的文件描述符上继续读写，当然如果你一直不去读写，它会一直通知你。如果系统中有大量你不需要读写的就绪文件描述符，而它们每次都会返回，这样会大大降低处理程序检索自己关心的就绪文件描述符的效率。

### 边缘触发

- 对于读操作

1. 当缓冲区由不可读变为可读的时候，即缓冲区由空变为不空的时候。
2. 当有新数据到达时，即缓冲区中的待读数据变多的时候。
3. 当缓冲区有数据可读，且应用进程对相应的描述符进行EPOLL_CTL_MOD 修改EPOLLIN事件时。

- 对于写操作

1. 当缓冲区由不可写变为可写时。
2. 当有旧数据被发送走，即缓冲区中的内容变少的时候。
3. 当缓冲区有空间可写，且应用进程对相应的描述符进行EPOLL_CTL_MOD 修改EPOLLOUT事件时。

当被监控的文件描述符上有可读写事件发生时，epoll_wait()会通知处理程序去读写。如果这次没有把数据全部读写完(如读写缓冲区太小)，那么下次调用epoll_wait()时，它不会通知你，也就是它只会通知你一次，直到该文件描述符上出现第二次可读写事件才会通知你。这种模式比水平触发效率高，系统不会充斥大量你不关心的就绪文件描述符。

> 在ET模式下， 缓冲区从不可读变成可读，会唤醒应用进程，缓冲区数据变少的情况，则不会再唤醒应用进程。

举例1：

1. 读缓冲区刚开始是空的
2. 读缓冲区写入2KB数据
3. 水平触发和边缘触发模式此时都会发出可读信号
4. 收到信号通知后，读取了1KB的数据，读缓冲区还剩余1KB数据
5. 水平触发会再次进行通知，而边缘触发不会再进行通知

举例2：（以脉冲的高低电平为例）

- 水平触发：0为无数据，1为有数据。缓冲区有数据则一直为1，则一直触发。
- 边缘触发发：0为无数据，1为有数据，只要在0变到1的上升沿才触发。

> JDK并没有实现边缘触发，Netty重新实现了epoll机制，采用边缘触发方式；另外像Nginx也采用边缘触发。

JDK在Linux已经默认使用epoll方式，但是JDK的epoll采用的是水平触发，而Netty重新实现了epoll机制，采用边缘触发方式，netty epoll transport 暴露了更多的nio没有的配置参数，如 TCP_CORK, SO_REUSEADDR等等；另外像Nginx也采用边缘触发。

## epoll与select、poll的对比

### 1 用户态将文件描述符传入内核的方式

- select：创建3个文件描述符集并拷贝到内核中，分别监听读、写、异常动作。这里受到单个进程可以打开的fd数量限制，默认是1024。
- poll：将传入的struct pollfd结构体数组拷贝到内核中进行监听。
- epoll：执行epoll_create会在内核的高速cache区中建立一颗红黑树以及就绪链表(该链表存储已经就绪的文件描述符)。接着用户执行的epoll_ctl函数添加文件描述符会在红黑树上增加相应的结点。

### 2 内核态检测文件描述符读写状态的方式

- select：采用轮询方式，遍历所有fd，最后返回一个描述符读写操作是否就绪的mask掩码，根据这个掩码给fd_set赋值。
- poll：同样采用轮询方式，查询每个fd的状态，如果就绪则在等待队列中加入一项并继续遍历。
- epoll：采用回调机制。在执行epoll_ctl的add操作时，不仅将文件描述符放到红黑树上，而且也注册了回调函数，内核在检测到某文件描述符可读/可写时会调用回调函数，该回调函数将文件描述符放在就绪链表中。

### 3 找到就绪的文件描述符并传递给用户态的方式

- select：将之前传入的fd_set拷贝传出到用户态并返回就绪的文件描述符总数。用户态并不知道是哪些文件描述符处于就绪态，需要遍历来判断。
- poll：将之前传入的fd数组拷贝传出用户态并返回就绪的文件描述符总数。用户态并不知道是哪些文件描述符处于就绪态，需要遍历来判断。
- epoll：epoll_wait只用观察就绪链表中有无数据即可，最后将链表的数据返回给数组并返回就绪的数量。内核将就绪的文件描述符放在传入的数组中，所以只用遍历依次处理即可。这里返回的文件描述符是通过mmap让内核和用户空间共享同一块内存实现传递的，减少了不必要的拷贝。

### 4 重复监听的处理方式

- select：将新的监听文件描述符集合拷贝传入内核中，继续以上步骤。
- poll：将新的struct pollfd结构体数组拷贝传入内核中，继续以上步骤。
- epoll：无需重新构建红黑树，直接沿用已存在的即可。

## epoll高效原理

1. select和poll的动作基本一致，只是poll采用链表来进行文件描述符的存储，而select采用fd标注位来存放，所以select会受到最大连接数的限制，而poll不会。
2. select、poll、epoll虽然都会返回就绪的文件描述符数量。但是select和poll并不会明确指出是哪些文件描述符就绪，而epoll会。造成的区别就是，系统调用返回后，调用select和poll的程序需要遍历监听的整个文件描述符找到是谁处于就绪，而epoll则直接处理即可。
3. select、poll都需要将有关文件描述符的数据结构拷贝进内核，最后再拷贝出来。而epoll创建的有关文件描述符的数据结构本身就存于内核态中，系统调用返回时利用mmap()文件映射内存加速与内核空间的消息传递：即**epoll使用mmap减少复制开销。**
4. select、poll采用轮询的方式来检查文件描述符是否处于就绪态，而epoll采用回调机制。造成的结果就是，随着fd的增加，select和poll的效率会线性降低，而epoll不会受到太大影响，除非活跃的socket很多。
5. epoll的边缘触发模式效率高，系统不会充斥大量不关心的就绪文件描述符

> 虽然epoll的性能最好，但是在连接数少并且连接都十分活跃的情况下，select和poll的性能可能比epoll好，毕竟epoll的通知机制需要很多函数回调。

## epoll高效的本质在于：???与上一个高效原理相似

- 减少了用户态和内核态的文件句柄拷贝
- 减少了对可读可写文件句柄的遍历
- mmap 加速了内核与用户空间的信息传递，epoll是通过内核与用户mmap同一块内存，避免了无谓的内存拷贝
- IO性能不会随着监听的文件描述的数量增长而下降
- 使用红黑树存储fd，以及对应的回调函数，其插入，查找，删除的性能不错，相比于hash，不必预先分配很多的空间















